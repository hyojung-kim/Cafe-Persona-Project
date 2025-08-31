package com.team.cafe.service;

import com.team.cafe.domain.Cafe;
import com.team.cafe.domain.Review;
import com.team.cafe.domain.ReviewImage;
import com.team.cafe.domain.ReviewLike;
import com.team.cafe.domain.SiteUser;
import com.team.cafe.dto.ReviewCreateRequest;
import com.team.cafe.repository.CafeRepository;
import com.team.cafe.repository.ReviewImageRepository;
import com.team.cafe.repository.ReviewLikeRepository;
import com.team.cafe.repository.ReviewRepository;
import com.team.cafe.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final CafeRepository cafeRepository;
    private final SiteUserRepository siteUserRepository;
    private final FileStorageService fileStorageService; // ✅ 업로드 서비스(우리가 제공했던 것)

    private static final double MIN_RATING = 0.0;
    private static final double MAX_RATING = 5.0;
    private static final double STEP = 0.5;

    /** ⭐ 별점 검증: 0.0~5.0 & 0.5 단위 */
    private void validateRating(Double rating) {
        if (rating == null || rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException("별점은 0.0 ~ 5.0 범위여야 합니다.");
        }
        double scaled = rating / STEP;
        if (Math.abs(scaled - Math.round(scaled)) > 1e-9) {
            throw new IllegalArgumentException("별점은 0.5 단위여야 합니다.");
        }
    }

    /** 카페별 리뷰 페이징 */
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByCafe(Long cafeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        // ✅ 메서드명 오타 수정: CreatedAt
        return reviewRepository.findByCafe_IdOrderByCreatedAtDesc(cafeId, pageable);
    }

    /** 평균 별점 */
    @Transactional(readOnly = true)
    public Double getAverageRating(Long cafeId) {
        return reviewRepository.averageRating(cafeId);
    }

    /**
     * 이 방법도 참고만 해 두자!
     @Transactional
     public long likeReview(String username, Long reviewId) {
     // 기존 toggleLike 로직 재사용
     return toggleLike(username, reviewId);
     }
     **/

    /** 리뷰 생성(회원만) */
    public Review createReview(String username, ReviewCreateRequest req, List<MultipartFile> images) throws IOException {
        // 본문/별점 검증
        if (req.content() == null || req.content().trim().length() < 50) {
            throw new IllegalArgumentException("리뷰는 50자 이상이어야 합니다.");
        }
        validateRating(req.rating());

        // 작성자/카페 로딩
        SiteUser author = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Cafe cafe = cafeRepository.findById(req.cafeId())
                .orElseThrow(() -> new IllegalArgumentException("카페 없음"));

        // 리뷰 저장
        Review review = Review.builder()
                .author(author)
                .cafe(cafe)
                .rating(req.rating())                      // ✅ Double로 저장
                .content(req.content().trim())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
        reviewRepository.save(review);

        // 이미지 저장 (최대 5장)
        if (images != null && !images.isEmpty()) {
            long count = images.stream().filter(f -> !f.isEmpty()).count();
            if (count > 5) {
                throw new IllegalArgumentException("이미지는 최대 5장까지 업로드할 수 있습니다.");
            }

            // ✅ 우리 FileStorageService는 URL 경로 리스트를 반환(예: /uploads/reviews/{id}/{uuid}.jpg)
            List<String> paths = fileStorageService.storeReviewImages(review.getId(), images);
            int i = 0;
            for (MultipartFile mf : images) {
                if (mf.isEmpty()) continue;
                ReviewImage img = ReviewImage.builder()
                        .review(review)
                        .urlPath(paths.get(i))               // ✅ 엔티티 필드명: urlPath
                        .originalFilename(mf.getOriginalFilename())
                        .sizeBytes(mf.getSize())
                        .build();
                reviewImageRepository.save(img);
                review.getImages().add(img);
                i++;
            }
        }

        return review;
    }

    /** 리뷰 수정(작성자만) */
    public Review updateReview(String username, Long reviewId, String content, Double rating) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 없음"));

        if (!review.getAuthor().getUsername().equals(username)) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }
        if (content == null || content.trim().length() < 50) {
            throw new IllegalArgumentException("리뷰는 50자 이상이어야 합니다.");
        }
        validateRating(rating);

        review.setContent(content.trim());
        review.setRating(rating);                            // ✅ Double로 유지
        review.setModifiedAt(LocalDateTime.now());
        return review;
    }

    /** 리뷰 삭제(작성자만) */
    public void deleteReview(String username, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 없음"));
        if (!review.getAuthor().getUsername().equals(username)) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }
        reviewRepository.delete(review);
    }

    /** 리뷰 상세 + 조회수 증가 */
    public Review getAndIncreaseView(Long reviewId) {
        reviewRepository.incrementView(reviewId);
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 없음"));
    }

    /** 좋아요 토글(작성자 본인 금지) */
    public long toggleLike(String username, Long reviewId) {
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 없음"));

        if (review.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("본인 리뷰에는 좋아요를 누를 수 없습니다.");
        }

        // ✅ 엔티티 기반 메서드 사용(이미 리포지토리에 구현돼 있다고 했던 것들)
        if (reviewLikeRepository.existsByReviewAndUser(review, user)) {
            reviewLikeRepository.deleteByReviewAndUser(review, user);
        } else {
            reviewLikeRepository.save(ReviewLike.builder()
                    .review(review)
                    .user(user)
                    .build());
        }
        return reviewLikeRepository.countByReview(review);
    }

    /** 좋아요 수 조회 */
    @Transactional(readOnly = true)
    public long getLikeCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 없음"));
        return reviewLikeRepository.countByReview(review);
    }
}
