package com.team.cafe.review.service;

import com.team.cafe.list.hj.Cafe;
import com.team.cafe.list.hj.CafeListRepository;
import com.team.cafe.review.domain.Review;
import com.team.cafe.review.domain.ReviewImage;
import com.team.cafe.review.repository.ReviewRepository;
import com.team.cafe.user.sjhy.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional // 기본: write 트랜잭션. 읽기 전용 메서드에만 readOnly=true 지정
public class ReviewService {

    private static final int MAX_IMAGES = 5;

    private final CafeListRepository cafeListRepository;
    private final ReviewRepository reviewRepository;

    public ReviewService(CafeListRepository cafeListRepository,
                         ReviewRepository reviewRepository) {
        this.cafeListRepository = cafeListRepository;
        this.reviewRepository = reviewRepository;
    }

    // ========================= 조회 =========================

    /** 카페별 활성 리뷰 페이징 */
    @Transactional(readOnly = true)
    public Page<Review> getActiveReviewsByCafe(Long cafeId, Pageable pageable) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        return reviewRepository.findByCafe_IdAndActiveTrue(cafeId, pageable);
    }

    /** 카페별 활성 리뷰 페이징 + user/images 동시 로딩(N+1 방지) */
    @Transactional(readOnly = true)
    public Page<Review> getActiveReviewsByCafeWithUserImages(Long cafeId, Pageable pageable) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        return reviewRepository.findByCafe_IdAndActiveTrueFetchUserImages(cafeId, pageable);
    }

    /** 작성자(user)별 활성 리뷰 페이징 */
    @Transactional(readOnly = true)
    public Page<Review> getActiveReviewsByUser(Long userId, Pageable pageable) {
        Objects.requireNonNull(userId, "userId is required");
        return reviewRepository.findByUser_IdAndActiveTrue(userId, pageable);
    }

    // ========================= 생성 =========================

    /**
     * 리뷰 생성 + 이미지 최대 5장 저장
     * - 빈/공백 URL 제거 후 최대 5개만 반영
     * - 부모(review)만 save (cascade = ALL, orphanRemoval = true 전제)
     * - 양방향 연관관계 세팅은 Review.addImage(...)에 위임 (img.review = this 세팅)
     */
    public Review createReview(Long cafeId,
                               SiteUser user,
                               Double rating,
                               String content,
                               List<String> imageUrls) {

        Objects.requireNonNull(user, "user is required");
        Objects.requireNonNull(cafeId, "cafeId is required");

        Cafe cafe = cafeListRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다. id=" + cafeId));

        validateRatingAndContent(rating, content);

        List<String> urls = sanitizeAndLimitImageUrls(imageUrls); // 최대 5개, 공백 제거

        // 부모 엔티티 생성
        Review review = new Review(cafe, user, rating, content.trim());
        review.setCafe(cafe);
        review.setUser(user);
        review.setRating(rating);
        review.setContent(content.trim());

        // 자식(이미지) 추가 — 도메인 헬퍼 사용 (양방향 일관성 보장)
        int order = 0;
        for (String url : urls) {
            ReviewImage img = new ReviewImage();
            img.setImageUrl(url);
            img.setSortOrder(order++);
            review.addImage(img); // 내부에서 img.setReview(this)
        }

        // 부모만 저장하면 cascade로 자식도 저장
        return reviewRepository.save(review);
    }

    // ========================= 수정 =========================

    /**
     * 리뷰 수정(작성자 또는 관리자)
     * - 내용/별점/이미지 전체 교체 (orphanRemoval 작동)
     * - 이미지 교체 시 Review.removeImage(...) 사용 (img.review = null 세팅)
     */
    public Review updateReview(Long reviewId,
                               SiteUser editor,
                               Double newRating,
                               String newContent,
                               List<String> newImageUrls) {

        Objects.requireNonNull(editor, "editor is required");
        Objects.requireNonNull(reviewId, "reviewId is required");

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        boolean isAuthor = review.getUser() != null && review.getUser().getId().equals(editor.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(editor.getRole())
                || "ROLE_ADMIN".equalsIgnoreCase(editor.getRole());
        if (!isAuthor && !isAdmin) {
            throw new SecurityException("작성자 또는 관리자만 수정할 수 있습니다.");
        }

        validateRatingAndContent(newRating, newContent);

        List<String> urls = sanitizeAndLimitImageUrls(newImageUrls);

        // 본문/별점 교체
        review.setRating(newRating);
        review.setContent(newContent.trim());

        // 기존 이미지 전량 제거 (orphanRemoval=true 전제)
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            List<ReviewImage> copy = new ArrayList<>(review.getImages());
            for (ReviewImage img : copy) {
                review.removeImage(img);
            }
        }

        // 새 이미지 추가
        int order = 0;
        for (String url : urls) {
            ReviewImage imgNew = new ReviewImage();
            imgNew.setImageUrl(url);
            imgNew.setSortOrder(order++);
            review.addImage(imgNew);
        }

        return reviewRepository.save(review);
    }

    // ========================= 삭제 =========================

    /**
     * 리뷰 삭제(작성자 또는 관리자)
     * - Review 삭제 시, 자식 이미지들(orphanRemoval)도 함께 제거됨
     */
    public void deleteReview(Long reviewId, SiteUser requester) {
        Objects.requireNonNull(requester, "requester is required");
        Objects.requireNonNull(reviewId, "reviewId is required");

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        boolean isAuthor = review.getUser() != null && review.getUser().getId().equals(requester.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(requester.getRole())
                || "ROLE_ADMIN".equalsIgnoreCase(requester.getRole());
        if (!isAuthor && !isAdmin) {
            throw new SecurityException("작성자 또는 관리자만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }

    // ========================= 좋아요 / 조회수 =========================

    /** 좋아요 추가 (본인 리뷰 금지) */
    public void likeReview(Long reviewId, SiteUser liker) {
        Objects.requireNonNull(liker, "liker is required");
        Objects.requireNonNull(reviewId, "reviewId is required");

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        if (review.getUser() != null && review.getUser().getId().equals(liker.getId())) {
            throw new IllegalArgumentException("자신의 리뷰에는 좋아요를 누를 수 없습니다.");
        }

        review.addLike();
        reviewRepository.save(review);
    }

    /** 좋아요 취소(하한 0) */
    public void unlikeReview(Long reviewId, SiteUser liker) {
        Objects.requireNonNull(liker, "liker is required");
        Objects.requireNonNull(reviewId, "reviewId is required");

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        review.removeLike();
        reviewRepository.save(review);
    }

    /** 조회수 증가 */
    public void increaseViewCount(Long reviewId) {
        Objects.requireNonNull(reviewId, "reviewId is required");

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));
        review.increaseViewCount();
        reviewRepository.save(review);
    }

    // ========================= 내부 유틸 =========================

    private void validateRatingAndContent(Double rating, String content) {
        if (rating == null || rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("별점은 1.0 ~ 5.0 사이여야 합니다.");
        }
        if (content == null || content.trim().length() < 5) {
            throw new IllegalArgumentException("리뷰 내용은 5자 이상이어야 합니다.");
        }
    }

    /**
     * - null 안전
     * - trim 후 빈 문자열 제거
     * - 최대 5개로 제한
     */
    private List<String> sanitizeAndLimitImageUrls(List<String> imageUrls) {
        List<String> urls = new ArrayList<>();
        if (imageUrls == null) return urls;

        for (String u : imageUrls) {
            if (u == null) continue;
            String t = u.trim();
            if (t.isEmpty()) continue;
            urls.add(t);
            if (urls.size() >= MAX_IMAGES) break; // 최대 5개
        }
        return urls;
    }
}
