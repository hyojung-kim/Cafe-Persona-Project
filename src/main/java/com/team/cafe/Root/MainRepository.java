package com.team.cafe.Root;

import com.team.cafe.list.hj.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MainRepository extends JpaRepository<Cafe, Long> {
    List<Cafe> findTop8ByOrderByHitCountDesc();
}
