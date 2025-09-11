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
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class MypageController {

    /** 1회용 재인증 토큰 세션 키 (엄격 보호용) */
    private static final String REAUTH_TOKEN_KEY = "MYPAGE_REAUTH_TOKEN";
    /** 카페관리 → 등록 흐름 허용 플래그 (같은 세션 내에서만) */
    private static final String CAFE_FLOW_FLAG = "CAFE_FLOW_OK";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CafeManageService cafeManageService;
    private final BusinessRepository businessRepository;

    /* ------------ 토큰 모델 ------------ */
    private record ReauthToken(String nonce, String allowExactUrl) {}

    /* ------------ 공통 유틸 ------------ */

    private SiteUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;

        Object principal = auth.getPrincipal();
        String username = (principal instanceof UserDetails ud) ? ud.getUsername() : principal.toString();
        return userService.getUser(username);
    }

    /** 템플릿에서 ${user} 바로 쓰도록 */
    @ModelAttribute("user")
    public SiteUser injectCurrentUser() { return getCurrentUser(); }

    private void setNoCache(HttpServletResponse res) {
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Expires", "0");
    }

    /** 내부 경로만 허용 */
    private boolean isSafeInternalPath(String url) {
        try {
            URI u = URI.create(url);
            return !u.isAbsolute() && url.startsWith("/");
        } catch (Exception e) {
            return false;
        }
    }

    /** 현재 요청을 재인증 페이지로 302 (reauth 제거 후 continue에 싣기) */
    private String redirectToVerifyWithContinue(HttpServletRequest req) {
        String target = currentUrlWithoutReauthNormalized(req);
        String encoded = URLEncoder.encode(target, StandardCharsets.UTF_8);
        return "redirect:/mypage/verify_password?continue=" + encoded;
    }

    private ReauthToken getToken(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object o = s.getAttribute(REAUTH_TOKEN_KEY);
        return (o instanceof ReauthToken t) ? t : null;
    }
    private void clearToken(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.removeAttribute(REAUTH_TOKEN_KEY);
    }

    private String stripReauthParam(String internalUrl) {
        if (internalUrl == null || internalUrl.isBlank()) return internalUrl;
        String[] parts = internalUrl.split("\\?", 2);
        if (parts.length == 1) return internalUrl;
        String path = parts[0];
        String filtered = Arrays.stream(parts[1].split("&"))
                .filter(p -> !p.toLowerCase().startsWith("reauth="))
                .collect(Collectors.joining("&"));
        return filtered.isEmpty() ? path : (path + "?" + filtered);
    }

    private String currentUrlWithoutReauthNormalized(HttpServletRequest req) {
        String rawPath = req.getRequestURI();               // 컨텍스트패스 포함
        String pathNoSemi = rawPath.split(";", 2)[0];       // ;jsessionid 제거
        if (pathNoSemi.length() > 1 && pathNoSemi.endsWith("/")) {
            pathNoSemi = pathNoSemi.substring(0, pathNoSemi.length() - 1);
        }
        String qs = req.getQueryString();
        if (qs != null && !qs.isEmpty()) {
            String filtered = Arrays.stream(qs.split("&"))
                    .filter(p -> !p.toLowerCase().startsWith("reauth="))
                    .collect(Collectors.joining("&"));
            return filtered.isEmpty() ? pathNoSemi : (pathNoSemi + "?" + filtered);
        }
        return pathNoSemi;
    }

    private String normalizeBasePath(HttpServletRequest req, String basePath) {
        String ctx = req.getContextPath() == null ? "" : req.getContextPath();
        String withoutSemicolon = basePath.split(";", 2)[0];
        int q = withoutSemicolon.indexOf('?');
        String path = (q >= 0) ? withoutSemicolon.substring(0, q) : withoutSemicolon;
        String query = (q >= 0) ? withoutSemicolon.substring(q) : "";
        if (path.length() > 1 && path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return ctx + path + query;
    }

    /** 1회용: nonce/URL 모두 맞아야 통과, 맞으면 즉시 소모 */
    private boolean requireReauthConsume(HttpServletRequest req) {
        String nonceParam = req.getParameter("reauth");
        if (nonceParam == null || nonceParam.isBlank()) return false;

        ReauthToken t = getToken(req);
        if (t == null) return false;

        String curr = currentUrlWithoutReauthNormalized(req);
        boolean ok = t.nonce().equals(nonceParam) && t.allowExactUrl().equals(curr);
        if (!ok) return false;

        clearToken(req);
        return true;
    }

    /* ============= 마이페이지 메인 ============= */
    @GetMapping("/mypage")
    public String mypage(HttpServletResponse response, Model model) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";
        setNoCache(response);
        model.addAttribute("isBusiness", user.getBusinessUser() != null);
        return "mypage/mypage-main";
    }

    /* ============= 계정 관리(보호) ============= */
    @GetMapping("/mypage/account")
    public String accountPage(HttpServletRequest request, HttpServletResponse response) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";

        if (!requireReauthConsume(request)) return redirectToVerifyWithContinue(request);
        setNoCache(response);
        return "mypage/account";
    }

    /* ============= 비밀번호 확인 ============= */
    @GetMapping("/mypage/verify_password")
    public String verifyPasswordPage(@RequestParam(value = "continue", required = false) String cont,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     Model model) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";

        HttpSession session = request.getSession(false);
        if (session != null) session.removeAttribute(REAUTH_TOKEN_KEY); // 뒤→앞 시 강제 재확인

        setNoCache(response);
        model.addAttribute("continueUrl", cont);
        return "mypage/verify_password"; // 뷰 렌더
    }

    @PostMapping("/mypage/verify_password")
    public String verifyPassword(@RequestParam String password,
                                 @RequestParam(value = "continue", required = false) String cont,
                                 HttpServletRequest request) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";

        if (!passwordEncoder.matches(password, user.getPassword())) {
            String encoded = cont != null ? URLEncoder.encode(cont, StandardCharsets.UTF_8) : "";
            return "redirect:/mypage/verify_password?error=1" + (encoded.isEmpty() ? "" : "&continue=" + encoded);
        }

        String base = (cont != null && isSafeInternalPath(cont)) ? cont : "/mypage/account";
        String baseNoReauth = stripReauthParam(base);
        String allowExactUrl = normalizeBasePath(request, baseNoReauth);

        String nonce = UUID.randomUUID().toString();
        request.getSession(true).setAttribute(REAUTH_TOKEN_KEY, new ReauthToken(nonce, allowExactUrl));

        String sep = (baseNoReauth.contains("?")) ? "&" : "?";
        String redirectUrl = baseNoReauth + sep + "reauth=" + URLEncoder.encode(nonce, StandardCharsets.UTF_8);

        return "redirect:" + redirectUrl;
    }

    /* ============= 계정 정보 수정(보호) ============= */
    @PostMapping("/mypage/account/update")
    public String updateAccount(@RequestParam String nickname,
                                @RequestParam String email,
                                HttpServletRequest request) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";
        if (!requireReauthConsume(request)) return redirectToVerifyWithContinue(request);

        user.setNickname(nickname);
        user.setEmail(email);
        userService.save(user);

        return "redirect:/mypage";
    }

    @PostMapping("/mypage/account/update-password")
    @ResponseBody
    public String updatePassword(HttpServletRequest request,
                                 @RequestParam String newPassword) throws ServletException {
        SiteUser user = getCurrentUser();
        if (user == null) return "fail";
        if (!requireReauthConsume(request)) return "fail";

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);
        request.logout();
        return "success";
    }

    @PostMapping("/mypage/account/update-phone")
    @ResponseBody
    public String updatePhone(HttpServletRequest request,
                              @RequestParam String phone) {
        SiteUser user = getCurrentUser();
        if (user == null) return "fail";
        if (!requireReauthConsume(request)) return "fail";

        user.setPhone(phone);
        userService.save(user);
        return "success";
    }

    // ✅ 카페 관리(보호: 반드시 재인증 소모)
    @GetMapping("/mypage/cafe/manage")
    public String cafeManage(HttpServletRequest request, HttpServletResponse response, Model model) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";
        if (user.getBusinessUser() == null) return "redirect:/mypage?denied=biz";

        // ⬇️ 여기서만 재인증을 '소모' 요구
        if (!requireReauthConsume(request)) {
            return redirectToVerifyWithContinue(request);
        }
        setNoCache(response);

        Business business = businessRepository.findByUserId(user.getId()).orElse(null);
        model.addAttribute("business", business);

        return "mypage/cafe_manage";
    }


    // ✅ 카페 등록(예외 경로): 재인증 요구 금지! (로그인/사업자만 확인)
    @GetMapping("/mypage/cafe/register")
    public String cafeRegister(HttpServletRequest request, HttpServletResponse response, Model model) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";
        if (user.getBusinessUser() == null) return "redirect:/mypage?denied=biz";

        // ⛔ 절대 requireReauthConsume 호출하지 마세요 (여기가 계속 verify로 가는 원인이었음)
        setNoCache(response);

