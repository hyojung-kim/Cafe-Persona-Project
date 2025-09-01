package com.team.cafe.repository;

import com.team.cafe.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
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
     * - 템플릿에서 rv.author.username 접근 시 Lazy 폭탄 방지를 위해 author를 미리 로딩
     *   (컬렉션(images)은 여기서 로딩하지 않음: 페이지네이션 왜곡 방지)
     */
    @EntityGraph(attributePaths = {"author"})
    Page<Review> findByCafe_IdOrderByCreatedAtDesc(Long cafeId, Pageable pageable);

    /**
     * 페이징 ID 전용 쿼리
     * - 서비스에서 먼저 이 메서드로 리뷰 ID 페이지만 뽑고,
     *   그 ID들로 ReviewImageRepository에서 이미지들을 IN 조회로 일괄 로딩하여
     *   템플릿에서는 rv.images 직접 접근 대신 Map<reviewId, images>로 안전하게 사용
     */
    @Query("""
            select r.id
            from Review r
            where r.cafe.id = :cafeId
            order by r.createdAt desc
            """)
    Page<Long> findIdsByCafeIdOrderByCreatedAtDesc(@Param("cafeId") Long cafeId, Pageable pageable);

    /**
     * 특정 카페의 리뷰 평균 별점
     * - 리뷰가 없을 경우 0 반환
     */
    @Query("select coalesce(avg(r.rating),0) from Review r where r.cafe.id = :cafeId")
    Double averageRating(@Param("cafeId") Long cafeId);

    /**
     * 리뷰 조회수(viewCount) +1
     */
    @Modifying
    @Query("update Review r set r.viewCount = r.viewCount + 1 where r.id = :id")
    void incrementView(@Param("id") Long id);
}

