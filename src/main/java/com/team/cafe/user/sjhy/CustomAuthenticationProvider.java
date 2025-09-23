//package com.team.cafe.user.sjhy;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//@Component
//public class CustomAuthenticationProvider implements AuthenticationProvider {
//
//    private final UserDetailsService userDetailsService;
//    private final PasswordEncoder passwordEncoder;
//
//    public CustomAuthenticationProvider(UserDetailsService userDetailsService,
//                                        PasswordEncoder passwordEncoder) {
//        this.userDetailsService = userDetailsService;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        String username = authentication.getName();
//        String rawPassword = authentication.getCredentials().toString();
//
//        // loginType 꺼내오기
//        HttpServletRequest request =
//                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        String loginType = request.getParameter("loginType");
//
//        // 사용자 조회
//        UserDetails user = userDetailsService.loadUserByUsername(username);
//
//        // 비밀번호 체크
//        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
//            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
//        }
//
//        // role vs loginType 체크
//        boolean isBusiness = user.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));
//        boolean isUser = user.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
//
//        if ("USER".equals(loginType) && isBusiness) {
//            throw new BadCredentialsException("사업자 계정은 일반 로그인 불가");
//        }
//        if ("BUSINESS".equals(loginType) && isUser) {
//            throw new BadCredentialsException("일반 계정은 사업자 로그인 불가");
//        }
//
//        // 성공 시 Authentication 리턴
//        return new UsernamePasswordAuthenticationToken(user, rawPassword, user.getAuthorities());
//    }
//
//    @Override
//    public boolean supports(Class<?> authentication) {
//        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
//    }
//}
