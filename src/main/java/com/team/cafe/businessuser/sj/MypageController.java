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
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class MypageController {

    /* =========================
       Constants (Session Keys)
       ========================= */
    /** 비번 재확인용 1회 토큰 */
    private static final String REAUTH_TOKEN_KEY = "MYPAGE_REAUTH_TOKEN";
    /** (기존) 카페 등록 플로우 POST 가드 */
    private static final String CAFE_FLOW_FLAG = "CAFE_FLOW_OK";
    /** ✅ 등록 성공 후, /manage 1회 통과 플래그 */
    private static final String MANAGE_PASS_ONCE = "MANAGE_PASS_ONCE";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CafeManageService cafeManageService;
    private final BusinessUserRepository businessUserRepository;
    private final BusinessUserService businessUserService;

    /* =========================
       Reauth Token
       ========================= */
    private record ReauthToken(String nonce, String allowExactUrl) {}

    /* =========================
       Common helpers
       ========================= */
    private SiteUser currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object p = auth.getPrincipal();
        if ("anonymousUser".equals(p)) return null;
        String username = (p instanceof UserDetails ud) ? ud.getUsername() : String.valueOf(p);
        return userService.getUser(username);
    }

    /** 템플릿에서 ${user} 사용 */
    @ModelAttribute("user")
    public SiteUser injectUser() { return currentUserOrNull(); }

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

    private String normalizedCurrentWithoutReauth(HttpServletRequest req) {
        String rawPath = req.getRequestURI();           // may include ctx-path
        String pathNoSemi = rawPath.split(";", 2)[0];   // strip ;jsessionid
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
        String ctx = (req.getContextPath() == null) ? "" : req.getContextPath();
        String withoutSemicolon = basePath.split(";", 2)[0];

        int q = withoutSemicolon.indexOf('?');
        String path = (q >= 0) ? withoutSemicolon.substring(0, q) : withoutSemicolon;
        String query = (q >= 0) ? withoutSemicolon.substring(q) : "";

        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // 이미 ctx로 시작하면 다시 붙이지 않음
        if (!ctx.isEmpty() && path.startsWith(ctx)) {
            return path + query;
        }

        // 일반 케이스: ctx + path
        return ctx + path + query;
    }

    /** 재인증 토큰 get/clear */
    private ReauthToken getReauthToken(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object o = s.getAttribute(REAUTH_TOKEN_KEY);
        return (o instanceof ReauthToken t) ? t : null;
    }
    private void clearReauthToken(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.removeAttribute(REAUTH_TOKEN_KEY);
    }

    /** ✅ 재인증 소모 (정확한 URL & nonce 일치 시) */
    private boolean requireReauthConsume(HttpServletRequest req) {

        String nonceParam = req.getParameter("reauth");
        if (nonceParam == null || nonceParam.isBlank()) return false;
        ReauthToken t = getReauthToken(req);
        if (t == null) return false;


        String curr = normalizedCurrentWithoutReauth(req);
        if (!safeUrlEqualsIgnoreReauthAndCtx(req, t.allowExactUrl(), curr)) {
            return false;
        }
        if (!t.nonce().equals(nonceParam)) return false;

        clearReauthToken(req);
        return true;
    }

    /** ctx 유무, reauth 파라미터, 파라미터 순서/인코딩 차이를 무시하고 동등 비교 */
    private boolean safeUrlEqualsIgnoreReauthAndCtx(HttpServletRequest req, String a, String b) {
        try {
            String ctx = (req.getContextPath() == null) ? "" : req.getContextPath();

            UrlParts ua = parseAndNormalizeUrl(a, ctx);
            UrlParts ub = parseAndNormalizeUrl(b, ctx);

            // 경로 동일?
            if (!Objects.equals(ua.path, ub.path)) return false;

            // 쿼리 파라미터 동등? (reauth 제외, 다중값은 정렬 비교)
            return Objects.equals(ua.params, ub.params);
        } catch (Exception e) {
            return a.equals(b); // 최후의 보루
        }
    }

    private static class UrlParts {
        final String path;
        final Map<String, List<String>> params;
        UrlParts(String p, Map<String, List<String>> q) {
            this.path = p;
            this.params = q;
        }
    }

    private UrlParts parseAndNormalizeUrl(String raw, String ctx) {
        // raw: "/app/mypage/cafe/manage?x=1&reauth=...&y=a+b"
        String noSemi = raw.split(";", 2)[0];

        String path, qs;
        int q = noSemi.indexOf('?');
        if (q >= 0) {
            path = noSemi.substring(0, q);
            qs   = noSemi.substring(q + 1);
        } else {
            path = noSemi;
            qs   = "";
        }

        // 트레일링 슬래시 제거
        if (path.length() > 1 && path.endsWith("/")) path = path.substring(0, path.length() - 1);

        // ✅ ctx 정규화: 이미 ctx로 시작하면 그대로, 아니면 붙여줌
        if (ctx != null && !ctx.isEmpty() && !path.startsWith(ctx)) {
            path = ctx + (path.startsWith("/") ? path : ("/" + path));
        }

        // 쿼리 파라미터 파싱 (reauth 제외), 값은 디코드 후 정렬
        Map<String, List<String>> map = new TreeMap<>(); // 키 정렬
        if (!qs.isEmpty()) {
            for (String kv : qs.split("&")) {
                if (kv.isBlank()) continue;
                int idx = kv.indexOf('=');
                String k = idx >= 0 ? kv.substring(0, idx) : kv;
                String v = idx >= 0 ? kv.substring(idx + 1) : "";
                k = urlDecode(k);
                if ("reauth".equalsIgnoreCase(k)) continue; // ✅ 제외
                v = urlDecode(v);
                map.computeIfAbsent(k, _k -> new ArrayList<>()).add(v);
            }
        }
        // 다중값 정렬
        for (List<String> vs : map.values()) {
            Collections.sort(vs);
        }

        return new UrlParts(path, map);
    }


    private String urlDecode(String s) {
        try {
            // application/x-www-form-urlencoded 규칙(+ -> space)
            return java.net.URLDecoder.decode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    /** ✅ 등록 직후 /manage 1회 통과 consume */
    private boolean consumeManagePassOnce(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return false;
        Object flag = s.getAttribute(MANAGE_PASS_ONCE);
        if (!Boolean.TRUE.equals(flag)) return false;
        s.removeAttribute(MANAGE_PASS_ONCE); // consume
        return true;
    }

    /** 재인증 페이지로 리다이렉트(현재 URL을 continue로) */
    private String redirectToVerifyWithContinue(HttpServletRequest req) {
        String target = normalizedCurrentWithoutReauth(req);
        String encoded = URLEncoder.encode(target, StandardCharsets.UTF_8);
        return "redirect:/mypage/verify_password?continue=" + encoded;
    }

    /* =========================
       My Page - Main
       ========================= */
    @GetMapping("/mypage")
    public String mypage(HttpServletResponse response, Model model) {
        SiteUser user = currentUserOrNull();
        if (user == null) return "redirect:/user/login";
        setNoCache(response);
        model.addAttribute("isBusiness", user.getBusinessUser() != null);
        return "mypage/mypage-main";
    }

    /* =========================
       Account (Protected by reauth)
       ========================= */
    @GetMapping("/mypage/account")
    public String accountPage(HttpServletRequest request, HttpServletResponse response) {
        SiteUser user = currentUserOrNull();
        if (user == null) return "redirect:/user/login";
        if (!requireReauthConsume(request)) return redirectToVerifyWithContinue(request);
        setNoCache(response);
        return "mypage/account";
    }

    /* =========================
       Verify Password Flow
       ========================= */
    @GetMapping("/mypage/verify_password")
    public String verifyPasswordPage(@RequestParam(value = "continue", required = false) String cont,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     Model model) {
        SiteUser user = currentUserOrNull();
        if (user == null) return "redirect:/user/login";
        // 뒤→앞 접근 시 항상 새로 확인
        HttpSession session = request.getSession(false);
        if (session != null) session.removeAttribute(REAUTH_TOKEN_KEY);

        setNoCache(response);
        model.addAttribute("continueUrl", cont);
        return "mypage/verify_password";
    }

    @PostMapping("/mypage/verify_password")
    public String verifyPassword(@RequestParam String password,
                                 @RequestParam(value = "continue", required = false) String cont,
                                 HttpServletRequest request) {
        SiteUser user = currentUserOrNull();
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

    /* =========================
       Account Update (Protected)
       ========================= */
    @PostMapping("/mypage/account/update")
    public String updateAccount(@RequestParam String nickname,
                                @RequestParam String email,
                                HttpServletRequest request) {
        SiteUser user = currentUserOrNull();
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
        SiteUser user = currentUserOrNull();
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
        SiteUser user = currentUserOrNull();
        if (user == null) return "fail";
        if (!requireReauthConsume(request)) return "fail";

        user.setPhone(phone);
        userService.save(user);
        return "success";
    }

    /* =========================
       Cafe Manage (Protected)
       ========================= */
    @GetMapping("/mypage/cafe/manage")
    public String cafeManage(HttpServletRequest request, HttpServletResponse response, Model model) {
        SiteUser user = currentUserOrNull();
        if (user == null) return "redirect:/user/login";
        // ✅ 등록 직후 1회 패스 소비
        if (!consumeManagePassOnce(request)) {
            // 1회 패스가 없다면 정상 재인증 소모 요구
            if (!requireReauthConsume(request)) {
                return redirectToVerifyWithContinue(request);
            }
        }

        setNoCache(response);
        BusinessUser business = businessUserRepository.findByUserId(user.getId()).orElse(null);
        model.addAttribute("business", business);
        return "mypage/cafe_manage";
    }

    /* =========================
       Cafe Register (NOT protected)
       ========================= */
    @GetMapping("/mypage/cafe/register")
    public String showCafeRegister(Principal principal,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   Model model) {
        SiteUser user = currentUserOrNull();
        if (user == null) return "redirect:/user/login";
        if (user.getBusinessUser() == null) return "redirect:/mypage?denied=biz";

        setNoCache(response);
        var biz = businessUserService.getMyBusinessByUsername(principal.getName()).orElse(null);
        model.addAttribute("biz", biz);
        return "mypage/cafe-register";
    }

    @PostMapping("/mypage/cafe/register")
    public String saveCafeRegister(HttpServletRequest request,
                                   @RequestParam String companyName,
                                   @RequestParam String businessNumber,
                                   @RequestParam(required = false) String representativeName,
                                   @RequestParam(required = false) String representativeEmail,
                                   @RequestParam(required = false) String representativePhone,
                                   @RequestParam(required = false) String address,
                                   @RequestParam(required = false) String description,
                                   RedirectAttributes ra) {
        SiteUser user = currentUserOrNull();
        if (user == null) return "redirect:/user/login";
        if (user.getBusinessUser() == null) return "redirect:/mypage?denied=biz";

        // (선택) 기존 CAFE_FLOW_FLAG 필요 시 체크
        // HttpSession s = request.getSession(false);
        // if (s == null || s.getAttribute(CAFE_FLOW_FLAG) == null) {
        //     return "redirect:/mypage/cafe/manage";
        // }

        try {
            cafeManageService.createBusiness(
                    user, companyName, businessNumber,
                    representativeName, representativeEmail, representativePhone,
                    address, description
            );

            // ✅ 등록 성공 → /manage 1회 통과 플래그 세팅
            request.getSession(true).setAttribute(MANAGE_PASS_ONCE, Boolean.TRUE);

            ra.addFlashAttribute("toast", "사업장이 등록되었습니다.");
            return "redirect:/mypage/cafe/manage";

        } catch (DuplicateBusinessNumberException e) {
            ra.addFlashAttribute("error", "이미 사용 중인 사업자등록번호입니다.");
            return "redirect:/mypage/cafe/register";
        }
    }
}
