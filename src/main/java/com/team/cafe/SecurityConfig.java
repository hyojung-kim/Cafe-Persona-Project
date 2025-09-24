package com.team.cafe;

import com.team.cafe.user.sjhy.CustomAuthenticationFailureHandler;
//import com.team.cafe.user.sjhy.CustomAuthenticationProvider;
import com.team.cafe.user.sjhy.CustomLoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
//
//    private final CustomAuthenticationProvider customAuthenticationProvider;
//
//    public SecurityConfig(CustomAuthenticationProvider customAuthenticationProvider) {
//        this.customAuthenticationProvider = customAuthenticationProvider;
//    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) 정적 리소스 & 공개 경로 명시 허용
//                .authenticationProvider(customAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",                 // 홈
                                "/cafe/**",          // 카페 목록/상세 등
                                "/user/**",          // 로그인/회원가입 등
                                "/business/**",      // 비지니스
                                "/css/**",           // 정적 CSS
                                "/js/**",            // 정적 JS
                                "/images/**",        // 정적 이미지
                                "/webjars/**",       // webjars (사용 시)
                                "/h2-console/**",    // H2 콘솔
                                "/cafes/**",
                                "/uploads/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/mypage/cafe/photo/**").authenticated()
                        .requestMatchers("/user/login", "/business/login", "/user/signup").permitAll()
                        // USER 전용 페이지
                        .requestMatchers("/user/**").hasRole("USER")
                        // BUSINESS 전용 페이지
                        .requestMatchers("/business/**").hasRole("BUSINESS")

                        .anyRequest().permitAll()   // 현재는 전부 공개 (필요 시 authenticated()로 조정)
                )

                // 2) CSRF: H2 콘솔 및 일부 엔드포인트/정적 리소스는 예외
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/h2-console/**",
                                "/user/signup",
                                "/user/kakao/**",
                                "/signup/check-username",
                                "/signup/check-email",
                                "/signup/check-nickname",
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/user/**", "business/**"
                        )
                )

                // 3) H2 콘솔 frame 허용
                .headers(headers -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
                )

                // 4) 로그인 설정
                .formLogin(form -> form
                        .loginPage("/user/login")          // GET: 로그인 페이지
                        .loginProcessingUrl("/user/login") // POST: 인증 처리
                        .failureHandler(customAuthenticationFailureHandler())
                        .successHandler(customLoginSuccessHandler())
                )

                // 5) 로그아웃 설정 (POST 권장)
                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/cafe/list")
                        .invalidateHttpSession(true)
                );

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CustomLoginSuccessHandler customLoginSuccessHandler() {
        return new CustomLoginSuccessHandler();
    }

    @Bean
    public CustomAuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    // business_user 로 저장되는 role_business를 사용하기 위함
    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        return authorities -> authorities.stream()
                .map(authority -> {
                    String role = authority.getAuthority();
                    // 롤에 'ROLE_' 접두사가 없으면 붙여서 반환
                    if (!role.startsWith("ROLE_")) {
                        return new SimpleGrantedAuthority("ROLE_" + role);
                    }
                    return authority;
                })
                .collect(Collectors.toSet());
    }
}
