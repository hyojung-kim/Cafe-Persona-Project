package com.team.cafe.repository;

import com.team.cafe.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ReviewImageRepository
 * - ReviewImage 엔티티를 DB와 연결해 CRUD 및 추가 쿼리 메서드를 제공하는 Repository
 * - JpaRepository<ReviewImage, Long> 상속 → save, findById, findAll, delete 등 기본 메서드 자동 제공
 */
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    /**
     * 특정 리뷰(reviewId)에 속한 이미지들을 정렬 순서(sortOrder) 기준 오름차순으로 조회
     *
     * 메서드 이름 규칙:
     * - findByReview_Id → Review 엔티티의 id 컬럼을 조건으로 검색
     * - OrderBySortOrderAsc → sortOrder 컬럼 기준 오름차순 정렬
     *
     * SQL로 표현하면:
     * SELECT * FROM review_image
     * WHERE review_id = ?
     * ORDER BY sort_order ASC;
     */
    List<ReviewImage> findByReview_IdOrderBySortOrderAsc(Long reviewId);

    /**
     * 특정 리뷰에 속한 이미지 개수 조회
     *
     * 메서드 이름 규칙:
     * - countByReview_Id → Review의 id를 조건으로 카운트
     *
     * SQL로 표현하면:
     * SELECT COUNT(*) FROM review_image
     * WHERE review_id = ?;
     */
    long countByReview_Id(Long reviewId);
}
