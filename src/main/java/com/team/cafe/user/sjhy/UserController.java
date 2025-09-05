package com.team.cafe.user.sjhy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.team.cafe.user.sjhy.UserService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

//    @GetMapping("/login")
//    public String loginForm() {
//        return "/login/login_form";
//    }





    @GetMapping("/login")
    public String kakaoLogin(Model model) {
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        model.addAttribute("redirectUri", redirectUri);
        return "/login/login_form"; // login.html
    }
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;
}
