package com.team.cafe.login.login_hy;

import com.team.cafe.user.user_hy.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 카카오 로그인
    public SiteUser registerOrGetKakaoUser(String kakaoId, String email, String nickname) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    SiteUser user = new SiteUser();
                    user.setUsername(kakaoId);
                    user.setEmail(email);
                    user.setNickname(nickname);
                    user.setPassword(passwordEncoder.encode("KAKAO_PASSWORD"));
                    return userRepository.save(user);
                });
    }
}
