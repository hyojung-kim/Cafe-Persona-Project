package com.team.cafe.service;


import com.team.cafe.cafe.CafeRepository;
import com.team.cafe.domain.*;
import com.team.cafe.repository.ReviewImageRepository;
import com.team.cafe.repository.ReviewLikeRepository;
import com.team.cafe.repository.ReviewReportRepository;
import com.team.cafe.repository.ReviewRepository;
import com.team.cafe.review.ReviewStorage;
import com.team.cafe.user.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final CafeRepository cafeRepository;
    private final SiteUserRepository siteUserRepository;
    private final ReviewStorage reviewStorage; // 파일 저장(아래 클래스)

    public Review create(Long cafeId, Long authorId, double ratingScore, String content, List<MultipartFile> images) {
        if (content == null || content.trim().length() < 50) {
            throw new IllegalArgumentException("리뷰는 50자 이상이어야 합니다.");
        }
        if (images != null && images.size() > 5) {
            throw new IllegalArgumentException("이미지는 최대 5장까지 업로드할 수 있습니다.");
        }

        Cafe cafe = cafeRepository.findById(cafeId).orElseThrow();
        SiteUser author = siteUserRepository.findById(authorId).orElseThrow();

        Review review = Review.builder()
                .author(author)
                .cafe(cafe)
                .rating(Rating.of(ratingScore))
                .content(content.trim())
                .status(ReviewStatus.ACTIVE)
                .build();

        reviewRepository.save(review);

        // 이미지 저장
        if (images != null) {
            int idx = 0;
            for (MultipartFile mf : images) {
                String path = reviewStorage.store(mf);
                ReviewImage ri = ReviewImage.builder()
                        .review(review)
                        .imagePath(path)
                        .sortOrder(idx++)
                        .build();
                reviewImageRepository.save(ri);
            }
        }

        recalcCafeStats(cafe.getId());
        return review;
    }

    public Review update(Long reviewId, Long authorId, double ratingScore, String content, List<MultipartFile> newImages) {
        Review r = reviewRepository.findById(reviewId).orElseThrow();
        if (!r.getAuthor().getId().equals(authorId)) {
            throw new SecurityException("작성자만 수정할 수 있습니다.");
        }
        if (content == null || content.trim().length() < 50) {
            throw new IllegalArgumentException("리뷰는 50자 이상이어야 합니다.");
        }
        r.setContent(content.trim());
        r.setRating(Rating.of(ratingScore));
        // 이미지 교체 로직이 필요하면 기존 삭제 후 재저장 구현 가능(여긴 스켈레톤)

        recalcCafeStats(r.getCafe().getId());
        return r;
    }

    public void requestDeletion(Long reviewId, Long authorId) {
        Review r = reviewRepository.findById(reviewId).orElseThrow();
        if (!r.getAuthor().getId().equals(authorId)) {
            throw new SecurityException("작성자만 삭제요청할 수 있습니다.");
        }
        r.setStatus(ReviewStatus.DELETION_REQUESTED);
    }

    public void increaseView(Long reviewId) {
        Review r = reviewRepository.findById(reviewId).orElseThrow();
        r.increaseView();
    }

    /** 좋아요 토글: 본인 리뷰면 예외, 이미 누른 경우 취소 */
    public int toggleLike(Long reviewId, Long userId) {
        Review r = reviewRepository.findById(reviewId).orElseThrow();
        if (!r.canBeLikedBy(siteUserRepository.findById(userId).orElse(null))) {
            throw new IllegalArgumentException("본인 리뷰에는 좋아요를 누를 수 없습니다.");
        }
        if (reviewLikeRepository.existsByReview_IdAndUser_Id(reviewId, userId)) {
            reviewLikeRepository.deleteByReview_IdAndUser_Id(reviewId, userId);
            r.decreaseLike();
        } else {
            ReviewLike like = ReviewLike.builder()
                    .review(r)
                    .user(siteUserRepository.findById(userId).orElseThrow())
                    .build();
            reviewLikeRepository.save(like);
            r.increaseLike();
        }
        return r.getLikeCount();
    }

    public void report(Long reviewId, Long reporterId, String reason) {
        if (reviewReportRepository.existsByReview_IdAndReporter_Id(reviewId, reporterId)) return;
        Review r = reviewRepository.findById(reviewId).orElseThrow();
        ReviewReport report = ReviewReport.builder()
                .review(r)
                .reporter(siteUserRepository.findById(reporterId).orElseThrow())
                .reason(reason == null ? "기타" : reason)
                .build();
        reviewReportRepository.save(report);
        r.setStatus(ReviewStatus.REPORTED);
    }

    @Transactional(readOnly = true)
    public Page<Review> listByCafe(Long cafeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return reviewRepository.findByCafe_IdAndStatus(cafeId, ReviewStatus.ACTIVE, pageable);
    }

    @Transactional(readOnly = true)
    public Review getDetail(Long id) { // 컨트롤러에서 조회수 증가 여부 판단
        return reviewRepository.findById(id).orElseThrow();
    }

    /** 카페별 평균/개수 캐시 컬럼 갱신 */
    private void recalcCafeStats(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId).orElseThrow();
        double avgHalf = reviewRepository.avgHalfStarsByCafeId(cafeId); // 0~10
        long count = reviewRepository.countActiveByCafeId(cafeId);
        cafe.setAvgRating(Math.round((avgHalf / 2.0) * 10.0) / 10.0); // 소수1
        cafe.setReviewCount((int) count);
    }
}
