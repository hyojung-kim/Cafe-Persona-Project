package com.team.cafe.list.hj;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface CafeListRepository extends JpaRepository<Cafe, Long> { // ⬅️ Integer → Long
    // 기본 전체 조회 페이징
    Page<Cafe> findAll(Pageable pageable);

    @Query("""
            select c from Cafe c
            where
              ( :kw is null or :kw = ''
                  or lower(c.name)     like lower(concat('%', :kw, '%'))
                  or lower(c.city)     like lower(concat('%', :kw, '%'))
                  or lower(c.district) like lower(concat('%', :kw, '%'))
                  or lower(c.address1) like lower(concat('%', :kw, '%'))
              )
              and ( :parking is null or c.parkingYn = :parking )
              and (
                    :now is null
                    or (
                          c.openTime is not null and c.closeTime is not null and
                          (
                             ( c.openTime <= c.closeTime
                               and c.openTime <= :now and :now <= c.closeTime )
                             or
                             ( c.openTime > c.closeTime
                               and ( :now >= c.openTime or :now <= c.closeTime ) )
                          )
                       )
                  )
            """)
    Page<Cafe> searchWithFilters(@Param("kw") String kw,
                                 @Param("parking") Boolean parking,
                                 @Param("now") java.time.LocalTime now,
                                 Pageable pageable);

    // 이 카페를 좋아요한 유저 수
    @Query("select count(u) from Cafe c join c.likedUsers u where c.id = :cafeId")
    long countLikes(@Param("cafeId") Long cafeId); // ⬅️ Integer → Long

    // 해당 유저가 이 카페를 좋아요 했는지 여부
    boolean existsByIdAndLikedUsers_Id(Long cafeId, Long userId); // ⬅️ Integer → Long


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update Cafe c set c.hitCount = c.hitCount + 1 where c.id = :id")
    int incrementHitCount(@Param("id") Long id);



    @Query(value = """
        SELECT c.*
        FROM cafe c
        JOIN cafe_keyword ck ON ck.cafe_id = c.id
        WHERE (:kw IS NULL OR :kw = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :kw, '%')))
          AND (:parking IS NULL OR c.parking = :parking)
          AND (:now IS NULL OR (c.open_time <= :now AND c.close_time > :now))
          AND ck.keyword_id IN (:ids)
        GROUP BY c.id
        HAVING COUNT(DISTINCT ck.keyword_id) = :size
        ORDER BY
              CASE WHEN :sort = 'name'  AND :dir = 'ASC'  THEN c.name END ASC,
              CASE WHEN :sort = 'name'  AND :dir = 'DESC' THEN c.name END DESC,
              CASE WHEN :sort = 'hit'   AND :dir = 'ASC'  THEN c.hit END ASC,
              CASE WHEN :sort = 'hit'   AND :dir = 'DESC' THEN c.hit END DESC,
              CASE WHEN :sort = 'date'  AND :dir = 'ASC'  THEN c.created_at END ASC,
              CASE WHEN :sort = 'date'  AND :dir = 'DESC' THEN c.created_at END DESC,
              c.created_at DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM (
          SELECT c.id
          FROM cafe c
          JOIN cafe_keyword ck ON ck.cafe_id = c.id
          WHERE (:kw IS NULL OR :kw = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :kw, '%')))
            AND (:parking IS NULL OR c.parking = :parking)
            AND (:now IS NULL OR (c.open_time <= :now AND c.close_time > :now))
            AND ck.keyword_id IN (:ids)
          GROUP BY c.id
          HAVING COUNT(DISTINCT ck.keyword_id) = :size
        ) t
        """,
            nativeQuery = true)
    Page<Cafe> findAllMatchAllWithFiltersCaseOrder(
            @Param("kw") String kw,
            @Param("parking") Boolean parking,
            @Param("now") LocalTime now,
            @Param("ids") List<Long> ids,
            @Param("size") Long size,
            @Param("sort") String sort,   // 👉 여기
            @Param("dir")  String dir,    // 👉 여기
            Pageable pageable
    );
}