package com.team.cafe.review;

import com.team.cafe.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"author", "cafe"})
    Page<Review> findByCafe_IdAndStatus(Long cafeId, ReviewStatus status, Pageable pageable);

    @Query("select coalesce(avg(r.rating.halfStars),0) from Review r " +
            "where r.cafe.id = :cafeId and r.status = com.example.cafepersona.review.ReviewStatus.ACTIVE")
    Double avgHalfStarsByCafeId(Long cafeId); // 반환값은 halfStars 평균(0~10), 서비스에서 /2.0

    @Query("select count(r) from Review r " +
            "where r.cafe.id = :cafeId and r.status = com.example.cafepersona.review.ReviewStatus.ACTIVE")
    long countActiveByCafeId(Long cafeId);

    @EntityGraph(attributePaths = {"author","cafe"})
    Optional<Review> findById(Long id);
}

