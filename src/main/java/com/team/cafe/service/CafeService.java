package com.team.cafe.service;


import com.team.cafe.repository.CafeRepository;
import com.team.cafe.domain.Cafe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CafeService {
    private final CafeRepository cafeRepository;
    public Cafe getCafeOrThrow(Long id) {
        return cafeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카페"));
    }
}
