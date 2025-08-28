package com.team.cafe.user;

import com.team.cafe.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupForm() {
        return "/signup_form";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String username,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String nickname,
                         Model model) {

        String errorMessage = userService.register(username, email, password, nickname);
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            return "signup_form"; // 에러가 있으면 다시 가입 페이지로
        }

        return "redirect:/user/login"; // 정상 가입 후 로그인 페이지
    }


    // 로그인 페이지
    @GetMapping("/login")
    public String loginForm() {
        return "/login_form";
    }


    // 아이디 중복 체크
    @GetMapping(value = "/check-username", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean available = !userService.existsByUsername(username);
        return ResponseEntity.ok(available);
    }

    // 이메일 중복 체크
    @GetMapping(value = "/check-email", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean available = !userService.existsByEmail(email);
        return ResponseEntity.ok(available);
    }


    // 닉네임 중복 체크
    @GetMapping(value = "/check-nickname", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        // true = 사용 가능, false = 이미 존재
        boolean available = !userService.existsByNickname(nickname);
        return ResponseEntity.ok(available);
    }
}
