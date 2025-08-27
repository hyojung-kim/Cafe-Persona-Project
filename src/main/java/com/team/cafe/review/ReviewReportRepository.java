package com.team.cafe.review;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    boolean existsByReview_IdAndReporter_Id(Long reviewId, Long userId);
}
