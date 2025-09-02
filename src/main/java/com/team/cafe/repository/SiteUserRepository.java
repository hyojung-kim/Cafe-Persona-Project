package com.team.cafe.repository;

import com.team.cafe.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteUserRepository extends JpaRepository<SiteUser, Long> {

    /** username으로 단건 조회 */
    Optional<SiteUser> findByUsername(String username);

    /** email로 단건 조회 */
    Optional<SiteUser> findByEmail(String email);

    /** username 존재 여부 */
    boolean existsByUsernameIgnoreCase(String username);

    /** email 존재 여부 */
    boolean existsByEmailIgnoreCase(String email);
}
