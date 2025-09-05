package com.team.cafe.businessuser.sj;

import com.team.cafe.user.sjhy.SiteUser;
import com.team.cafe.user.sjhy.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDate;

@RequiredArgsConstructor
@Controller
public class MypageController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // 현재 로그인한 사용자 가져오기
    private SiteUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null; // 로그인 안 됨
        }

        Object principal = auth.getPrincipal();
        String username;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else {
            username = principal.toString();
        }

        return userService.getUser(username); // DB에서 SiteUser 조회
    }

    // 마이페이지 메인
    @GetMapping("/mypage")
    public String mypage(Model model) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("username", siteUser.getUsername());
        return "mypage/main";
    }

    // 계정 관리 화면
    @GetMapping("/mypage/account")
    public String accountPage(Model model) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("user", siteUser);
        return "mypage/account";
    }



    // 계정 관리 진입 전 - 비밀번호 확인 페이지
    @GetMapping("/mypage/verify_password")
    public String verifyPasswordPage() {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "redirect:/user/login";
        }
        return "mypage/verify_password"; // 비밀번호 입력 화면
    }

    // 비밀번호 확인 처리
    @PostMapping("/mypage/verify_password")
    public String verifyPassword(@RequestParam String password, Model model) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "redirect:/user/login";
        }

        if (!passwordEncoder.matches(password, siteUser.getPassword())) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "mypage/verify_password";
        }

        // 비밀번호 확인 성공 → 계정 관리 화면으로 이동
        return "redirect:/mypage/account";
    }


    // 계정 관리 수정 처리
    @PostMapping("/mypage/account/update")
    public String updateAccount(@RequestParam String nickname,
                                @RequestParam String email) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "redirect:/user/login";
        }

        siteUser.setNickname(nickname);
        siteUser.setEmail(email);
        userService.save(siteUser); // 저장 메서드가 UserService에 필요합니다

        return "redirect:/mypage";
    }

    // 비밀번호 변경
    @PostMapping("/mypage/account/update-password")
    @ResponseBody
    public String updatePassword(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam String newPassword) throws ServletException {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "fail"; // 로그인 안 됨
        }

        // 비밀번호 암호화 후 저장
        siteUser.setPassword(passwordEncoder.encode(newPassword));
        userService.save(siteUser);

        // 세션 종료 및 로그아웃 처리
        request.logout(); // Spring Security가 처리하는 로그아웃

        return "success";
    }


    // 휴대폰 번호 변경
    @PostMapping("/mypage/account/update-phone")
    @ResponseBody
    public String updatePhone(@RequestParam String phone) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "fail"; // 로그인 안 됨
        }

        // 휴대폰 번호 저장
        siteUser.setPhone(phone);
        userService.save(siteUser);

        return "success";
    }


}
