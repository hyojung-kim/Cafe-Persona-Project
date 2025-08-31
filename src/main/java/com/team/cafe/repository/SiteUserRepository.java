package com.team.cafe.repository;

import com.team.cafe.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * SiteUserRepository
 * - SiteUser 엔티티를 DB와 연결해 CRUD 및 추가 쿼리 메서드를 제공
 * - 회원 가입/로그인/중복 체크 기능을 쉽게 구현할 수 있음
 */
public interface SiteUserRepository extends JpaRepository<SiteUser, Long> {

    /**
     * username으로 회원 조회
     * - 로그인 시 사용자 정보 확인할 때 사용
     *
     * SQL 예시:
     * SELECT * FROM site_user WHERE username = ?;
     */
    Optional<SiteUser> findByUsername(String username);

    /**
     * 특정 username이 이미 존재하는지 여부 확인
     * - 회원 가입 시 중복 아이디 체크할 때 사용
     *
     * SQL 예시:
     * SELECT EXISTS (
     *   SELECT 1 FROM site_user WHERE username = ?
     * );
     */
    boolean existsByUsername(String username);
}
