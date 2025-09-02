package com.team.cafe.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "site_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_user_username", columnList = "username"),
                @Index(name = "idx_user_email", columnList = "email")
        }
)
public class SiteUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 로그인 아이디 (표시 이름과 동일하게 사용할 수 있음) */
    @NotBlank
    @Column(nullable = false, length = 60)
    private String username;

    /** 암호(BCrypt 등으로 인코딩된 값 저장) */
    @NotBlank
    @Column(nullable = false, length = 100)
    private String password;

    /** 이메일(선택적 인증 프로세스 고려) */
    @Email
    @Column(length = 120)
    private String email;

    /** 활성/잠금/탈퇴 등 상태 */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    /**
     * 권한 코드 목록 (예: ROLE_USER, ROLE_ADMIN)
     * - 병합 시 다른 팀의 권한 구조와 매핑하기 쉽도록 문자열 리스트로 보관
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "site_user_roles",
            joinColumns = @JoinColumn(name = "user_id",
                    foreignKey = @ForeignKey(name = "fk_user_roles_user"))
    )
    @Column(name = "role_code", length = 40, nullable = false)
    private List<String> roles = new ArrayList<>();

    protected SiteUser() { }

    public SiteUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ---- Getter/Setter ----
    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public List<String> getRoles() { return roles; }
    public void addRole(String roleCode) {
        if (!this.roles.contains(roleCode)) this.roles.add(roleCode);
    }
    public void removeRole(String roleCode) { this.roles.remove(roleCode); }
}
