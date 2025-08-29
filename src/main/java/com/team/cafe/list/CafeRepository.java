package com.team.cafe.list;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CafeRepository extends JpaRepository<Cafe, Integer> {
    // 기본 전체 조회 페이징
    Page<Cafe> findAll(Pageable pageable);
}
