package com.team.cafe.review;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    boolean existsByReview_IdAndUser_Id(Long reviewId, Long userId);
    long countByReview_Id(Long reviewId);
    void deleteByReview_IdAndUser_Id(Long reviewId, Long userId);
}
