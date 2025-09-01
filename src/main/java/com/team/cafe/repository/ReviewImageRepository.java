package com.team.cafe.repository;

import com.team.cafe.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ReviewImageRepository
 * - ReviewImage 엔티티 CRUD + 배치 로딩 쿼리 제공
 */
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    /**
     * 단일 리뷰의 이미지들을 정렬 순서로 조회
     * - 엔티티에 sortOrder 필드가 있을 때 사용
     */
    List<ReviewImage> findByReview_IdOrderBySortOrderAsc(Long reviewId);

    /**
     * (대안) 단일 리뷰의 이미지들을 ID 기준으로 조회
     * - 엔티티에 sortOrder 필드가 없을 때 사용
     */
    List<ReviewImage> findByReview_IdOrderByIdAsc(Long reviewId);

    /**
     * 여러 리뷰 ID에 대한 이미지를 한 번에 조회 (권장: 페이지 화면에서 사용)
     * - 리뷰 ID, 정렬 순서 기준으로 정렬해 가져옴
     * - 엔티티에 sortOrder가 있을 때
     */
    @Query("""
           select ri
           from ReviewImage ri
           where ri.review.id in :reviewIds
           order by ri.review.id asc, ri.sortOrder asc
           """)
    List<ReviewImage> findAllByReviewIdsOrderByReviewIdAndSortOrder(
            @Param("reviewIds") List<Long> reviewIds
    );

    /**
     * (대안) sortOrder가 없다면 ID 기준 정렬 버전 사용
     */
    @Query("""
           select ri
           from ReviewImage ri
           where ri.review.id in :reviewIds
           order by ri.review.id asc, ri.id asc
           """)
    List<ReviewImage> findAllByReviewIdsOrderByReviewIdAndId(
            @Param("reviewIds") List<Long> reviewIds
    );

    /**
     * 여러 리뷰에 대한 이미지 개수를 한 번에 조회 (템플릿에서 조건부 노출 시 유용)
     */
    @Query("""
           select ri.review.id as reviewId, count(ri) as cnt
           from ReviewImage ri
           where ri.review.id in :reviewIds
           group by ri.review.id
           """)
    List<ImageCountPerReview> countByReviewIds(@Param("reviewIds") List<Long> reviewIds);

    /**
     * Projection for countByReviewIds
     */
    interface ImageCountPerReview {
        Long getReviewId();
        long getCnt();
    }

    /** 단일 리뷰의 이미지 수 */
    long countByReview_Id(Long reviewId);
}

