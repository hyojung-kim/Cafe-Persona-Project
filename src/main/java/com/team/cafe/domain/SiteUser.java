package com.team.cafe.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

/**
 * SiteUser 엔티티
 * - 회원(사용자) 정보를 저장하는 DB 테이블과 매핑되는 클래스
 * - 로그인/권한관리/리뷰 작성자 등으로 활용됨
 */
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(
        name = "site_user",
        uniqueConstraints = @UniqueConstraint(columnNames = "username")
        // username 컬럼에 UNIQUE 제약 → 동일한 아이디 중복 가입 방지
)
public class SiteUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // PK. Auto Increment (DB에서 자동 증가)
    private Long id;

    @Column(nullable = false, length=50)
    // 회원 아이디 (필수, 최대 50자)
    private String username;

    @Column(nullable = false)
    // 비밀번호 (암호화된 값 저장 필수! → BCrypt 사용 권장)
    private String password;

    /**
     * 권한(ROLE) 목록
     * - @ElementCollection: 별도 엔티티가 아닌 단순 값 컬렉션을 별도 테이블에 저장
     * - user_roles 테이블에 (user_id, role) 형태로 저장됨
     * - 예: {"ROLE_USER", "ROLE_ADMIN"}
     */
    @ElementCollection(fetch = FetchType.EAGER)
    // FetchType.EAGER: 사용자 조회 시 항상 roles도 즉시 가져옴 (권한 확인이 잦으므로 적절)
    @CollectionTable(
            name="user_roles",
            joinColumns=@JoinColumn(name="user_id") // FK: SiteUser.id → user_roles.user_id
    )
    @Column(name="role") // 컬럼명: "role"
    private Set<String> roles;
}
