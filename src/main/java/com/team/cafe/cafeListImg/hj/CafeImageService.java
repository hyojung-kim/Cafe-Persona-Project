package com.team.cafe.cafeListImg.hj;

import com.team.cafe.list.hj.CafeListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CafeImageService {
    private final CafeListRepository cafeListRepository;
    private final CafeImageRepository cafeImageRepository;

    /** cafeId -> 대표이미지 URL (없으면 키 자체가 없음) */
    public Map<Long, String> getImageUrlMap(Collection<Long> cafeIds) {
        if (cafeIds == null || cafeIds.isEmpty()) return Map.of();

        List<CafeImage> images = cafeImageRepository.findByCafe_IdIn(cafeIds);

        // 중복 키는 첫 값 유지(유니크 제약 있으면 중복 자체가 없음)
        return images.stream().collect(Collectors.toMap(
                img -> img.getCafe().getId(),
                CafeImage::getImgUrl,
                (a, b) -> a
        ));
    }

}