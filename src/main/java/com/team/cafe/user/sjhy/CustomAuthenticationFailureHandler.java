package com.team.cafe.user.sjhy;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String loginType = request.getParameter("loginType");
        String errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";
        // URL 인코딩을 적용한 메시지 변수 사용
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString());

        // 요청에 loginType이 BUSINESS로 명시된 경우 사업자 로그인 페이지로 리다이렉션
        if ("BUSINESS".equals(loginType)) {
            response.sendRedirect("/business/login?error&message=" + encodedMessage);
        } else {
            // 그 외의 경우 일반 사용자 로그인 페이지로 리다이렉션
            response.sendRedirect("/user/login?error&message=" + encodedMessage);
        }
    }
}