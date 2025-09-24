package com.team.cafe.user.sjhy;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;


// 일반, 사업자 로그인 구분하기 위한 핸들러
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomLoginSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {


        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        String loginType = request.getParameter("loginType");

        // 인증된 사용자의 실제 역할(roles) 가져오기
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isUser = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        boolean isBusiness = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));

        // 1. 일반 로그인 페이지에서 사업자 계정으로 로그인 (loginType=null, 실제 롤: ROLE_BUSINESS)
        if ("USER".equals(loginType) && isBusiness) {
            request.getSession().invalidate(); // 세션 무효화
            response.sendRedirect("/user/login?mismatch=business");
            return;
        }

        // 2. 사업자 로그인 페이지에서 일반 계정으로 로그인 (loginType=BUSINESS, 실제 롤: ROLE_USER)
        if ("BUSINESS".equals(loginType) && isUser) {
            request.getSession().invalidate();
            response.sendRedirect("/business/login?mismatch=user");
            return;
        }

        // 정상 로그인 시 role 기반 리다이렉트(임시)
        if (isUser) {
            response.sendRedirect("/main");
        } else {
            response.sendRedirect("/cafe/list");
        }
        logger.info("로그인 성공! 로그인 타입: {}, 실제 롤: {}", loginType, roles);
    }
}