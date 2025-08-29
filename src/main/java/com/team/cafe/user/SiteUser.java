package com.team.cafe.user;

import com.team.cafe.bookmark.Bookmark;
import com.team.cafe.review.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "site_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_siteuser_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_siteuser_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_siteuser_username", columnList = "username"),
                @Index(name = "idx_siteuser_email", columnList = "email")
        }
)
public class SiteUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", columnDefinition = "BIGINT UNSIGNED")
    @Comment("회원 PK")
    private Integer id;

    @Column(name = "username", length = 50, nullable = false)
    @Comment("로그인 아이디 (unique)")
    private String username;

    @Column(name = "password", nullable = false)
    @Comment("비밀번호 (암호화 저장)")
    private String password;

    @Column(name = "email", length = 100, nullable = false)
    @Comment("이메일 (unique)")
    private String email;

    @Column(name = "nickname", length = 50)
    @Comment("닉네임")
    private String nickname;

    @Column(name = "role", length = 20, nullable = false)
    @Comment("권한(USER, ADMIN 등)")
    private String role = "USER";

    @Column(name = "created_at", nullable = false)
    @Comment("회원가입 일시")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    @Comment("최근 로그인 일시")
    private LocalDateTime lastLogin;

//    @Column(name = "is_active", nullable = false)
//    @Comment("계정 활성화 여부")
//    private boolean active = true;

    // --- 관계 매핑 ---
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("작성한 리뷰 목록")
    private List<Review> reviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("북마크 목록")
    private List<Bookmark> bookmarks;

    // 좋아요(추천)한 리뷰 (Review 쪽의 voters와 연결)
    @ManyToMany(mappedBy = "voters")
    @Comment("좋아요 누른 리뷰들")
    private Set<Review> likedReviews;


}