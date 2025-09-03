package com.team.cafe.review;

import jakarta.persistence.*;
import lombok.*;
import com.team.cafe.user.sjhy.SiteUser;

/**
 * ReviewLike 엔티티
 * - "리뷰 좋아요"를 누른 사용자와 리뷰의 관계를 저장
 * - 예: 사용자(user_id=5)가 리뷰(review_id=10)에 좋아요를 눌렀다면
 *   ReviewLike 테이블에 한 행이 생김
 */
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"review_id", "user_id"})
        // 동일한 사용자가 같은 리뷰에 중복 좋아요를 누르지 못하게 보장하는 제약 조건
        // (DB 레벨에서 UNIQUE KEY 설정)
)
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // 기본키(PK). Auto Increment로 자동 생성
    private Long id;

    // (좋아요 ↔ 리뷰) N:1 관계
    // 여러 사용자가 같은 리뷰에 좋아요를 누를 수 있음
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Review review;

    // (좋아요 ↔ 사용자) N:1 관계
    // 한 사용자가 여러 리뷰에 좋아요 가능
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    // FK 컬럼명을 "user_id"로 지정 (기본 규칙은 site_user_id일 수 있는데, 명시적으로 지정)
    private SiteUser user;
}
