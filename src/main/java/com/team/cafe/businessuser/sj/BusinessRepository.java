package com.team.cafe.businessuser.sj;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByBusinessNumber(String businessNumber);

    Optional<Business> findByBusinessNumber(String businessNumber);
}
