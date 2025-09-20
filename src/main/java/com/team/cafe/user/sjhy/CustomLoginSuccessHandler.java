package com.team.cafe.user.sjhy;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;


// 일반, 사업자 로그인 구분하기 위한 핸들러
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        String loginType = request.getParameter("loginType");

        // 로그인 페이지와 계정 역할 불일치 시 안내 페이지
        if ("USER".equals(loginType) && roles.contains("ROLE_BUSINESS")) {
            response.sendRedirect("/login/mismatch?target=business");
            return;
        } else if ("BUSINESS".equals(loginType) && roles.contains("ROLE_USER")) {
            response.sendRedirect("/login/mismatch?target=user");
            return;
        }

        // 정상 로그인 시 role 기반 리다이렉트
        if (roles.contains("ROLE_BUSINESS")) {
            response.sendRedirect("/");
        } else {
            response.sendRedirect("/");
        }
    }
}