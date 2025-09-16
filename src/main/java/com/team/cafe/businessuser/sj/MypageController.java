package com.team.cafe.businessuser.sj;

import com.team.cafe.businessuser.sj.owner.cafe.CafeManageService;
import com.team.cafe.businessuser.sj.BusinessUserRepository;
import com.team.cafe.cafeListImg.hj.CafeImage;
import com.team.cafe.cafeListImg.hj.CafeImageService;
import com.team.cafe.list.hj.Cafe;
import com.team.cafe.list.hj.CafeListRepository;
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

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class MypageController {

    /* =========================
       Constants (Session Keys)
       ========================= */
    private static final String REAUTH_TOKEN_KEY = "MYPAGE_REAUTH_TOKEN";
    private static final String CAFE_FLOW_FLAG   = "CAFE_FLOW_OK";
    private static final String MANAGE_PASS_ONCE = "MANAGE_PASS_ONCE";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final BusinessUserRepository businessUserRepository;
    private final CafeImageService cafeImageService;
    private final CafeListRepository cafeListRepository;

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
        String rawPath = req.getRequestURI();
        String pathNoSemi = rawPath.split(";", 2)[0];
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

        if (!ctx.isEmpty() && path.startsWith(ctx)) {
            return path + query;
        }
        return ctx + path + query;
    }

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

    private boolean safeUrlEqualsIgnoreReauthAndCtx(HttpServletRequest req, String a, String b) {
        try {
            String ctx = (req.getContextPath() == null) ? "" : req.getContextPath();

            UrlParts ua = parseAndNormalizeUrl(a, ctx);
            UrlParts ub = parseAndNormalizeUrl(b, ctx);

            if (!Objects.equals(ua.path, ub.path)) return false;
            return Objects.equals(ua.params, ub.params);
        } catch (Exception e) {
            return a.equals(b);
        }
    }

    private static class UrlParts {
        final String path;
        final Map<String, List<String>> params;
        UrlParts(String p, Map<String, List<String>> q) { this.path = p; this.params = q; }
    }

    private UrlParts parseAndNormalizeUrl(String raw, String ctx) {
        String noSemi = raw.split(";", 2)[0];

        String path, qs;
        int q = noSemi.indexOf('?');
        if (q >= 0) { path = noSemi.substring(0, q); qs = noSemi.substring(q + 1); }
        else { path = noSemi; qs = ""; }

        if (path.length() > 1 && path.endsWith("/")) path = path.substring(0, path.length() - 1);
        if (ctx != null && !ctx.isEmpty() && !path.startsWith(ctx)) {
            path = ctx + (path.startsWith("/") ? path : ("/" + path));
        }

        Map<String, List<String>> map = new TreeMap<>();
        if (!qs.isEmpty()) {
            for (String kv : qs.split("&")) {
                if (kv.isBlank()) continue;
                int idx = kv.indexOf('=');
                String k = idx >= 0 ? kv.substring(0, idx) : kv;
                String v = idx >= 0 ? kv.substring(idx + 1) : "";
                k = urlDecode(k);
                if ("reauth".equalsIgnoreCase(k)) continue;
                v = urlDecode(v);
                map.computeIfAbsent(k, _k -> new ArrayList<>()).add(v);
            }
        }
        for (List<String> vs : map.values()) Collections.sort(vs);
        return new UrlParts(path, map);
    }

    private String urlDecode(String s) {
        try {
            return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) { return s; }
    }

    private boolean consumeManagePassOnce(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return false;
        Object flag = s.getAttribute(MANAGE_PASS_ONCE);
        if (!Boolean.TRUE.equals(flag)) return false;
        s.removeAttribute(MANAGE_PASS_ONCE);
        return true;
    }

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

    @GetMapping("/mypage/verify_password")
    public String verifyPasswordPage(@RequestParam(value = "continue", required = false) String cont,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     Model model) {
        SiteUser user = currentUserOrNull();
        if (user == null) return "redirect:/user/login";
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
        return "redirect:" + baseNoReauth + sep + "reauth=" + URLEncoder.encode(nonce, StandardCharsets.UTF_8);
    }

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
    public String cafeManage(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(name = "cafeId", required = false) Long cafeId,
                             Model model) {
        SiteUser user = currentUserOrNull();
        if (user == null) return "redirect:/user/login";

        // 등록 직후 1회 패스 소비 → 없으면 재인증 필요
        if (!consumeManagePassOnce(request)) {
            if (!requireReauthConsume(request)) {
                return redirectToVerifyWithContinue(request);
            }
        }
        setNoCache(response);

        // 사업자 정보
        var business = businessUserRepository.findByUserId(user.getId()).orElse(null);
        model.addAttribute("business", business);

        // 1) 볼 카페 결정
        if (cafeId == null) {
            // (권장) 로그인 사용자의 카페 1개 선택
            cafeId = cafeListRepository.findByBusinessUser_User_Id(user.getId())
                    .map(Cafe::getId)
                    .orElse(null);

            // (보조) 그래도 없으면 가장 최근 카페 1개
            if (cafeId == null) {
                Cafe latest = cafeListRepository.findTopByOrderByIdDesc();
                cafeId = (latest != null) ? latest.getId() : null;
            }
        }

        // 2) 카페/사진 로드
        if (cafeId != null) {
            Cafe cafe = cafeListRepository.findById(cafeId).orElse(null);
            if (cafe != null) {
                var photos = cafeImageService.findAllByCafeId(cafeId);
                model.addAttribute("cafe", cafe);
                model.addAttribute("photos", photos);
                model.addAttribute("photoCount", photos.size());
                model.addAttribute("isRegistered", true);
            } else {
                model.addAttribute("photos", List.of());
                model.addAttribute("photoCount", 0);
                model.addAttribute("isRegistered", false);
            }
        } else {
            model.addAttribute("photos", List.of());
            model.addAttribute("photoCount", 0);
            model.addAttribute("isRegistered", false);
        }

        return "mypage/cafe_manage"; // 템플릿 파일명과 정확히 일치
    }


}
