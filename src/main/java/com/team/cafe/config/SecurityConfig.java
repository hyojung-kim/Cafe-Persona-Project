package com.team.cafe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CSRF: 기본 활성 (템플릿에서 _csrf 사용 중)
                // ※ 예외가 필요하면 ignoringRequestMatchers(...)에 "명시적인" 패턴을 넣어주세요.
                .csrf(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 허용
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**", "/webjars/**"
                        ).permitAll()

                        // 공개 GET 페이지 (목록/상세)
                        .requestMatchers(
                                "/", "/cafes", "/cafes/**",   // GET 목록/상세
                                "/reviews/**"                 // 리뷰 상세 GET
                        ).permitAll()

                        // 변경 행위는 인증 필요 (POST/PUT/PATCH/DELETE 패턴 기준)
                        .requestMatchers(
                                // 리뷰 작성/좋아요/수정/삭제
                                "/cafes/*/reviews",      // POST create
                                "/reviews/*/like",       // POST like
                                "/reviews/*/unlike",     // POST unlike
                                "/reviews/*",            // POST update
                                "/reviews/*/delete",     // POST delete

                                // 카페 속성 수정 (POST 등)
                                "/cafes/*/phone",
                                "/cafes/*/location",
                                "/cafes/*/category",
                                "/cafes/*/active"
                        ).authenticated()

                        // 나머지는 인증
                        .anyRequest().authenticated()
                )

                // 기본 로그인/로그아웃
                .formLogin(Customizer.withDefaults())
                .logout(Customizer.withDefaults());

        return http.build();
    }
}
