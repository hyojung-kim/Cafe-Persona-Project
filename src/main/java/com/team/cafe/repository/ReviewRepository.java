package com.team.cafe.repository;

import com.team.cafe.domain.Review;
import com.team.cafe.domain.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * ReviewRepository
 * - Review 엔티티와 DB를 연결해주는 DAO
 * - 기본 CRUD는 JpaRepository<Review, Long> 상속으로 자동 제공
 * - 리뷰 조회/평균별점/조회수 증가 등 커스텀 기능 추가
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 특정 카페(cafeId)에 속한 리뷰들을 작성일(createdAt) 기준 내림차순으로 페이징 조회
     *
     * 메서드 이름 규칙:
     * findByCafe_IdOrderByCreatedAtDesc → Review.cafe.id를 조건으로, createdAt 기준 내림차순 정렬
     *
     * SQL 예시:
     * SELECT * FROM review
     * WHERE cafe_id = ?
     * ORDER BY created_at DESC
     * LIMIT ?, ?;
     */
    Page<Review> findByCafe_IdOrderByCreatedAtDesc(Long cafeId, Pageable pageable);

    /**
     * 특정 카페의 리뷰 평균 별점 구하기
     * - coalesce(avg(r.rating),0): 리뷰가 없을 경우 NULL 대신 0 반환
     *
     * JPQL 예시:
     * SELECT coalesce(avg(r.rating),0)
     * FROM Review r
     * WHERE r.cafe.id = :cafeId
     */
    @Query("select coalesce(avg(r.rating),0) from Review r where r.cafe.id = :cafeId")
    Double averageRating(@Param("cafeId") Long cafeId);

    /**
     * 리뷰 조회수(viewCount) +1 증가
     * - JPQL update 쿼리 사용 (@Modifying 필요)
     * - EntityManager 캐시 무시하고 DB 값 직접 업데이트
     *
     * JPQL 예시:
     * UPDATE Review r
     * SET r.viewCount = r.viewCount + 1
     * WHERE r.id = :id
     */
    @Modifying
    @Query("update Review r set r.viewCount = r.viewCount + 1 where r.id = :id")
    void incrementView(@Param("id") Long id);
}
