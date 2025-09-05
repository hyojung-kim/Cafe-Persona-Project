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

    /** 카페별 활성 리뷰 페이징 + author/images 동시 로딩(N+1 방지) */
    @Transactional(readOnly = true)
    public Page<Review> getActiveReviewsByCafeWithAuthorImages(Long cafeId, Pageable pageable) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        return reviewRepository.findByCafe_IdAndActiveTrueFetchAuthorImages(cafeId, pageable);
    }

    /** 작성자별 활성 리뷰 페이징 */
    @Transactional(readOnly = true)
    public Page<Review> getActiveReviewsByAuthor(Long authorId, Pageable pageable) {
        Objects.requireNonNull(authorId, "authorId is required");
        return reviewRepository.findByAuthor_IdAndActiveTrue(authorId, pageable);
    }

    // ========================= 생성 =========================

    /**
     * 리뷰 생성 + 이미지 최대 5장 저장
     * - 빈/공백 URL 제거 후 최대 5개만 반영
     * - 부모(review)만 save (cascade = ALL, orphanRemoval = true)
     */
    public Review createReview(Long cafeId,
                               SiteUser author,
                               Double rating,
                               String content,
                               List<String> imageUrls) {

        Objects.requireNonNull(author, "author is required");
        Objects.requireNonNull(cafeId, "cafeId is required");

        Cafe cafe = cafeListRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다. id=" + cafeId));

        validateRatingAndContent(rating, content);

        List<String> urls = sanitizeAndLimitImageUrls(imageUrls); // 최대 5개, 공백 제거

        // 부모 엔티티 생성
        Review review = new Review();
        review.setCafe(cafe);
        review.setAuthor(author);
        review.setRating(rating);
        review.setContent(content.trim());

        // 자식(이미지) 추가
        int order = 0;
        for (String url : urls) {
            ReviewImage img = new ReviewImage();
            img.setImageUrl(url);
            img.setSortOrder(order++);
            review.addImage(img); // 양방향 연관관계 일관성 보장
        }

        // 부모만 저장하면 cascade로 자식도 저장
        return reviewRepository.save(review);
    }

    // ========================= 수정 =========================

    /**
     * 리뷰 수정(작성자 또는 관리자)
     * - 내용/별점/이미지 전체 교체 (orphanRemoval 작동)
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

        boolean isAuthor = review.getAuthor() != null && review.getAuthor().getId().equals(editor.getId());
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

        // 기존 이미지 전량 제거 (orphanRemoval=true)
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            List<ReviewImage> copy = new ArrayList<>(review.getImages());
            for (ReviewImage img : copy) {
                review.removeImage(img); // 컬렉션 제거 + img.review = null + sort 재정렬(도메인 로직에 따름)
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

        boolean isAuthor = review.getAuthor() != null && review.getAuthor().getId().equals(requester.getId());
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

        if (review.getAuthor() != null && review.getAuthor().getId().equals(liker.getId())) {
            throw new IllegalArgumentException("자신의 리뷰에는 좋아요를 누를 수 없습니다.");
        }

        review.addLike();              // 엔티티가 하한/중복 등 자체 규칙을 보장
        reviewRepository.save(review);
    }

    /** 좋아요 취소(하한 0) */
    public void unlikeReview(Long reviewId, SiteUser liker) {
        Objects.requireNonNull(liker, "liker is required");
        Objects.requireNonNull(reviewId, "reviewId is required");

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        review.removeLike();           // 엔티티에서 0 미만 방지
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
        if (content == null || content.trim().length() < 50) {
            throw new IllegalArgumentException("리뷰 내용은 50자 이상이어야 합니다.");
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
            if (urls.size() == 5) break; // 최대 5개
        }
        return urls;
    }
}
