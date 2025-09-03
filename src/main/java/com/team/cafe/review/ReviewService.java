package com.team.cafe.review;

import com.team.cafe.list.Cafe;
import com.team.cafe.list.CafeListRepository;
import com.team.cafe.user.sjhy.SiteUser;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ReviewService {

    private final CafeListRepository cafeListRepository;
    private final ReviewRepository reviewRepository;

    public ReviewService(CafeListRepository cafeListRepository,
                         ReviewRepository reviewRepository) {
        this.cafeListRepository = cafeListRepository;
        this.reviewRepository = reviewRepository;
    }

    // ========================= 조회 =========================

    /** 카페별 활성 리뷰 페이징 */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Review> getActiveReviewsByCafe(Long cafeId, Pageable pageable) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        return reviewRepository.findByCafe_IdAndActiveTrue(cafeId, pageable);
    }

    /** 카페별 활성 리뷰 페이징 + author/images 동시 로딩(N+1 방지) */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Review> getActiveReviewsByCafeWithAuthorImages(Long cafeId, Pageable pageable) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        return reviewRepository.findByCafe_IdAndActiveTrueFetchAuthorImages(cafeId, pageable);
    }

    /** 작성자별 활성 리뷰 페이징 */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Review> getActiveReviewsByAuthor(Long authorId, Pageable pageable) {
        Objects.requireNonNull(authorId, "authorId is required");
        return reviewRepository.findByAuthor_IdAndActiveTrue(authorId, pageable);
    }

    // ========================= 생성 =========================

    /**
     * 리뷰 생성 + 이미지 최대 5장 저장
     * - 양방향 연관관계 일관성 보장: review.addImage(...) 사용
     * - 부모(review)만 save (cascade = ALL, orphanRemoval = true)
     */
    public Review createReview(Long cafeId,
                               SiteUser author,
                               Double rating,
                               String content,
                               List<String> imageUrls) {
        Objects.requireNonNull(author, "author is required");

        Cafe cafe = cafeListRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다. id=" + cafeId));

        if (rating == null || rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("별점은 1.0 ~ 5.0 사이여야 합니다.");
        }
        if (content == null || content.trim().length() < 50) {
            throw new IllegalArgumentException("리뷰 내용은 50자 이상이어야 합니다.");
        }
        if (imageUrls != null && imageUrls.size() > 5) {
            throw new IllegalArgumentException("이미지는 최대 5장까지만 가능합니다.");
        }

        // 부모 엔티티 생성
        Review review = new Review();
        review.setCafe(cafe);
        review.setAuthor(author);
        review.setRating(rating);
        review.setContent(content.trim());

        // 자식(이미지) 추가: 도메인 메서드로 컬렉션 & FK 동시 설정
        if (imageUrls != null) {
            int order = 0;
            for (String url : imageUrls) {
                if (url == null || url.isBlank()) continue;
                ReviewImage img = new ReviewImage(); // 기본 생성자 public이어야 함
                img.setImageUrl(url.trim());
                img.setSortOrder(order++);
                review.addImage(img); // img.setReview(this) + 컬렉션 add + sort 보정
            }
        }

        // 부모만 저장하면 cascade로 자식도 저장
        return reviewRepository.save(review);
    }

    // ========================= 수정 =========================

    /**
     * 리뷰 수정(작성자 또는 관리자)
     * - 내용/별점/이미지 전체 교체 (orphanRemoval 작동)
     */
    public Review updateReview(Long reviewId,
                               SiteUser editor,
                               Double newRating,
                               String newContent,
                               List<String> newImageUrls) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        boolean isAuthor = review.getAuthor() != null && review.getAuthor().getId().equals(editor.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(editor.getRole())
                || "ROLE_ADMIN".equalsIgnoreCase(editor.getRole());
        if (!isAuthor && !isAdmin) {
            throw new SecurityException("작성자 또는 관리자만 수정할 수 있습니다.");
        }

        if (newRating == null || newRating < 1.0 || newRating > 5.0) {
            throw new IllegalArgumentException("별점은 1.0 ~ 5.0 사이여야 합니다.");
        }
        if (newContent == null || newContent.trim().length() < 50) {
            throw new IllegalArgumentException("리뷰 내용은 50자 이상이어야 합니다.");
        }
        if (newImageUrls != null && newImageUrls.size() > 5) {
            throw new IllegalArgumentException("이미지는 최대 5장까지만 가능합니다.");
        }

        // 본문/별점 교체
        review.setRating(newRating);
        review.setContent(newContent.trim());

        // 기존 이미지 전량 제거 (orphanRemoval=true)
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            List<ReviewImage> copy = new ArrayList<>(review.getImages());
            for (ReviewImage img : copy) {
                review.removeImage(img); // 컬렉션 제거 + img.review = null + sort 재정렬
            }
        }

        // 새 이미지 추가
        if (newImageUrls != null) {
            int order = 0;
            for (String url : newImageUrls) {
                if (url == null || url.isBlank()) continue;
                ReviewImage imgNew = new ReviewImage();
                imgNew.setImageUrl(url.trim());
                imgNew.setSortOrder(order++);
                review.addImage(imgNew);
            }
        }

        // 부모만 save (자식들은 cascade)
        return reviewRepository.save(review);
    }

    // ========================= 삭제 =========================

    /**
     * 리뷰 삭제(작성자 또는 관리자)
     * - Review 삭제 시, 자식 이미지들(orphanRemoval)도 함께 제거됨
     */
    public void deleteReview(Long reviewId, SiteUser requester) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        boolean isAuthor = review.getAuthor() != null && review.getAuthor().getId().equals(requester.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(requester.getRole())
                || "ROLE_ADMIN".equalsIgnoreCase(requester.getRole());
        if (!isAuthor && !isAdmin) {
            throw new SecurityException("작성자 또는 관리자만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }

    // ========================= 좋아요 / 조회수 =========================

    /** 좋아요 추가 */
    public void likeReview(Long reviewId, SiteUser liker) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        // 본인 리뷰 좋아요 금지
        if (review.getAuthor() != null && review.getAuthor().getId().equals(liker.getId())) {
            throw new IllegalArgumentException("자신의 리뷰에는 좋아요를 누를 수 없습니다.");
        }

        review.addLike();              // 엔티티 도메인 메서드
        reviewRepository.save(review);
    }

    /** 좋아요 취소(하한 0) */
    public void unlikeReview(Long reviewId, SiteUser liker) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        review.removeLike();
        reviewRepository.save(review);
    }

    /** 조회수 증가 */
    public void increaseViewCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));
        review.increaseViewCount();
        reviewRepository.save(review);
    }
}
