package com.team.cafe.review;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

/**
 * ReviewStorage
 * - 리뷰 작성 시 업로드된 이미지 파일을 서버 로컬 디렉토리에 저장하는 클래스
 * - 저장 후 접근 가능한 URL 경로(/uploads/...)를 반환
 */
@Component
@RequiredArgsConstructor
public class ReviewStorage {

    // application.properties 에서 설정값 주입
    // 예: app.upload-dir=file:./uploads
    @Value("${app.upload-dir:file:./uploads}")
    private String uploadRoot; // 저장소 루트 경로 (file: 접두사 필수)

    /**
     * 업로드된 파일을 저장하고 URL 경로 반환
     * @param file 업로드된 파일(MultipartFile)
     * @return 웹에서 접근 가능한 파일 경로 (예: /uploads/2025/09/01/12345_image.png)
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) return null; // 업로드 파일이 없으면 null 반환
        try {
            // 파일명 생성: 현재시간밀리초 + "_" + 원본파일명
            String filename = System.currentTimeMillis() + "_" + StringUtils.cleanPath(file.getOriginalFilename());

            // 오늘 날짜 기준으로 연/월/일 폴더 경로 생성
            LocalDate d = LocalDate.now();
            Path root = Paths.get(URI.create(uploadRoot)); // file:./uploads → ./uploads 폴더
            Path dir = root.resolve(
                    d.getYear() + "/" +
                            String.format("%02d", d.getMonthValue()) + "/" +
                            String.format("%02d", d.getDayOfMonth())
            );

            // 경로 없으면 생성 (중첩 디렉토리 포함)
            Files.createDirectories(dir);

            // 최종 목적지 경로
            Path dest = dir.resolve(filename);

            // 파일 저장 (이미 존재하면 덮어쓰기)
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            // 반환 경로는 WebMvcConfig 의 정적 리소스 매핑(/uploads/**)과 일치
            return "/uploads/" +
                    d.getYear() + "/" +
                    String.format("%02d", d.getMonthValue()) + "/" +
                    String.format("%02d", d.getDayOfMonth()) + "/" +
                    filename;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
