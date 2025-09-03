package com.team.cafe.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public List<String> storeReviewImages(Long reviewId, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) return List.of();
        Path base = Paths.get(uploadDir, "reviews", reviewId.toString());
        Files.createDirectories(base);

        List<String> urlPaths = new ArrayList<>();
        for (MultipartFile f : files) {
            if (f.isEmpty()) continue;
            String ext = Optional.ofNullable(StringUtils.getFilenameExtension(f.getOriginalFilename()))
                    .map(String::toLowerCase).orElse("bin");
            if (!List.of("jpg","jpeg","png","webp","gif").contains(ext)) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
            }
            String filename = UUID.randomUUID() + "." + ext;
            Path target = base.resolve(filename);
            Files.copy(f.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            urlPaths.add("/uploads/reviews/" + reviewId + "/" + filename);
        }
        return urlPaths;
    }
}
