package com.team.cafe.review.service;

import com.team.cafe.review.domain.Review;
import com.team.cafe.review.domain.ReviewLike;
import com.team.cafe.review.repository.ReviewLikeRepository;
import com.team.cafe.review.repository.ReviewRepository;
import com.team.cafe.user.sjhy.SiteUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewLikeService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    public ReviewLikeService(ReviewRepository reviewRepository,
                             ReviewLikeRepository reviewLikeRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
    }

    /** 좋아요 토글 */
    public LikeResult toggle(Long reviewId, SiteUser user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        if (review.getUser() != null && review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("자신의 리뷰에는 좋아요를 누를 수 없습니다.");
        }

        boolean likedNow;
        var existing = reviewLikeRepository.findByReviewAndUser(review, user);

        if (existing.isPresent()) {
            reviewLikeRepository.delete(existing.get());
            likedNow = false;
        } else {
            try {
                ReviewLike rl = new ReviewLike();
                rl.setReview(review);
                rl.setUser(user);
                reviewLikeRepository.save(rl);
                likedNow = true;
            } catch (DataIntegrityViolationException ignore) {
                likedNow = true; // 동시성 보정
            }
        }

        long cnt = reviewLikeRepository.countByReview(review);
        review.setLikeCount(Math.max(0, cnt));
        reviewRepository.save(review);

        return new LikeResult(likedNow, cnt);
    }

    public record LikeResult(boolean liked, long count) {}
}
