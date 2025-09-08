package com.team.cafe.businessuser.sj;

import com.team.cafe.user.sjhy.SiteUser;
import com.team.cafe.user.sjhy.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BusinessUserService {

    private final BusinessUserRepository businessUserRepository;
    private final UserRepository siteUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public BusinessUser register(BusinessUserDto dto) {
        // 1. 일반 회원 정보 생성
        SiteUser user = new SiteUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname());
        user.setCreateDate(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole("BUSINESS_USER");

        // SiteUser 저장
        siteUserRepository.save(user);

        // 2. BusinessUser 정보 생성
        BusinessUser businessUser = new BusinessUser();
        businessUser.setUser(user);
        businessUser.setCompanyName(dto.getCompanyName());
        businessUser.setBusinessNumber(dto.getBusinessNumber());
        businessUser.setRepresentativeName(dto.getRepresentativeName());
        businessUser.setRepresentativePhone(dto.getRepresentativePhone());
        businessUser.setRepresentativeEmail(dto.getRepresentativeEmail());
        businessUser.setAddress(dto.getAddress());
        businessUser.setStatus("pending");

        return businessUserRepository.save(businessUser);
    }

    // -------------------- 중복 체크 --------------------

    // 아이디 존재 여부 (SiteUser 기준)
    public boolean existsByUsername(String username) {
        return siteUserRepository.existsByUsername(username);
    }

    // 이메일 존재 여부 (SiteUser 기준)
    public boolean existsByEmail(String email) {
        return siteUserRepository.existsByEmail(email);
    }

    // 닉네임 존재 여부 (SiteUser 기준)
    public boolean existsByNickname(String nickname) {
        return siteUserRepository.existsByNickname(nickname);
    }

    // 사업자번호 존재 여부 (BusinessUser 기준)
    public boolean existsByBusinessNumber(String businessNumber) {
        return businessUserRepository.existsByBusinessNumber(businessNumber);
    }
}
