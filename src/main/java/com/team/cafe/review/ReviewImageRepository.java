package com.team.cafe.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByReview_IdOrderBySortOrderAsc(Long reviewId);
    long countByReview_Id(Long reviewId);
}
