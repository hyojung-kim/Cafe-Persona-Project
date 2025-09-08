package com.team.cafe.businessuser.sj;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/businessuser")
@RequiredArgsConstructor
public class BusinessUserController {

    private final BusinessUserService businessUserService;

    /* ---------- 회원가입 화면 ---------- */
    @GetMapping("/register")
    public String showRegisterForm(@RequestParam(value = "error", required = false) String error,
                                   Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new BusinessUserDto());
        }
        model.addAttribute("error", error); // ?error=... 로 넘어온 경우 JS/Thymeleaf에서 표시
        return "signup/business-signup";
    }

    /* ---------- 회원가입 처리 ---------- */
    @PostMapping("/register")
    public String register(@ModelAttribute("form") BusinessUserDto form,
                           RedirectAttributes ra) {
        try {
            businessUserService.register(form);    // 저장 및 연관관계 설정
            return "redirect:/";                   // 성공 시 이동 (원하면 로그인 페이지로 바꿔도 됨)
        } catch (DuplicateBusinessNumberException e) {
            ra.addAttribute("error", "businessNumberExists");
            return "redirect:/businessuser/register";
        } catch (DuplicateUsernameException e) {
            ra.addAttribute("error", "usernameExists");
            return "redirect:/businessuser/register";
        } catch (DuplicateEmailException e) {
            ra.addAttribute("error", "emailExists");
            return "/login/login_form";
        }
    }

    /* ---------- 실시간 중복 체크 API ---------- */
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
