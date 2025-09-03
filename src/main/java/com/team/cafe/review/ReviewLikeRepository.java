package com.team.cafe.review;


import com.team.cafe.user.sjhy.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * ReviewLikeRepository
 * - ReviewLike 엔티티에 대한 CRUD 및 커스텀 쿼리 메서드를 제공
 * - "사용자가 특정 리뷰에 좋아요를 눌렀는지", "리뷰 좋아요 수는 몇 개인지" 등을 처리할 수 있음
 */
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    /**
     * 해당 리뷰에 대해 특정 사용자가 이미 좋아요를 눌렀는지 확인
     *
     * SQL 예시:
     * SELECT EXISTS (
     *   SELECT 1 FROM review_like
     *   WHERE review_id = ? AND user_id = ?
     * );
     */
    boolean existsByReviewAndUser(Review review, SiteUser user);

    /**
     * 특정 리뷰의 좋아요 수를 세기
     *
     * SQL 예시:
     * SELECT COUNT(*) FROM review_like WHERE review_id = ?;
     */
    long countByReview(Review review);

    /**
     * 특정 리뷰 + 특정 사용자 조합의 좋아요 엔티티를 찾기
     * - Optional로 반환 → 있으면 get(), 없으면 empty()
     *
     * SQL 예시:
     * SELECT * FROM review_like
     * WHERE review_id = ? AND user_id = ?;
     */
    Optional<ReviewLike> findByReviewAndUser(Review review, SiteUser user);

    /**
     * 특정 리뷰 + 특정 사용자 조합의 좋아요 엔티티를 삭제
     * - 좋아요 취소할 때 사용
     * - 반환값: 삭제된 행(row)의 개수
     *
     * SQL 예시:
     * DELETE FROM review_like
     * WHERE review_id = ? AND user_id = ?;
     */
    long deleteByReviewAndUser(Review review, SiteUser user);
}
