package com.team.cafe.businessuser.sj;

import groovy.util.logging.Slf4j;
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
                                   @RequestParam(value = "success", required = false) String success,
                                   Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new BusinessUserDto());
        }
        model.addAttribute("error", error);
        model.addAttribute("success", success);
        return "signup/business-signup";
    }

    /* ---------- 회원가입 처리 ---------- */
    @PostMapping("/register")
    public String register(@ModelAttribute("form") BusinessUserDto form,
                           RedirectAttributes ra) {
        try {
            businessUserService.register(form); // 실제 저장
            ra.addAttribute("success", 1);
            return "redirect:/user/login";     // ★ 성공 시 로그인 화면으로
        } catch (DuplicateBusinessNumberException e) {
            ra.addAttribute("error", "businessNumberExists");
        } catch (DuplicateUsernameException e) {
            ra.addAttribute("error", "usernameExists");
        } catch (DuplicateEmailException e) {
            ra.addAttribute("error", "emailExists");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // NOT NULL/유니크/외래키 등 DB 제약 위반
            ra.addAttribute("error", "db");
        } catch (Exception e) {
            ra.addAttribute("error", "unknown");
        }
        ra.addFlashAttribute("form", form); // 입력값 유지
        System.out.println("[REGISTER][END:FAIL]");
        return "redirect:/businessuser/register"; // ★ 실패 시 폼으로
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
