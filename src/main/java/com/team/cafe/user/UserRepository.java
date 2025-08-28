package com.team.cafe.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {

    // 조회용
    Optional<SiteUser> findByUsername(String username);
    Optional<SiteUser> findByEmail(String email);

    // 중복 체크용
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}