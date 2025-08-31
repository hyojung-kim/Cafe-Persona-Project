package com.team.cafe.repository;

import com.team.cafe.domain.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
}