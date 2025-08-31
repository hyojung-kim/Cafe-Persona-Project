package com.team.cafe.config;
// config 패키지 → 보안(Security) 관련 설정 파일을 모아두는 공간


import org.springframework.context.annotation.Bean;
// @Bean: 스프링 컨테이너에 객체를 등록해서 다른 곳에서 주입(사용)할 수 있게 함
import org.springframework.context.annotation.Configuration;
// @Configuration: 이 클래스가 설정 클래스임을 알림
import org.springframework.security.config.Customizer;
// 보안 설정을 기본값(Customizer.withDefaults())으로 빠르게 적용할 때 사용
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// @PreAuthorize, @PostAuthorize 같은 메서드 단위 보안 활성화
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// HttpSecurity: 웹 보안 설정을 세부적으로 정의할 때 사용
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// 스프링 시큐리티를 웹 애플리케이션에서 활성화하는 어노테이션 (Spring Boot 3.x에서는 생략해도 됨)
import org.springframework.security.core.userdetails.User;
// User 객체 빌더
import org.springframework.security.core.userdetails.UserDetailsService;
// 사용자 계정 정보를 불러오는 서비스 인터페이스
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// 비밀번호를 안전하게 암호화하는 클래스 (BCrypt 알고리즘)
import org.springframework.security.crypto.password.PasswordEncoder;
// 암호화/검증을 위한 인터페이스
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
// 메모리 내에 계정을 저장하는 UserDetailsService 구현체 (테스트/데모용)
import org.springframework.security.web.SecurityFilterChain;
// 요청을 필터링하는 시큐리티 체인 객체
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
// 브라우저의 frame 옵션을 제어하는 보안 헤더 (H2 콘솔에서 iframe 허용)
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
// 요청 URL 패턴을 매칭할 때 사용하는 도우미 클래스


/**
 * SecurityConfig 클래스
 * - 웹 애플리케이션의 보안(로그인, 권한, 접근 제한)을 담당하는 설정 클래스
 */
@Configuration
@EnableMethodSecurity
// @EnableMethodSecurity: @PreAuthorize 같은 메서드 단위 보안 활성화
public class SecurityConfig {

    /**
     * SecurityFilterChain 설정 메서드
     * - 어떤 요청을 허용할지/인증이 필요한지 정의
     * - 로그인/로그아웃 처리 방식을 정의
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF(사이트 간 요청 위조) 보안 기능 → 기본적으로 활성화
                // /h2-console/** 경로만 예외 처리 (개발 편의용)
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))

                // H2 콘솔 같은 경우 iframe으로 열리기 때문에 sameOrigin 허용 필요
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))

                // 요청 경로별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스(css, js, 이미지) 및 h2-console → 누구나 접근 가능
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/uploads/**", "/h2-console/**").permitAll()
                        // 카페 조회 페이지는 전체 허용
                        .requestMatchers("/cafes/**").permitAll()
                        // 리뷰 조회는 전체 허용, 단 POST 같은 민감한 작업은 @PreAuthorize로 제한
                        .requestMatchers("/reviews/**").permitAll()
                        // 나머지 모든 요청은 인증 필요 (로그인해야 접근 가능)
                        .anyRequest().authenticated()
                )

                // 기본 로그인 폼 사용 (커스터마이징 가능)
                .formLogin(Customizer.withDefaults())

                // 기본 로그아웃 기능 사용
                .logout(Customizer.withDefaults());

        return http.build();
    }

    /**
     * 데모용 계정 관리 (InMemoryUserDetailsManager)
     * - DB 대신 메모리에 임시로 사용자 계정을 저장
     * - 실제 서비스에서는 DB 기반 UserDetailsService로 교체해야 함
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        InMemoryUserDetailsManager m = new InMemoryUserDetailsManager();
        // user1 계정 (비밀번호는 "pass1", BCrypt로 암호화됨)
        m.createUser(User.withUsername("user1")
                .password(encoder.encode("pass1"))
                .roles("USER")
                .build());
        // user2 계정 (비밀번호는 "pass2")
        m.createUser(User.withUsername("user2")
                .password(encoder.encode("pass2"))
                .roles("USER")
                .build());
        return m;
    }

    /**
     * 비밀번호 암호화기(PasswordEncoder)
     * - 사용자 비밀번호를 평문 그대로 저장하면 위험하므로 반드시 암호화
     * - BCrypt는 보안에 널리 쓰이는 해시 함수
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}