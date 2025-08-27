package com.team.cafe.user;

import com.team.cafe.test.TestData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class UserController {
    private final UserRepository userRepository;


    @PreAuthorize("isAnonymous()")
    @GetMapping("/user/login")
    public String login() {
        return "user_login_form";
    }

//    @GetMapping("/user/login")
//    public String loginTest(Model model) {
//        List<SiteUser> userList = this.userRepository.findAll();
//        model.addAttribute("UserList", userList);
//        return "login_form";
//    }
}
