//package com.team.cafe.user;
//
//import com.team.cafe.user.sjhy.SiteUser;
//import com.team.cafe.user.sjhy.UserRepository;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/dummy")
//public class SiteUserController_HJ {
//
//    private final UserRepository siteUserRepository;
//
//    /**
//     * 더미 로그인
//     * ex) /dummy/login/1 → userId=1 유저로 로그인
//     */
//    @GetMapping("/login/{id}")
//    public String devLogin(@PathVariable Long id,
//                           HttpServletRequest request,
//                           HttpServletResponse response) {
//        SiteUser user = siteUserRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + id));
//
//        // ROLE_USER 권한 부여
//        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
//
//        // 인증 객체 생성
//        UsernamePasswordAuthenticationToken auth =
//                new UsernamePasswordAuthenticationToken(user, null, authorities);
//
//        // SecurityContext 생성 & 세션에 저장
//        SecurityContext context = SecurityContextHolder.createEmptyContext();
//        context.setAuthentication(auth);
//        SecurityContextHolder.setContext(context);
//
//        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
//
//        return "redirect:/";
//    }
//
//    /**
//     * 더미 로그아웃
//     * ex) /dummy/logout
//     */
//    @GetMapping("/logout")
//    public String devLogout(HttpServletRequest request, HttpServletResponse response) {
//        SecurityContextHolder.clearContext();
//        request.getSession().invalidate();
//        return "redirect:/";
//    }
//}