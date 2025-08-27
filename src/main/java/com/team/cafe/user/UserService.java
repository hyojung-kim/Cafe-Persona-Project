package com.team.cafe.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SiteUser join (Long userid, String username, String email, String password_hash) {
        SiteUser siteUser = SiteUser.builder()
                .userid(null)
                .username(username)
                .email(email)
                .password_hash(passwordEncoder.encode(password_hash))
                .build();

        return  this.userRepository.save(siteUser);
    }

    @Transactional
    public SiteUser whenSocialLogin(String providerTypeCode, String username) throws Exception {
        SiteUser siteUser = findByUsername(username);

        // 소셜 로그인를 통한 가입시 비번은 없다.
        return join(null, "test", "test@test.com", "1234"); // 최초 로그인 시 딱 한번 실행
    }

    public SiteUser findByUsername(String username) throws Exception {
        Optional<SiteUser> optionalSiteUser = this.userRepository.findByusername(username);

        if (optionalSiteUser.isPresent()) {
            return optionalSiteUser.get();
        } else {
            throw  new RuntimeException("data not found");
        }
    }
}
