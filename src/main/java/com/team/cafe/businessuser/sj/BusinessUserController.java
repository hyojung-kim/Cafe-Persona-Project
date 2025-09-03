package com.team.cafe.businessuser.sj;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/businessuser")
@RequiredArgsConstructor
public class BusinessUserController {

    private final BusinessUserService businessUserService;
    private final BusinessUserRepository businessUserRepository;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "signup/business-signup";  // Thymeleaf 뷰
    }

    @PostMapping("/register")
    public String register(@ModelAttribute BusinessUserDto dto, Model model) {
        BusinessUser businessUser = businessUserService.register(dto);
        model.addAttribute("user", businessUser);
        return "cafe/list"; // 가입 성공 페이지
    }

    @GetMapping(value = "/check-username", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean available = !businessUserService.existsByUsername(username);
        return ResponseEntity.ok(available);
    }

    @GetMapping(value = "/check-email", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean available = !businessUserService.existsByEmail(email);
        return ResponseEntity.ok(available);
    }

    @GetMapping(value = "/check-nickname", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean available = !businessUserService.existsByNickname(nickname);
        return ResponseEntity.ok(available);
    }


}
