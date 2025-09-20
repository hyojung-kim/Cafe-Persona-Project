package com.team.cafe.user.sjhy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.team.cafe.user.sjhy.UserService;

@Controller
@RequiredArgsConstructor
//@RequestMapping("/user")// hy
public class UserController {

    private final UserService userService;

    @GetMapping("/login/select")
    public String loginSelect() {
        return "/login/login_select";
    }

    @GetMapping("/user/login")
    public String kakaoLogin(Model model) {
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        model.addAttribute("redirectUri", redirectUri);
        return "/login/login_form_user"; // login.html
    }

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @GetMapping("/business/login")
    public String businessLoginForm() {
        return "/login/login_form_business";
    }
}
