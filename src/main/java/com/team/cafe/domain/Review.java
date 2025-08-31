package com.team.cafe.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Review 엔티티
 * - “카페에 달린 리뷰” 한 건을 표현하는 도메인 모델 + DB 테이블 매핑 클래스
 * - JPA/Hibernate가 이 클래스를 보고 테이블 스키마와 CRUD를 자동으로 처리
 */
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder // 빌더 패턴으로 가독성 있게 생성 가능 (필드 많을 때 유용)
@Table(indexes = {
        // (cafe_id, createdAt DESC) 복합 인덱스: 특정 카페의 최신 리뷰를 빠르게 페이징/정렬 조회하려고 설정
        // ⚠ createdAt 컬럼명은 네이밍 전략에 따라 created_at로 생성될 수도 있으니, 전략/컬럼명 일치 확인 필요
        @Index(name="idx_review_cafe_created", columnList = "cafe_id, createdAt DESC")
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MariaDB/MySQL의 AUTO_INCREMENT 사용
    private Long id;

    // (리뷰↔카페) N:1 관계. “어떤 카페의 리뷰인지”
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    // optional=false → 반드시 카페가 있어야 저장 가능(Not null FK)
    // LAZY → 실제로 필요할 때만 카페를 DB에서 가져옴(불필요한 조인/쿼리 방지)
    private Cafe cafe;

    // (리뷰↔작성자) N:1 관계. “누가 썼는지”
    @ManyToOne(optional = false, fetch=FetchType.LAZY)
    private SiteUser author;

    // 별점(0.0 ~ 5.0, 0.5 단위는 서비스/검증에서 체크 권장)
    @Column(nullable = false)
    private Double rating;

    // 리뷰 본문(선택 입력 가능)
    @Column
    private String content;

    // 조회수(기본 0으로 시작). @Builder.Default 없으면 빌더로 생성 시 null이 들어갈 수 있어 방지용
    @Builder.Default
    private Long viewCount = 0L;

    // 생성/수정 시각(감사(Audit) 정보). @PrePersist/@PreUpdate로 자동 세팅 권장
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // 리뷰 상태(예: ACTIVE, BLOCKED, DELETED 등). 문자열로 저장해서 가독성/이식성 확보
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.ACTIVE;

    // (리뷰↔리뷰이미지) 1:N 관계. 리뷰가 삭제되면 이미지도 같이 삭제(cascade + orphanRemoval)
    @OneToMany(mappedBy = "review", cascade=CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();

    /* ---------------------------------------------------------
       ⬇ 권장: 생성/수정 시각을 자동으로 채우는 라이프사이클 콜백
       (스키마나 팀 컨벤션에 맞게 @Column(nullable=false)도 고려)
       --------------------------------------------------------- */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = this.createdAt;
        if (this.viewCount == null) this.viewCount = 0L; // 방어코드(빌더 사용 시 안전망)
        if (this.status == null) this.status = ReviewStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }
}
