package com.team.cafe.repository;

import com.team.cafe.domain.Review;
import com.team.cafe.domain.ReviewLike;
import com.team.cafe.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    boolean existsByReview_IdAndUser_Id(Long reviewId, SiteUser user);
    long countByReview(Review review);
    Optional<ReviewLike> findByRevewAndUser(Review review, SiteUser user);
    long deleteByReviewAndUser(Review review, SiteUser user);
}
