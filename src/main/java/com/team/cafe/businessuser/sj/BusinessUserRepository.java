package com.team.cafe.businessuser.sj;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessUserRepository extends JpaRepository<BusinessUser, Long> {

    // 사업자번호 중복 체크
    boolean existsByBusinessNumber(String businessNumber);

    // 대표 이메일 중복 체크 (선택)
    Optional<BusinessUser> findByRepresentativeEmail(String email);
}
