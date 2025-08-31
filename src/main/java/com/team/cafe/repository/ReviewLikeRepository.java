package com.team.cafe.repository;

import com.team.cafe.domain.Review;
import com.team.cafe.domain.ReviewLike;
import com.team.cafe.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    //해당 리뷰를 해당 유저가 이미 좋아요 했는지 확인
    boolean existsByReviewAndUser(Review review, SiteUser user);
    //리뷰 좋아요수
    long countByReview(Review review);
    //해당 유저와 해당리뷰를 확인해서 이미 좋아요 눌렀으면 취소
    Optional<ReviewLike> findByReviewAndUser(Review review, SiteUser user);
    long deleteByReviewAndUser(Review review, SiteUser user);
}
