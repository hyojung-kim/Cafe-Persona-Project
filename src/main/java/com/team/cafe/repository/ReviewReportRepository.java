package com.team.cafe.repository;

import com.team.cafe.domain.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    boolean existsByReview_IdAndReporter_Id(Long reviewId, Long userId);
}
