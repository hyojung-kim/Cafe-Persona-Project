package com.team.cafe.service;

import com.team.cafe.domain.Cafe;
import com.team.cafe.domain.Review;
import com.team.cafe.domain.SiteUser;
import com.team.cafe.repository.CafeRepository;
import com.team.cafe.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ReviewCafeService {

    private final CafeRepository cafeRepository;
    private final ReviewRepository reviewRepository;

    public ReviewCafeService(CafeRepository cafeRepository,
                             ReviewRepository reviewRepository) {
        this.cafeRepository = cafeRepository;
        this.reviewRepository = reviewRepository;
    }

    // ========================= 카페 리뷰 조회 =========================

    /** 특정 카페의 활성 리뷰 전체 조회 */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Review> getActiveReviewsByCafe(Long cafeId) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        return reviewRepository.findByCafe_IdAndActiveTrue(cafeId);
    }

    /** 특정 카페의 리뷰 개수 */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public long getReviewCountByCafe(Long cafeId) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        return reviewRepository.countByCafe_IdAndActiveTrue(cafeId);
    }

    // ========================= 카페 리뷰 생성 =========================

    /**
     * 카페 리뷰 생성
     */
    public Review createCafeReview(Long cafeId,
                                   SiteUser author,
                                   Double rating,
                                   String content) {
        Objects.requireNonNull(author, "author is required");

        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다. id=" + cafeId));

        if (rating == null || rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("별점은 1.0 ~ 5.0 사이여야 합니다.");
        }
        if (content == null || content.trim().length() < 50) {
            throw new IllegalArgumentException("리뷰 내용은 50자 이상이어야 합니다.");
        }

        // ✅ 빌더 제거 → new Review() + setter
        Review review = new Review();
        review.setCafe(cafe);
        review.setAuthor(author);
        review.setRating(rating);
        review.setContent(content.trim());

        return reviewRepository.save(review);
    }

    // ========================= 카페 리뷰 활성/비활성 =========================

    /** 리뷰 비활성화 */
    public void deactivateReview(Long reviewId, SiteUser requester) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        boolean isAuthor = review.getAuthor() != null && review.getAuthor().getId().equals(requester.getId());
        boolean isAdmin = requester.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r));
        if (!isAuthor && !isAdmin) {
            throw new SecurityException("작성자 또는 관리자만 비활성화할 수 있습니다.");
        }

        review.setActive(false);
        reviewRepository.save(review);
    }

    /** 리뷰 활성화 (관리자 전용) */
    public void activateReview(Long reviewId, SiteUser requester) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        boolean isAdmin = requester.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r));
        if (!isAdmin) {
            throw new SecurityException("관리자만 활성화할 수 있습니다.");
        }

        review.setActive(true);
        reviewRepository.save(review);
    }
}
