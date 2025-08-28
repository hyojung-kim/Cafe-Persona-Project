package com.team.cafe.review;

import com.team.cafe.domain.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    boolean existsByReview_IdAndUser_Id(Long reviewId, Long userId);
    long countByReview_Id(Long reviewId);
    void deleteByReview_IdAndUser_Id(Long reviewId, Long userId);
}
