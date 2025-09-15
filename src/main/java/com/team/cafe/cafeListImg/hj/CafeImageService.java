package com.team.cafe.cafeListImg.hj;

import com.team.cafe.list.hj.Cafe;
import com.team.cafe.list.hj.CafeListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CafeImageService {
    private final CafeListRepository cafeListRepository;
    private final CafeImageRepository cafeImageRepository;
    private static final Path ROOT = Paths.get("uploads/cafes"); // 로컬 저장 루트


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

    // 카페이미지 저장 메서드
    @Transactional
    public List<CafeImage> saveCafeImages(Long cafeId, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) return List.of();

        Cafe cafe = cafeListRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페가 존재하지 않습니다: " + cafeId));

        Path dir = ROOT.resolve(String.valueOf(cafeId));
        Files.createDirectories(dir);

        List<CafeImage> saved = new ArrayList<>();
        for (MultipartFile f : files) {
            if (f.isEmpty()) continue;

            String ext = StringUtils.getFilenameExtension(f.getOriginalFilename());
            String filename = UUID.randomUUID().toString() + (ext != null ? "." + ext.toLowerCase() : "");
            Path dest = dir.resolve(filename);

            // 실제 저장
            f.transferTo(dest.toFile());

            // 정적 서빙 URL
            String url = "/uploads/cafes/" + cafeId + "/" + filename;

            CafeImage entity = CafeImage.builder()
                    .cafe(cafe)
                    .imgUrl(url)
                    .build();

            saved.add(cafeImageRepository.save(entity));
        }
        return saved;
    }

    public List<CafeImage> findAllByCafeId(Long cafeId) {
        return cafeImageRepository.findAllByCafe_Id(cafeId);
    }


}
