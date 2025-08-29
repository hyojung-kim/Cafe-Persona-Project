package com.team.cafe.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CafeListService {
    private final CafeRepository cafeRepository;

    public List<Cafe> getAllCafes() {
        return cafeRepository.findAll();
    }

//    public Cafe getCafe(Integer id) {
//        return cafeRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("카페 없음: " + id));
//    }
}
