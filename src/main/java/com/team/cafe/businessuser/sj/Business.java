package com.team.cafe.businessuser.sj;

import com.team.cafe.user.sjhy.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "business",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_business_number", columnNames = "business_number"),
                @UniqueConstraint(name = "uk_business_user", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_business_number", columnList = "business_number"),
                @Index(name = "idx_business_user", columnList = "user_id")
        }
)
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_id", columnDefinition = "BIGINT UNSIGNED")
    @Comment("Business PK")
    private Long id;

    /**
     * 한 사용자당 사업장 1개를 기본 가정(원하면 @OneToMany로 변경 가능)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @Comment("해당 사업장의 소유자(SiteUser)")
    private SiteUser user;

    @Column(name = "company_name", nullable = false, length = 100)
    @Comment("상호명")
    private String companyName;

    @Column(name = "business_number", nullable = false, length = 30)
    @Comment("사업자등록번호 (unique)")
    private String businessNumber;

    @Column(name = "representative_name", length = 50)
    @Comment("대표자명")
    private String representativeName;

    @Column(name = "representative_email", length = 100)
    @Comment("대표자 이메일")
    private String representativeEmail;

    @Column(name = "representative_phone", length = 30)
    @Comment("대표자 연락처")
    private String representativePhone;

    @Column(name = "address", length = 255)
    @Comment("사업장 주소")
    private String address;

    @Column(name = "description", columnDefinition = "TEXT")
    @Comment("소개/설명")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Comment("운영 상태")
    private BusinessStatus status = BusinessStatus.DRAFT;

    @Column(name = "created_at", nullable = false)
    @Comment("생성일시")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Comment("수정일시")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        final LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.status = BusinessStatus.ACTIVE;
    }

    // 혹시라도 null 들어오면 ACTIVE로 보정
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
        if (this.status == null) this.status = BusinessStatus.ACTIVE;
    }

    public void setStatus(BusinessStatus ignored) {
        this.status = BusinessStatus.ACTIVE;
    }
}
