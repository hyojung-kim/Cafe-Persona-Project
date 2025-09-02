package com.team.cafe.cafeListImg.hj;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CafeImageRepository extends JpaRepository<CafeImage, Long> {

    // 단일 카페 대표 이미지 1장
    Optional<CafeImage> findByCafe_Id(Long cafeId);

    // 이번 페이지의 카페들 대표 이미지 일괄 조회
    List<CafeImage> findByCafe_IdIn(Collection<Long> cafeIds);
}