package com.team.cafe.repository;

import com.team.cafe.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByCafe_IdOrderByCreateAtDesc(Long cafeId, Pageable pageable);

    @Query("select coalesce(avg(r.rating),0) from Review r where r.cafe.id = :cafeId")
    Double averageRating(@Param("cafeId") Long cafeId);

    @Modifying
    @Query("update Review r set r.viewCount = r.viewCount + 1 where r.id = :id")
    void incrementView(@Param("id") Long id);
}