package com.team.cafe.businessuser.sj;

import com.team.cafe.user.sjhy.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "business")
public class BusinessUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private SiteUser user;  // 연결된 일반회원

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "business_number", nullable = false, unique = true)
    private String businessNumber;

    @Column(name = "representative_name")
    private String representativeName;

    @Column(name = "representative_phone")
    private String representativePhone;

    @Column(name = "representative_email")
    private String representativeEmail;

    @Column(name = "address")
    private String address;

    @Column(name = "status", length = 20)
    private String status = "pending";

    @Column(name = "created_at", nullable = false)
    @Comment("회원 가입 일시")
    private LocalDateTime createDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
