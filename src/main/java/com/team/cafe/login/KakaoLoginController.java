package com.team.cafe.login;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KakaoLoginController {

    @GetMapping("/kakao/login")
    public String kakaoLogin() {
        return ("kakao_login");
    }
}
