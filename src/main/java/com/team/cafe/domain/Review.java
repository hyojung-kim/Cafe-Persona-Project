package com.team.cafe.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Review 엔티티
 * - 카페에 달린 리뷰 한 건을 표현
 * - 템플릿에서 rv.author.username 접근 시 LazyInitialization 방지를 위해 author만 EAGER 로딩
 */
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(indexes = {
        // 특정 카페의 최신 리뷰를 빠르게 페이징/정렬하기 위한 복합 인덱스
        @Index(name = "idx_review_cafe_created", columnList = "cafe_id, createdAt DESC")
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** (리뷰↔카페) N:1 — 어떤 카페의 리뷰인지 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Cafe cafe;

    /**
     * (리뷰↔작성자) N:1 — 누가 썼는지
     * - 템플릿에서 rv.author.username를 바로 쓰기 위해 EAGER로 전환 (Lazy 폭탄 방지)
     * - 회원만 작성 가능하므로 optional=false
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private SiteUser author;

    /** 별점: 0.0 ~ 5.0 (0.5 단위는 서비스/검증 레이어에서 별도 체크 권장) */
    @DecimalMin("0.0")
    @DecimalMax("5.0")
    @Column(nullable = false)
    private Double rating;

    /** 리뷰 본문: 최소 50자 요구사항에 맞춘 Bean Validation (DB 제약은 nullable로만) */
    @Size(min = 50, message = "리뷰 내용은 최소 50자 이상이어야 합니다.")
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 조회수 (기본 0) */
    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;

    /** 생성/수정 시각 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    /** 리뷰 상태 (예: ACTIVE, BLOCKED, DELETED 등) */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private ReviewStatus status = ReviewStatus.ACTIVE;

    /** (리뷰↔이미지) 1:N — 리뷰 삭제 시 이미지도 함께 삭제 */
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();

    /** (리뷰↔좋아요) 1:N — 리뷰 삭제 시 좋아요도 함께 삭제 */
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewLike> likes = new ArrayList<>();

    /* ===== 라이프사이클 콜백: 생성/수정 시각 자동 세팅 + 기본값 방어 ===== */
    @PrePersist
    protected void onCreate() {
        final LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.modifiedAt = now;
        if (this.viewCount == null) this.viewCount = 0L;
        if (this.status == null) this.status = ReviewStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }

    /* ===== 편의 메서드 ===== */
    public void addImage(ReviewImage image) {
        images.add(image);
        image.setReview(this);
    }

    public void removeImage(ReviewImage image) {
        images.remove(image);
        image.setReview(null);
    }
}
