package com.team.cafe.review;

import com.team.cafe.list.Cafe;
import com.team.cafe.user.sjhy.SiteUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.util.*;

/**
 * 리뷰 엔티티.
 * - BaseEntity 감사 필드 상속 (createdAt/updatedAt/createdBy/updatedBy)
 * - 병합 시 충돌을 줄이기 위해 컬럼/인덱스/제약을 명확하게 지정
 */
@Entity
@Table(
        name = "reviews",
        indexes = {
                @Index(name = "idx_review_cafe_id_created_at", columnList = "cafe_id, created_at"),
                @Index(name = "idx_review_author_id_created_at", columnList = "author_id, created_at")
        }
)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어느 카페의 리뷰인지 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cafe_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_cafe"))
    private Cafe cafe;

    /** 작성자 (SiteUser 엔티티 존재를 가정) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_author"))
    private SiteUser author;

    /** 별점(1.0 ~ 5.0) */
    @DecimalMin(value = "1.0", message = "별점은 최소 1.0 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "별점은 최대 5.0 이하여야 합니다.")
    @Column(name = "rating", nullable = false)
    private double rating;

    /** 내용(최소 50자 권장) */
    @Size(min = 50, message = "리뷰 내용은 50자 이상이어야 합니다.")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 조회수 */
    @Column(name = "view_count", nullable = false)
    private long viewCount = 0L;

    /** 좋아요 수 (작성자 본인은 불가 – 비즈니스 룰은 서비스에서 보장) */
    @Column(name = "like_count", nullable = false)
    private long likeCount = 0L;

    /** 노출/활성 여부 */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * 리뷰 이미지 컬렉션(정렬 보장)
     * - ReviewImage.sortOrder 기준 오름차순
     * - orphanRemoval=true 로 이미지 제거 시 DB에서도 삭제
     */
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ReviewImage> images = new ArrayList<>();

    // ---- 기본 생성자 ----
    public Review() { }   // protected → public

    // ---- 편의 생성자 ----
    public Review(Cafe cafe, SiteUser author, double rating, String content) {
        this.cafe = Objects.requireNonNull(cafe, "cafe는 null일 수 없습니다.");
        this.author = Objects.requireNonNull(author, "author는 null일 수 없습니다.");
        this.rating = rating;
        this.content = Objects.requireNonNull(content, "content는 null일 수 없습니다.");
    }

    // ---- Getter/Setter ----
    public Long getId() { return id; }

    public Cafe getCafe() { return cafe; }
    public void setCafe(Cafe cafe) { this.cafe = cafe; }

    public SiteUser getAuthor() { return author; }
    public void setAuthor(SiteUser author) { this.author = author; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getViewCount() { return viewCount; }
    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = Math.max(0, likeCount); }  // ✅ 추가됨

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<ReviewImage> getImages() { return images; }

    // ---- 도메인 보조 메서드 ----
    /** 리뷰 열람 시 호출 (동시성은 서비스/리포지토리 계층에서 처리) */
    public void increaseViewCount() { this.viewCount += 1; }

    /** 좋아요 추가 */
    public void addLike() { this.likeCount += 1; }

    /** 좋아요 취소(하한 0) */
    public void removeLike() { this.likeCount = Math.max(0, this.likeCount - 1); }

    /**
     * 이미지 추가(편의 메서드)
     * - 기본적으로 다음 순번(sortOrder = 현재 크기)으로 삽입
     * - 리뷰당 최대 5장 가드(초과 시 IllegalStateException) — 서비스에서 사전 검증 권장
     */
    public void addImage(ReviewImage image) {
        Objects.requireNonNull(image, "image는 null일 수 없습니다.");
        if (this.images.size() >= 5) {
            throw new IllegalStateException("리뷰에는 최대 5장의 이미지만 첨부할 수 있습니다.");
        }
        image.setReview(this);
        if (image.getSortOrder() < 0) {
            image.setSortOrder(this.images.size());
        }
        this.images.add(image);
        normalizeSortOrders();
    }

    /**
     * 이미지 제거(편의 메서드)
     * - 제거 후 연속 정렬 유지
     */
    public void removeImage(ReviewImage image) {
        if (image == null) return;
        this.images.remove(image);
        image.setReview(null);
        normalizeSortOrders();
    }

    /**
     * 정렬값을 0..N-1 연속으로 보정
     * - @OrderBy 정렬 일관성을 보장하기 위해 내부적으로 호출
     */
    public void normalizeSortOrders() {
        for (int i = 0; i < this.images.size(); i++) {
            this.images.get(i).setSortOrder(i);
        }
    }
}
