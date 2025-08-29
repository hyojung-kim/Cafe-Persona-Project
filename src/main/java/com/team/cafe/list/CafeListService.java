package com.team.cafe.list;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CafeListService {
    private final CafeRepository cafeRepository;

    public List<Cafe> getAllCafes() {
        return cafeRepository.findAll();
    }

    public Page<Cafe> getCafePage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        return cafeRepository.findAll(pageable);
    }
//    public Cafe getCafe(Integer id) {
//        return cafeRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("카페 없음: " + id));
//    }
}
