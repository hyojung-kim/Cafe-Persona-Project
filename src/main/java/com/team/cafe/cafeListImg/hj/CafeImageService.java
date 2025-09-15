package com.team.cafe.cafeListImg.hj;

import com.team.cafe.list.hj.Cafe;
import com.team.cafe.list.hj.CafeListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * CafeImageService - 카페 이미지 저장/조회
 *
 * - 저장 루트는 절대경로(app.upload.dir) 하위의 /uploads/cafes/{cafeId}/ 로 고정
 * - 정적 서빙은 properties의
 *     spring.web.resources.static-locations=classpath:/static/,file:${app.upload.dir}/
 *   과 매칭되도록 URL은 /uploads/cafes/... 형태로 저장
 */
@Service
@RequiredArgsConstructor
public class CafeImageService {

    private final CafeListRepository cafeListRepository;
    private final CafeImageRepository cafeImageRepository;

    /** application.properties: app.upload.dir=${user.home}/cafe-review/uploads */
    @Value("${app.upload.dir}")
    private String uploadRoot; // 절대경로 루트

    /** 최종 물리 저장 루트: ${app.upload.dir}/uploads/cafes */
    private Path physicalRoot() {
        // 예) /Users/you/cafe-review/uploads/uploads/cafes
        return Paths.get(uploadRoot).resolve("uploads").resolve("cafes");
    }

    /**
     * 이번 페이지의 카페들에 대한 대표 이미지 URL 맵 (cafeId -> imgUrl)
     * - 같은 카페에 이미지가 여러 장 있어도 첫 값 유지
     * - 이미지가 없으면 키 자체가 없음
     */
    public Map<Long, String> getImageUrlMap(Collection<Long> cafeIds) {
        if (cafeIds == null || cafeIds.isEmpty()) return Map.of();

        List<CafeImage> images = cafeImageRepository.findByCafe_IdIn(cafeIds);

        // 중복 키는 첫 값 유지(유니크 제약 있으면 중복 자체가 없음)
        return images.stream().collect(Collectors.toMap(
                img -> img.getCafe().getId(),
                CafeImage::getImgUrl,
                (a, b) -> a,                    // 중복키: 첫 값 유지
                LinkedHashMap::new
        ));
    }

    public Cafe getDetailImg(Long cafeId) {
        return cafeImageRepository.findByIdWithImages(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("no cafe: " + cafeId));
    }

    /** 단일 카페의 모든 이미지 조회 */
    public List<CafeImage> findAllByCafeId(Long cafeId) {
        if (cafeId == null) return List.of();
        return cafeImageRepository.findAllByCafe_Id(cafeId);
    }

    /**
     * 카페 이미지 저장
     * - 파일은 ${app.upload.dir}/uploads/cafes/{cafeId}/ 에 저장
     * - DB에는 /uploads/cafes/{cafeId}/{filename} 형태의 URL 저장
     */
    @Transactional
    public List<CafeImage> saveCafeImages(Long cafeId, List<MultipartFile> files) throws IOException {
        if (cafeId == null) throw new IllegalArgumentException("cafeId가 필요합니다.");
        if (files == null || files.isEmpty()) return List.of();

        Cafe cafe = cafeListRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페가 존재하지 않습니다: " + cafeId));

        Path dir = physicalRoot().resolve(String.valueOf(cafeId));
        Files.createDirectories(dir); // 디렉터리 보장

        List<CafeImage> saved = new ArrayList<>();

        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;

            String original = Optional.ofNullable(f.getOriginalFilename()).orElse("");
            String ext = StringUtils.getFilenameExtension(original);
            String filename = UUID.randomUUID().toString() + (ext != null && !ext.isBlank() ? "." + ext.toLowerCase() : "");

            Path dest = dir.resolve(filename);
            Files.createDirectories(dest.getParent()); // 안전빵
            f.transferTo(dest.toFile());               // 실제 저장

            String url = "/uploads/cafes/" + cafeId + "/" + filename; // 정적 서빙 URL

            CafeImage entity = CafeImage.builder()
                    .cafe(cafe)
                    .imgUrl(url)
                    .build();

            saved.add(cafeImageRepository.save(entity));
        }

        return saved;
    }
}
