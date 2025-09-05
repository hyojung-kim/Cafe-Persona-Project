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

    @GetMapping("/user/findId")
    public String findId() {
        return "login/find_id";
    }

    //인증메일 발송
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

    // 인증번호 확인 및 아이디 출력.
    @PostMapping("/user/verifyCode")
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
}

