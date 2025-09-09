package com.team.cafe.businessuser.sj;

import com.team.cafe.user.sjhy.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    @Column(name = "id")
    @Comment("Business PK")
    private Long id;

    /**
     * 한 사용자당 사업장 1개를 기본 가정(원하면 @OneToMany로 변경 가능)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @Comment("해당 사업장의 소유자(SiteUser)")
    private SiteUser user;

    @Column(name = "company_name", nullable = false, length = 255)
    @Comment("상호명")
    private String companyName;

    @Column(name = "business_number", nullable = false, length = 50)
    @Comment("사업자등록번호 (unique)")
    private String businessNumber;

    @Column(name = "representative_name", length = 100)
    @Comment("대표자명")
    private String representativeName;

    @Column(name = "representative_email", length = 255)
    @Comment("대표자 이메일")
    private String representativeEmail;

    @Column(name = "representative_phone", length = 50)
    @Comment("대표자 연락처")
    private String representativePhone;

    @Column(name = "address", length = 255)
    @Comment("사업장 주소")
    private String address;

    @Column(name = "description", columnDefinition = "TEXT")
    @Comment("소개/설명")
    private String description;


    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    @Comment("생성일시")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    @Comment("수정일시")
    private LocalDateTime updatedAt;




}
