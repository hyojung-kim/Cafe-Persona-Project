package com.team.cafe.login.login_hy;

import com.team.cafe.user.user_hy.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kakaoService;
    private final UserService userService;

    @GetMapping("/user/kakao/callback")
    public String kakaoCallback(@RequestParam String code) {
        // 1. AccessToken 발급
        String accessToken = kakaoService.getAccessToken(code);

        // 2. 사용자 정보 가져오기
        Map<String, Object> kakaoUserInfo = kakaoService.getUserInfo(accessToken);
        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");

        String kakaoId = String.valueOf(kakaoUserInfo.get("id"));
        String email = (String) kakaoAccount.get("email");
        String nickname = (String)((Map<String, Object>)kakaoAccount.get("profile")).get("nickname");

        // 3. DB 회원 조회/등록
        SiteUser user = userService.registerOrGetKakaoUser(kakaoId, email, nickname);

        // 4. Spring Security 로그인 처리
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        System.out.println("카카오 인가코드: " + code);
        // 여기서 토큰 요청 로직 실행
        return "redirect:/test/list";
        // 5. 메인 페이지로 리다이렉트
//        return "redirect:/test/list";
    }
}
