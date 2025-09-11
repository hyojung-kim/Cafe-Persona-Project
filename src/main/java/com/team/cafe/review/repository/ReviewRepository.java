package com.team.cafe.review.repository;

import com.team.cafe.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /* ========== 페이징/전체 조회 ========== */

    /** 카페별 활성 리뷰 (user 즉시 로딩으로 N+1 축소) */
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByCafe_IdAndActiveTrue(Long cafeId, Pageable pageable);

    /** 카페별 활성 리뷰 전체 */
    List<Review> findByCafe_IdAndActiveTrue(Long cafeId);

    /** 사용자별 활성 리뷰 (user 즉시 로딩) */
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByUser_IdAndActiveTrue(Long userId, Pageable pageable);

    /** 카페별 활성 리뷰 (user + images 즉시 로딩; 서비스에서 사용) */
    @EntityGraph(attributePaths = {"user", "images"})
    @Query("SELECT r FROM Review r WHERE r.cafe.id = :cafeId AND r.active = true")
    Page<Review> findByCafe_IdAndActiveTrueFetchUserImages(@Param("cafeId") Long cafeId, Pageable pageable);

    /** 활성 리뷰 개수 */
    long countByCafe_IdAndActiveTrue(Long cafeId);

    /* ========== 단건 상세/통계 ========== */

    /**
     * 상세: 연관된 user, cafe, images를 즉시 로딩하여 지연 초기화 예외 방지
     */
    @EntityGraph(attributePaths = {"user", "cafe", "images"})
    @Query("SELECT r FROM Review r WHERE r.id = :id")
    Optional<Review> findWithUserAndImagesById(@Param("id") Long id);

    /** 활성 리뷰 평균 평점 */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.cafe.id = :cafeId AND r.active = true")
    Double calculateAverageRating(@Param("cafeId") Long cafeId);

    boolean existsByIdAndLikedUsers_Id(Long reviewId, Long userId);

    @Query("select count(u) from Review r join r.likedUsers u where r.id = :reviewId")
    long countLikes(@Param("reviewId") Long reviewId);
}
