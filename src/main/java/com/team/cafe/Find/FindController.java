package com.team.cafe.Find;

import com.team.cafe.user.sjhy.SiteUser;
import com.team.cafe.user.sjhy.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class FindController {

    private final UserRepository userRepository;

    @GetMapping("/user/findId")
    public String findId() {
        return "/login/find_form";
    }

//    @PostMapping("/user/findId")
//    public String searchId(HttpServletRequest request, Model model,
//                           @RequestParam String nickname,
//                           @RequestParam String email) {
//    }
}
