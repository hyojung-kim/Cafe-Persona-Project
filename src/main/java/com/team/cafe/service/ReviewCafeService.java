package com.team.cafe.service;

import com.team.cafe.domain.Cafe;
import com.team.cafe.domain.Review;
import com.team.cafe.domain.ReviewImage;
import com.team.cafe.domain.SiteUser;
import com.team.cafe.dto.ReviewCreateRequest;
import com.team.cafe.repository.CafeRepository;
import com.team.cafe.repository.ReviewImageRepository;
import com.team.cafe.repository.ReviewLikeRepository;
import com.team.cafe.repository.ReviewRepository;
import com.team.cafe.repository.SiteUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewCafeService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final CafeRepository cafeRepository;
    private final SiteUserRepository siteUserRepository;
    private final FileStorageService fileStorageService;

    private static final double MIN_RATING = 0.0;
    private static final double MAX_RATING = 5.0;
    private static final double STEP = 0.5; // 0.5 단위 검증

    private void validateRating(Double rating) {
        if (rating == null || rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException("별점은 0.0 ~ 5.0 범위여야 합니다.");
        }
        double scaled = rating / STEP;
        if (Math.abs(scaled - Math.round(scaled)) > 1e-9) {
            throw new IllegalArgumentException("별점은 0.5 단위여야 합니다.");
        }
    }

    /** 리뷰 생성(회원만) + 이미지 업로드(최대 5장) */
    @Transactional
    public Review createReview(String username, ReviewCreateRequest req, List<MultipartFile> files) throws IOException {
        // 1) 입력 검증
        validateRating(req.rating());
        if (req.content() == null || req.content().trim().length() < 50) {
            throw new IllegalArgumentException("리뷰는 50자 이상이어야 합니다.");
        }

        // 2) 작성자/카페 로딩
        SiteUser author = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Cafe cafe = cafeRepository.findById(req.cafeId())
                .orElseThrow(() -> new IllegalArgumentException("카페 없음"));

        // 3) 리뷰 저장
        Review review = Review.builder()
                .cafe(cafe)
                .author(author)
                .content(req.content().trim())
                .rating(req.rating())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
        reviewRepository.save(review);

        // 4) 이미지 업로드(선택) — 최대 5장
        if (files != null && !files.isEmpty()) {
            long nonEmpty = files.stream().filter(f -> f != null && !f.isEmpty()).count();
            if (nonEmpty > 5) {
                throw new IllegalArgumentException("이미지는 최대 5장까지 업로드할 수 있습니다.");
            }

            // 파일 저장 서비스가 물리 파일을 저장하고 /uploads/... 경로를 반환
            List<String> urlPaths = fileStorageService.storeReviewImages(review.getId(), files);

            int p = 0;
            for (MultipartFile f : files) {
                if (f == null || f.isEmpty()) continue;
                ReviewImage img = ReviewImage.builder()
                        .review(review)
                        .urlPath(urlPaths.get(p++))             // 저장된 요청 경로
                        .originalFilename(f.getOriginalFilename())
                        .sizeBytes(f.getSize())
                        .build();
                reviewImageRepository.save(img);
                review.getImages().add(img); // 양방향 컬렉션 유지(선택)
            }
        }

        // 5) 결과 반환
        return review;
    }
}