//        // 사업장 이미 있으면 관리로 보냄(원래 로직 유지)
//        if (businessRepository.existsByUserId(user.getId())) {
//            return "redirect:/mypage/cafe/manage";
//        }

        // 파일명이 mypage/cafe-register.html 이면 뷰 이름도 하이픈 그대로
        return "mypage/cafe-register";

    }

    @PostMapping("/mypage/cafe/register")
    public String saveCafeRegister(HttpServletRequest request,
                                   @RequestParam String companyName,
                                   @RequestParam String businessNumber,
                                   @RequestParam(required=false) String representativeName,
                                   @RequestParam(required=false) String representativeEmail,
                                   @RequestParam(required=false) String representativePhone,
                                   @RequestParam(required=false) String address,
                                   @RequestParam(required=false) String description,
                                   RedirectAttributes ra) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";
        if (user.getBusinessUser() == null) return "redirect:/mypage?denied=biz";

        // ✅ POST도 플래그만 확인 (비번 재확인 요구 X)
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(CAFE_FLOW_FLAG) == null) {
            return "redirect:/mypage/cafe/manage";
        }

        try {
            cafeManageService.createBusiness(user, companyName, businessNumber,
                    representativeName, representativeEmail, representativePhone,
                    address, description);
            ra.addFlashAttribute("toast", "사업장이 등록되었습니다.");

            // (선택) 성공 후 플로우 플래그 제거
            session.removeAttribute(CAFE_FLOW_FLAG);

            return "redirect:/mypage/cafe/manage";
        } catch (DuplicateBusinessNumberException e) {
            ra.addFlashAttribute("error", "이미 사용 중인 사업자등록번호입니다.");
            return "redirect:/mypage/cafe/register";
        }
    }
}
