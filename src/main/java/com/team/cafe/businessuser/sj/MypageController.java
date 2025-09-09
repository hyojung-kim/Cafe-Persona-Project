package com.team.cafe.businessuser.sj;

import com.team.cafe.businessuser.sj.owner.cafe.CafeManageService;
import com.team.cafe.user.sjhy.SiteUser;
import com.team.cafe.user.sjhy.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class MypageController {

    /** (과거 TTL 방식 키 — 사용 안 함) */
    public static final String REAUTH_SESSION_KEY = "MYPAGE_REAUTH_AT";

    /** 🔐 1회용 재인증 토큰 키 */
    private static final String REAUTH_TOKEN_KEY = "MYPAGE_REAUTH_TOKEN";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CafeManageService cafeManageService;
    private final BusinessRepository businessRepository;

    /* ------------ 내부 record: 1회용 토큰 ------------ */
    private record ReauthToken(String nonce, String allowPathPrefix) {}

    /* ------------ 공통 유틸 ------------ */

    private SiteUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;

        Object principal = auth.getPrincipal();
        String username = (principal instanceof UserDetails ud) ? ud.getUsername() : principal.toString();
        return userService.getUser(username);
    }

    /** 캐시 금지(뒤로가기/BFCache 대응 도움) */
    private void setNoCache(HttpServletResponse res) {
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Expires", "0");
    }

    private boolean isSafeInternalPath(String url) {
        try {
            URI u = URI.create(url);
            return !u.isAbsolute() && url.startsWith("/");
        } catch (Exception e) {
            return false;
        }
    }

    private String redirectToVerifyWithContinue(HttpServletRequest req) {
        String target = req.getRequestURI();
        if (req.getQueryString() != null) target += "?" + req.getQueryString();
        String encoded = URLEncoder.encode(target, StandardCharsets.UTF_8);
        return "redirect:/mypage/verify_password?continue=" + encoded;
    }

    // 🔸 이름도 consume → present 로 의미 명확화
    private boolean requireReauthPresent(HttpServletRequest req, String requiredPathPrefix) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;

        Object o = session.getAttribute(REAUTH_TOKEN_KEY);
        if (!(o instanceof ReauthToken token)) return false;

        // 경로 프리픽스 안전장치
        if (requiredPathPrefix != null) {
            if (token.allowPathPrefix() == null || !requiredPathPrefix.startsWith("/")) {
                return false;
            }
            if (!requiredPathPrefix.startsWith(token.allowPathPrefix())) {
                return false;
            }
        }

        // 여기서는 '소모하지 않음'
        return true;
    }


    /* ------------ 마이페이지 메인 ------------ */
    @GetMapping("/mypage")
    public String mypage(Model model) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) return "redirect:/user/login";

        model.addAttribute("user", siteUser);
        model.addAttribute("isBusiness", siteUser.getBusinessUser() != null);
        return "mypage/mypage-main";
    }

    /* ------------ 계정 관리(보호: 재인증 필요) ------------ */
    @GetMapping("/mypage/account")
    public String accountPage(HttpServletRequest request, HttpServletResponse response, Model model) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) return "redirect:/user/login";

        // 존재 여부만 확인 (소모 X)
        if (!requireReauthPresent(request, "/mypage")) {
            return redirectToVerifyWithContinue(request);
        }
        setNoCache(response);

        model.addAttribute("user", siteUser);
        return "mypage/account";
    }


    /* ------------ 비밀번호 확인 ------------ */
    @GetMapping("/mypage/verify_password")
    public String verifyPasswordPage(@RequestParam(value = "continue", required = false) String cont,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     Model model) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) return "redirect:/user/login";

        //  여기서 기존 토큰을 지워서 '뒤→앞' 시 반드시 재로그인 유도
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(REAUTH_TOKEN_KEY);
        }

        setNoCache(response);
        model.addAttribute("continueUrl", cont);
        return "mypage/verify_password";
    }


    @PostMapping("/mypage/verify_password")
    public String verifyPassword(@RequestParam String password,
                                 @RequestParam(value = "continue", required = false) String cont,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 Model model) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) return "redirect:/user/login";

        if (!passwordEncoder.matches(password, siteUser.getPassword())) {
            setNoCache(response);
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("continueUrl", cont);
            return "mypage/verify_password";
        }

        //  1회용 토큰 발급 (허용 범위: /mypage 하위)
        HttpSession session = request.getSession(true);
        session.setAttribute(REAUTH_TOKEN_KEY, new ReauthToken(UUID.randomUUID().toString(), "/mypage"));

        String safe = (cont != null && isSafeInternalPath(cont)) ? cont : "/mypage/account";
        return "redirect:" + safe;
    }

    /* ------------ 계정 정보 수정(POST) ------------ */
    @PostMapping("/mypage/account/update")
    public String updateAccount(@RequestParam String nickname,
                                @RequestParam String email) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) return "redirect:/user/login";

        siteUser.setNickname(nickname);
        siteUser.setEmail(email);
        userService.save(siteUser);

        return "redirect:/mypage-main";
    }

    /* ------------ 비밀번호 변경(ajax) ------------ */
    @PostMapping("/mypage/account/update-password")
    @ResponseBody
    public String updatePassword(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam String newPassword) throws ServletException {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) return "fail";

        siteUser.setPassword(passwordEncoder.encode(newPassword));
        userService.save(siteUser);

        request.logout(); // 보안상 로그아웃
        return "success";
    }

    /* ------------ 휴대폰 변경(ajax) ------------ */
    @PostMapping("/mypage/account/update-phone")
    @ResponseBody
    public String updatePhone(@RequestParam String phone) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) return "fail";

        siteUser.setPhone(phone);
        userService.save(siteUser);
        return "success";
    }

    /* ------------ 사업자 가드 & 카페 관리 ------------ */

    @GetMapping("/mypage/cafe/manage")
    public String cafeManage(HttpServletRequest request, HttpServletResponse response, Model model) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";
        if (user.getBusinessUser() == null) return "redirect:/mypage?denied=biz";

        if (!requireReauthPresent(request, "/mypage")) {
            return redirectToVerifyWithContinue(request);
        }
        setNoCache(response);

        Business business = businessRepository.findByUserId(user.getId()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("business", business);
        return "mypage/cafe_manage";
    }

    @GetMapping("/mypage/cafe/register")
    public String cafeRegister(HttpServletRequest request, HttpServletResponse response, Model model) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";
        if (user.getBusinessUser() == null) return "redirect:/mypage?denied=biz";

        if (!requireReauthPresent(request, "/mypage")) {
            return redirectToVerifyWithContinue(request);
        }
        setNoCache(response);

        if (businessRepository.existsByUserId(user.getId())) {
            return "redirect:/mypage/cafe/manage";
        }
        model.addAttribute("user", user);
        return "mypage/cafe_register";
    }

    @PostMapping("/mypage/cafe/register")
    public String saveCafeRegister(
            @RequestParam String companyName,
            @RequestParam String businessNumber,
            @RequestParam(required=false) String representativeName,
            @RequestParam(required=false) String representativeEmail,
            @RequestParam(required=false) String representativePhone,
            @RequestParam(required=false) String address,
            @RequestParam(required=false) String description,
            RedirectAttributes ra
    ) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";
        if (user.getBusinessUser() == null) return "redirect:/mypage?denied=biz";

        try {
            cafeManageService.createBusiness(user, companyName, businessNumber,
                    representativeName, representativeEmail, representativePhone,
                    address, description);
            ra.addFlashAttribute("toast", "사업장이 등록되었습니다.");
            return "redirect:/mypage/cafe/manage";
        } catch (DuplicateBusinessNumberException e) {
            ra.addFlashAttribute("error", "이미 사용 중인 사업자등록번호입니다.");
            return "redirect:/mypage/cafe/register";
        }
    }
}
