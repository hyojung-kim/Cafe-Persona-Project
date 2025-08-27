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

@Component
@RequiredArgsConstructor
public class ReviewStorage {

    @Value("${app.upload-dir:file:./uploads}")
    private String uploadRoot; // 예: file:./uploads

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            String filename = System.currentTimeMillis() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
            LocalDate d = LocalDate.now();
            Path root = Paths.get(URI.create(uploadRoot));
            Path dir = root.resolve(d.getYear() + "/" + String.format("%02d", d.getMonthValue()) + "/" + String.format("%02d", d.getDayOfMonth()));
            Files.createDirectories(dir);
            Path dest = dir.resolve(filename);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            // 반환 경로는 정적 리소스 매핑(/uploads/**)과 일치하게
            return "/uploads/" + d.getYear() + "/" + String.format("%02d", d.getMonthValue()) + "/" + String.format("%02d", d.getDayOfMonth()) + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
