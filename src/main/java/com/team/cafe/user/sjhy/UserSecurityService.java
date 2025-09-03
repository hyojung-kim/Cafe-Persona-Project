package com.team.cafe.user.sjhy;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SiteUser> _siteUser = this.userRepository.findByUsername(username);
        if (_siteUser.isEmpty()) {
            throw new UsernameNotFoundException("사용자를 찾을수 없습니다.");
        }
        SiteUser siteUser = _siteUser.get();
        List<GrantedAuthority> authorities = new ArrayList<>();
        if ("business".equals(username)) {
            authorities.add(new SimpleGrantedAuthority(UserRole.BUSINESS.getValue()));
        } else {
            authorities.add(new SimpleGrantedAuthority(UserRole.USER.getValue()));
        }
        // 카카오 로그인 위한 임시 데이터
//        return User.builder()
//                .username(siteUser.getUsername())
//                .password(siteUser.getPassword() != null ? siteUser.getPassword() : "{noop}kakao_user")
//                .authorities(authorities)
//                .accountExpired(false)       // 계정 만료 여부
//                .accountLocked(false)        // 계정 잠금 여부
//                .credentialsExpired(false)   // 비밀번호 만료 여부
//                .disabled(false)             // 활성화 여부
//                .build();

        // 카카오 api는 비밀번호 null상태로 데이터를 받아오기 때문에
        // null 값일때 임시 데이터를 넣어주기로,
        return new User(siteUser.getUsername(),siteUser.getPassword() != null ? siteUser.getPassword() : "{noop}kakao_user", authorities);
    }
}