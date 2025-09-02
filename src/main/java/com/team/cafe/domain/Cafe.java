package com.team.cafe.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 카페 기본 정보 엔티티.
 * - BaseEntity 감사 필드 상속 (createdAt/updatedAt/createdBy/updatedBy)
 * - 다른 팀 프로젝트와 병합 시 충돌을 줄이기 위해 컬럼 네이밍/제약을 명확히 지정
 */
@Entity
@Table(
        name = "cafes",
        indexes = {
                @Index(name = "idx_cafe_name", columnList = "name")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cafe_name_address", columnNames = {"name", "address"})
        }
)
public class Cafe extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카페명 */
    @Column(nullable = false, length = 120)
    private String name;

    /** 주소(도로명/지번 혼용 가능) */
    @Column(length = 255)
    private String address;

    /** 위도 (WGS84) */
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    /** 경도 (WGS84) */
    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    /** 연락처(선택) */
    @Column(length = 30)
    private String phone;

    /**
     * 카테고리 코드(예: CAFE, BAKERY, DESSERT ...)
     * - 병합 이후 Enum 도입 예정: CafeCategory
     */
    @Column(name = "category_code", length = 50)
    private String categoryCode;

    /**
     * 리뷰 통계(선택적 사용)
     * - 다른 팀의 리뷰/별점 구조와 병합 용이성을 위해 비즈니스 필드로 마련
     */
    @Column(name = "avg_rating", nullable = false)
    private double avgRating = 0.0;

    @Column(name = "review_count", nullable = false)
    private int reviewCount = 0;

    /**
     * 서비스 활성화 여부
     * - soft delete / 노출 제어 등에 활용 가능
     */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * 리뷰 연관관계(양방향)
     * - Review.cafe 에 의해 관리됨
     * - 컬렉션 초기화만 해두고 지연로딩 기본값 유지
     * - 여기서는 매핑만 두고 비즈니스 로직은 서비스 계층에서 다룸
     */
    @OneToMany(mappedBy = "cafe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    // ---- 기본 생성자 ----
    protected Cafe() { }

    // ---- 편의 생성자 ----
    public Cafe(String name, String address) {
        this.name = name;
        this.address = address;
    }

    // ---- Getter/Setter ----
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public double getAvgRating() { return avgRating; }
    public int getReviewCount() { return reviewCount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<Review> getReviews() { return reviews; }

    // ---- 통계 업데이트 보조 메서드(선택적 사용) ----
    /**
     * 리뷰 추가 시 통계 업데이트
     * @param newRating 새 리뷰의 별점(1.0~5.0)
     */
    public void applyReviewAdded(double newRating) {
        double total = this.avgRating * this.reviewCount + newRating;
        this.reviewCount += 1;
        this.avgRating = (this.reviewCount == 0) ? 0.0 : total / this.reviewCount;
    }

    /**
     * 리뷰 수정 시 통계 업데이트
     * @param oldRating 수정 전 별점
     * @param newRating 수정 후 별점
     */
    public void applyReviewUpdated(double oldRating, double newRating) {
        double total = this.avgRating * this.reviewCount - oldRating + newRating;
        this.avgRating = (this.reviewCount == 0) ? 0.0 : total / this.reviewCount;
    }

    /**
     * 리뷰 삭제 시 통계 업데이트
     * @param removedRating 삭제한 리뷰 별점
     */
    public void applyReviewRemoved(double removedRating) {
        if (this.reviewCount <= 0) return;
        double total = this.avgRating * this.reviewCount - removedRating;
        this.reviewCount -= 1;
        this.avgRating = (this.reviewCount == 0) ? 0.0 : total / this.reviewCount;
    }
}
