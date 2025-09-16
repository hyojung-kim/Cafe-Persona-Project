package com.team.cafe.Root;

import com.team.cafe.list.hj.Cafe;
import com.team.cafe.list.hj.CafeListRepository;
import com.team.cafe.list.hj.CafeListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MainService {
    private final MainRepository mainRepository;

    public List<Cafe> getTop16ByViews() {
        return mainRepository.findTop8ByOrderByHitCountDesc();
    }

}
