package com.team.cafe.user;

import org.springframework.web.bind.annotation.GetMapping;


public class UserController {

    @GetMapping("/user/login")
    public String login() {
        return "login_form";
    }
}
