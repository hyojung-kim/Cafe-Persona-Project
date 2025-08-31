package com.team.cafe.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {


    // 자체 로그인 페이지
    // 실제 로그인 진행은 시큐리티가 대신 하기 때문에
    // PostMapping 사용하여 코드 구현 안 해도 됨
    @GetMapping("/user/login")
    public String loginForm() {
        return ("login_form");
    }
}
