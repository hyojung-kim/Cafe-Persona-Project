package com.team.cafe.list;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;

@Repository
public interface CafeRepository extends JpaRepository<Cafe, Integer> {
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

    // 유저가 이 카페를 좋아요 했는지 (추후에 사용)
    boolean existsByIdAndLikedUsers_Id(Integer cafeId, Integer userId);

    // 좋아요 수
    @Query("select count(u) from Cafe c join c.likedUsers u where c.id = :cafeId")
    long countLikes(@Param("cafeId") Integer cafeId);
}
