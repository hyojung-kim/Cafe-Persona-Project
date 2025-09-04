package com.team.cafe;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) 정적 리소스 & 공개 경로 명시 허용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",                 // 홈
                                "/cafe/**",          // 카페 목록/상세 등
                                "/user/**",          // 로그인/회원가입 등
                                "/css/**",           // 정적 CSS
                                "/js/**",            // 정적 JS
                                "/images/**",        // 정적 이미지
                                "/webjars/**",       // webjars (사용 시)
                                "/h2-console/**"     // H2 콘솔
                        ).permitAll()
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
                                "/css/**", "/js/**", "/images/**", "/webjars/**"
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
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/user/login?error")
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
}
