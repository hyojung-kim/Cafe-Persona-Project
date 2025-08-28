package com.team.cafe.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 일반 회원가입
    public String register(String username, String email, String password, String nickname) {

        // 1️⃣ 아이디 중복 체크
        if (userRepository.existsByUsername(username)) {
            return "이미 사용중인 아이디입니다.";
        }

        // 2️⃣ 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            return "이미 가입된 이메일입니다.";
        }

        // 3️⃣ 닉네임 중복 체크
        if (userRepository.existsByNickname(nickname)) {
            return "이미 사용중인 닉네임입니다.";
        }

        // 4️⃣ 비밀번호 유효성 검사
        if (!isValidPassword(password)) {
            return "비밀번호는 최소 8자 이상이어야 하고, 특수문자를 포함해야 합니다.";
        }

        // 5️⃣ 정상 회원가입 처리
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        return null; // null이면 정상 가입
    }

    // 비밀번호 검증: 최소 8자, 특수문자 포함
    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,}$";
        return password != null && password.matches(pattern);
    }

    // 카카오 로그인/회원가입
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

    // 사용자 조회
    public SiteUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
    }

    // 중복 체크용 메소드 (컨트롤러에서 AJAX 호출)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
