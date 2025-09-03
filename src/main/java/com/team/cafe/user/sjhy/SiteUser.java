package com.team.cafe.user.sjhy;

import com.team.cafe.bookmark.Bookmark;
import com.team.cafe.review.Review;
import com.team.cafe.review.ReviewLike;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public class SiteUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", columnDefinition = "BIGINT UNSIGNED")
    @Comment("회원 PK")
    private Long id;

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

    @Column(name = "last_login")
    @Comment("최근 로그인 일시")
    private LocalDateTime lastLogin;

//    @Column(name = "is_active", nullable = false)
//    @Comment("계정 활성화 여부")
//    private boolean active = true;

    // --- 관계 매핑 ---
    // ✅ Review 쪽 필드명이 'author' 이므로 mappedBy도 'author'로 맞춘다.
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("작성한 리뷰 목록")
    private List<Review> reviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("북마크 목록")
    private List<Bookmark> bookmarks;

    // ✅ ReviewLike 중간 엔티티를 통한 좋아요 목록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("내가 좋아요한 리뷰(ReviewLike 중간 엔티티)")
    private List<ReviewLike> reviewLikes = new ArrayList<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //  사용자의 권한 목록을 반환
        //  여기서는 ROLE_USER 권한 하나만 부여 (추후 수정 예정)
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }


    //  계정 만료 여부 확인 override
//  true : 만료되지 않음 (로그인 가능)
//  신고기능 여부에 따라 수정 및 삭제 가능
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}

