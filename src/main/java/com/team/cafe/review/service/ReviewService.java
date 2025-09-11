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
@Transactional // 기본: write. 조회는 메서드에 readOnly=true
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

    /** 카페별 활성 리뷰 페이징 + user/images 즉시 로딩(N+1 방지) */
    @Transactional(readOnly = true)
    public Page<Review> getActiveReviewsByCafeWithUserImages(Long cafeId, Pageable pageable) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        return reviewRepository.findByCafe_IdAndActiveTrueFetchUserImages(cafeId, pageable);
    }

    /** 사용자별 활성 리뷰 페이징 */
    @Transactional(readOnly = true)
    public Page<Review> getActiveReviewsByUser(Long userId, Pageable pageable) {
        Objects.requireNonNull(userId, "userId is required");
        return reviewRepository.findByUser_IdAndActiveTrue(userId, pageable);
    }

    // ========================= 생성 =========================

    /** 리뷰 생성 (+이미지 최대 5장) */
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

        List<String> urls = sanitizeAndLimitImageUrls(imageUrls);

        // 명시적 생성자 사용 (cafe, user, rating, content)
        Review review = new Review(cafe, user, rating, content.trim());

        int order = 0;
        for (String url : urls) {
            ReviewImage img = new ReviewImage();
            img.setImageUrl(url);
            img.setSortOrder(order++);
            review.addImage(img);
        }

        return reviewRepository.save(review);
    }

    // ========================= 수정/삭제 =========================

    /** 리뷰 수정 */
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
        boolean isAdmin = "ADMIN".equalsIgnoreCase(editor.getRole()) || "ROLE_ADMIN".equalsIgnoreCase(editor.getRole());
        if (!isAuthor && !isAdmin) throw new SecurityException("작성자 또는 관리자만 수정할 수 있습니다.");

        validateRatingAndContent(newRating, newContent);

        List<ReviewImage> copy = new ArrayList<>(review.getImages());
        for (ReviewImage img : copy) review.removeImage(img);

        List<String> urls = sanitizeAndLimitImageUrls(newImageUrls);
        int order = 0;
        for (String url : urls) {
            ReviewImage imgNew = new ReviewImage();
            imgNew.setImageUrl(url);
            imgNew.setSortOrder(order++);
            review.addImage(imgNew);
        }

        review.setRating(newRating);
        review.setContent(newContent.trim());
        return reviewRepository.save(review);
    }

    /** 리뷰 삭제 */
    public void deleteReview(Long reviewId, SiteUser requester) {
        Objects.requireNonNull(requester, "requester is required");
        Objects.requireNonNull(reviewId, "reviewId is required");

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        boolean isAuthor = review.getUser() != null && review.getUser().getId().equals(requester.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(requester.getRole()) || "ROLE_ADMIN".equalsIgnoreCase(requester.getRole());
        if (!isAuthor && !isAdmin) throw new SecurityException("작성자 또는 관리자만 삭제할 수 있습니다.");

        reviewRepository.delete(review);
    }

    // ========================= 조회수 =========================

    public void increaseViewCount(Long reviewId) {
        Objects.requireNonNull(reviewId, "reviewId is required");
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));
        review.increaseViewCount();
        reviewRepository.save(review);
    }

    // ========================= 유틸 =========================

    private void validateRatingAndContent(Double rating, String content) {
        if (rating == null || rating < 0.5 || rating > 5.0)
            throw new IllegalArgumentException("별점은 0.5 ~ 5.0 사이여야 합니다.");
        if (content == null || content.trim().length() < 5)
            throw new IllegalArgumentException("리뷰 내용은 5자 이상이어야 합니다.");
    }

    private List<String> sanitizeAndLimitImageUrls(List<String> imageUrls) {
        List<String> urls = new ArrayList<>();
        if (imageUrls == null) return urls;
        for (String u : imageUrls) {
            if (u == null) continue;
            String t = u.trim();
            if (t.isEmpty()) continue;
            urls.add(t);
            if (urls.size() >= MAX_IMAGES) break;
        }
        return urls;
    }
}
