package com.team.cafe.login;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {


    // 카카오, 일반 로그인 페이지
    // 실제 로그인 진행은 시큐리티가 대신 하기 때문에
    // PostMapping 사용하여 코드 구현 안 해도 됨
    @GetMapping("/user/login")
    public String kakaoLogin(Model model) {
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        model.addAttribute("redirectUri", redirectUri);
        return "login_form"; // login.html
    }

    // 사업자, 일반 회원 선택 페이지
    @GetMapping("/user/choice")
    public String userChoice() {
        return ("user_choice");
    }

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;


}
