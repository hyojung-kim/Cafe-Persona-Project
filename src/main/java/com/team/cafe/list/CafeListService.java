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

    public Page<Cafe> getCafes(String kw, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        String q = (kw == null) ? null : kw.trim(); //null 위험처리
        return cafeRepository.search(q, pageable);
    }

//    public Cafe getCafe(Integer id) {
//        return cafeRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("카페 없음: " + id));
//    }
}
