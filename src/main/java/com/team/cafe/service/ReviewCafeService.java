package com.team.cafe.service;

import com.team.cafe.repository.CafeRepository;
import com.team.cafe.domain.Cafe;
import com.team.cafe.domain.Review;
import com.team.cafe.domain.SiteUser;
import com.team.cafe.dto.ReviewCreateRequest;
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

    private void validateRating(Double rating) {
        if (rating < minRating || rating > maxRating) {
            throw new IllegalArgumentException("별점은" + step + "단위여야 합니다.");
        }
    }
    @Transactional
    public Review createReview(String username, ReviewCreateRequest req, List<MultipartFile> files) throws IOException {
        validateRating(req.rating());

        SiteUser author = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Cafe cafe = cafeRepository.findById(req.cafeId())
                .orElseThrow(() -> new IllegalArgumentException("카페 없음"));

        Review Review = Review.builder
                .Cafe(cafe)
                .author(author)
                .content(req.content())
                .rating(req.rating())
                .createAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
        reviewRepository.save(review);

        if (files != null && !files.isEmpty()) {
            long count = files.stream().filter(f -> !f.isEmpty()).count();
        }

    }
}
