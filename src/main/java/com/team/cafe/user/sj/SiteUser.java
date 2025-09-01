package com.team.cafe.user.sj;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
public class SiteUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;    // 로그인 ID

    private String password;    // 암호화된 비밀번호

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;





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
    public boolean isAccountNonLocked() { return true; }
}
