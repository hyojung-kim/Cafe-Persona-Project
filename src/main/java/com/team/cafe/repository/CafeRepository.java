package com.team.cafe.repository;

import com.team.cafe.domain.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CafeRepository
 * - Cafe 엔티티를 DB에서 조회/저장/수정/삭제할 수 있게 해주는 Repository 인터페이스
 * - JpaRepository<Cafe, Long>을 상속받으면 기본 CRUD 메서드 자동 제공
 *
 * 예시)
 * cafeRepository.save(cafe) → 카페 저장
 * cafeRepository.findById(1L) → ID=1 카페 조회
 * cafeRepository.findAll() → 전체 카페 목록 조회
 * cafeRepository.deleteById(1L) → ID=1 카페 삭제
 */
public interface CafeRepository extends JpaRepository<Cafe, Long> {
    // JpaRepository<엔티티 타입, PK 타입>
    // 여기서는 Cafe 엔티티, PK 타입은 Long

    // ⚡ 커스텀 메서드도 쉽게 추가 가능 (Spring Data JPA 메서드 쿼리 기능)
    // 예: List<Cafe> findByCategory(String category);
    // 예: Optional<Cafe> findByName(String name);
}
