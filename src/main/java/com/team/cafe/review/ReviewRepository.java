package com.team.cafe.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 페이징 조회
    Page<Review> findByCafe_IdAndActiveTrue(Long cafeId, Pageable pageable);

    // 전체 조회
    List<Review> findByCafe_IdAndActiveTrue(Long cafeId);

    Page<Review> findByAuthor_IdAndActiveTrue(Long authorId, Pageable pageable);

    // fetch join 예시 (카페별 + author/images 동시 로딩)
    @EntityGraph(attributePaths = {"author", "images"})
    @Query("SELECT r FROM Review r WHERE r.cafe.id = :cafeId AND r.active = true")
    Page<Review> findByCafe_IdAndActiveTrueFetchAuthorImages(@Param("cafeId") Long cafeId, Pageable pageable);

    long countByCafe_IdAndActiveTrue(Long cafeId);

    // ✅ 리뷰 단건 조회 시 author + images fetch join
    @EntityGraph(attributePaths = {"author", "images"})
    @Query("SELECT r FROM Review r WHERE r.id = :id")
    Optional<Review> findWithAuthorAndImagesById(@Param("id") Long id);

    // ✅ 활성 리뷰의 평균 평점
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.cafe.id = :cafeId AND r.active = true")
    Double calculateAverageRating(@Param("cafeId") Long cafeId);
}
