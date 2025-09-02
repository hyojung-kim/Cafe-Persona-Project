package com.team.cafe.repository;

import com.team.cafe.domain.Cafe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;          // ✅
import org.springframework.data.repository.query.Param;   // ✅

public interface CafeRepository extends JpaRepository<Cafe, Long> {

    /** 활성 카페 페이징 */
    Page<Cafe> findByActiveTrue(Pageable pageable);

    /** 이름 부분검색 (활성만) */
    Page<Cafe> findByActiveTrueAndNameContainingIgnoreCase(String keyword, Pageable pageable);

    /** 카테고리 부분검색 (활성만) */
    Page<Cafe> findByActiveTrueAndCategoryCodeContainingIgnoreCase(String categoryCode, Pageable pageable);

    /** 이름+주소 중복 방지용 존재 체크 */
    boolean existsByNameIgnoreCaseAndAddressIgnoreCase(String name, String address);

    /**
     * 카페별 활성 리뷰 통계를 한 번에 조회(평균 평점, 리뷰 개수).
     * - 네이티브 쿼리 사용(Hibernate/DB별 CASE 집계 호환성 확보)
     */
    // ===== 통계 1회 조회 (상세)도 기존에 추가해둠 =====
    @Query(value = """
            SELECT 
              COALESCE(AVG(r.rating), 0)   AS avgRating,
              COALESCE(COUNT(r.id), 0)     AS reviewCount
            FROM reviews r
            WHERE r.cafe_id = :cafeId
              AND r.is_active = true
            """, nativeQuery = true)
    CafeStatsProjection getActiveStatsByCafeId(@Param("cafeId") Long cafeId);

    interface CafeStatsProjection {
        Double getAvgRating();
        Long getReviewCount();
    }

    // ========================= 이번 턴: 목록 + 통계 동시 조회 =========================

    /**
     * 활성 카페 목록 + 활성 리뷰 통계(평균/개수)까지 한 번에 페이지 조회.
     * - 정렬은 Pageable(예: created_at DESC)로 제어
     * - 템플릿/컨트롤러에서 Projection의 필드를 바로 사용 가능
     */
    @Query(
            value = """
            SELECT 
              c.id                          AS id,
              c.name                        AS name,
              c.address                     AS address,
              c.phone                       AS phone,
              c.category_code               AS categoryCode,
              c.is_active                   AS active,
              c.created_at                  AS createdAt,
              COALESCE(AVG(r.rating), 0)    AS avgRating,
              COALESCE(COUNT(r.id), 0)      AS reviewCount
            FROM cafes c
            LEFT JOIN reviews r
              ON r.cafe_id = c.id
             AND r.is_active = true
            WHERE c.is_active = true
            GROUP BY c.id, c.name, c.address, c.phone, c.category_code, c.is_active, c.created_at
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM cafes c
            WHERE c.is_active = true
            """,
            nativeQuery = true
    )
    Page<CafeListProjection> findActiveCafesWithStats(Pageable pageable);

    /**
     * 목록 행 Projection (SELECT 별칭과 동일한 getter 이름 필요)
     */
    interface CafeListProjection {
        Long getId();
        String getName();
        String getAddress();
        String getPhone();
        String getCategoryCode();
        Boolean getActive();
        java.time.OffsetDateTime getCreatedAt();  // DB 컬럼 타입에 맞춰 LocalDateTime 등으로 바꿔도 됨
        Double getAvgRating();
        Long getReviewCount();
    }
}