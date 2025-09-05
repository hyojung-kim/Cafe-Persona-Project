package com.team.cafe.Find;

import com.team.cafe.user.sjhy.SiteUser;
import com.team.cafe.user.sjhy.UserRepository;
import com.team.cafe.user.sjhy.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class FindController {

    private final FindService findService;

    // 아이디 찾기 페이지
    @GetMapping("/user/findId")
    public String findId() {
        return "login/find_id";
    }

    // 비밀번호 찾기 페이지
    @GetMapping("/user/findPassword")
    public String findPassword() {
        return "login/find_password";
    }
    // 아이디 찾기 인증메일 발송
    @PostMapping("/user/findId")
    public String searchId(Model model, @RequestParam String email) {

        try {
            findService.sendVerificationCode(email);
            model.addAttribute("email", email);
            // 인증번호 입력창 보여줄지 여부
            model.addAttribute("showVerification", true);
        } catch (RuntimeException e) {
            model.addAttribute("emailError",
                    e.getMessage());
        }
        return "login/find_id";
    }

    // 인증번호 확인 및 아이디 출력
    @PostMapping("/user/verifyIdCode")
    public String verifyCode(@RequestParam String email,
                             @RequestParam String code,
                             Model model) {
        try {
            String username = findService.verifyCodeAndFindId(email, code);
            model.addAttribute("username", username);
            return "login/show_id";
        } catch (RuntimeException e) {
            model.addAttribute("codeError", e.getMessage());
            model.addAttribute("email", email);
            model.addAttribute("showVerification", true); // 다시 인증번호 입력 폼 표시
            return "login/find_id";
        }
    }



    // 비밀번호 찾기 인증메일 발송
    @PostMapping("/user/findPassword")
    public String searchUser(Model model, @RequestParam String username, @RequestParam String email) {

        try {
            findService.sendVerificationCodePW(username, email);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            // 임시 비밀번호 입력창 보여줄지 여부
            model.addAttribute("showVerification", true);
        } catch (RuntimeException e) {
            model.addAttribute("emailError",
                    e.getMessage());
        }
        return "login/find_password";
    }

    // 인증번호 확인 및 비밀번호 재설정 페이지 이동
    @PostMapping("/user/verifyPWCode")
    public String verifyPasswordCode(@RequestParam String username,
                                     @RequestParam String email,
                                     @RequestParam String code,
                                     Model model) {
        try {
            findService.verifyCodeAndFindPW(email, code);
            model.addAttribute("username", username);
            return "login/modify_password";
        } catch (RuntimeException e) {
            model.addAttribute("codeError", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("showVerification", true); // 다시 인증번호 입력 폼 표시
            return "login/find_password";
        }
    }

    @PostMapping("/user/modifyPassword")
    public String modifyPassword(@RequestParam String username,
                                @RequestParam String newPassword,
                                Model model) {
        try {
            findService.updatePassword(username, newPassword);
            model.addAttribute("successMessage", "비밀번호가 변경되었습니다. 다시 로그인 해주세요.");
            return "login/login_form"; // 로그인 페이지로 이동
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "login/modify_password";
        }
    }
}

