package com.team.cafe.keyword.hj;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    // 타입 ASC → 이름 ASC
    @EntityGraph(attributePaths = {"type"})
    List<Keyword> findAllByOrderByTypeAscNameAsc();
//    SELECT k
//    FROM Keyword k
//    ORDER BY k.type ASC, k.name ASC
}
