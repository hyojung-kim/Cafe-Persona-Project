package com.team.cafe.review;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    /**
     * 특정 리뷰에 속한 이미지들을 sortOrder 오름차순으로 조회.
     */
    List<ReviewImage> findByReview_IdOrderBySortOrderAsc(Long reviewId);

    /**
     * (선택) 리뷰별 이미지 개수
     */
    long countByReview_Id(Long reviewId);

    /**
     * (선택) 특정 리뷰의 모든 이미지를 일괄 삭제
     */
    void deleteByReview_Id(Long reviewId);
}
