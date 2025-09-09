package com.team.cafe.businessuser.sj.owner.cafe;

import com.team.cafe.businessuser.sj.*;
import com.team.cafe.user.sjhy.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사업장(카페) 관리 서비스
 * - 사업장 생성(createBusiness)
 * - 프로필 저장(saveProfile)
 *
 * 주의:
 *  - Business 엔티티에 createdAt/updatedAt은 @PrePersist/@PreUpdate로 자동 세팅되도록 구성되어 있어야 합니다.
 *  - BusinessRepository에 findByUserId, existsByUserId, existsByBusinessNumber, findByBusinessNumber 메서드가 필요합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CafeManageService {

    private final BusinessRepository businessRepository;

    /**
     * 사업장 신규 등록
     */
    @Transactional
    public Business createBusiness(
            SiteUser user,
            String companyName,
            String businessNumber,
            String representativeName,
            String representativeEmail,
            String representativePhone,
            String address,
            String description
    ) {
        // 사용자당 1개 제한(원치 않으면 제거)
        if (businessRepository.existsByUserId(user.getId())) {
            throw new IllegalStateException("이미 등록된 사업장이 있습니다.");
        }

        // 사업자번호 중복
        if (businessNumber != null && !businessNumber.isBlank()
                && businessRepository.existsByBusinessNumber(businessNumber)) {
            throw new DuplicateBusinessNumberException("중복 사업자번호: " + businessNumber);
        }

        Business b = new Business();
        b.setUser(user);
        b.setCompanyName(companyName);
        b.setBusinessNumber(businessNumber);
        b.setRepresentativeName(representativeName);
        b.setRepresentativeEmail(representativeEmail);
        b.setRepresentativePhone(representativePhone);
        b.setAddress(address);
        b.setDescription(description);

        return businessRepository.save(b);
    }

    /**
     * 사업장 프로필 저장/수정
     *
     * 주의: Business 엔티티에 website/instagram/phone/wifi/outlet/parking 필드가 없다면
     *       현재 메서드는 해당 파라미터를 "받기만" 하며 사용하지 않습니다(컴파일 에러 방지).
     *       실제로 저장하려면 Business 엔티티에 필드를 추가하고 setter를 호출하도록 확장하세요.
     */
    @Transactional
    public Business saveProfile(
            SiteUser user,
            String companyName,
            String address,
            String phone,        // (엔티티에 없으면 미사용)
            String website,      // (엔티티에 없으면 미사용)
            String instagram,    // (엔티티에 없으면 미사용)
            String description,
            boolean wifi,        // (엔티티에 없으면 미사용)
            boolean outlet,      // (엔티티에 없으면 미사용)
            boolean parking,     // (엔티티에 없으면 미사용)
            String businessNumber,
            String representativeName,
            String representativeEmail
    ) {
        Business biz = businessRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    // 없으면 새로 생성(원치 않으면 예외로 변경)
                    Business nb = new Business();
                    nb.setUser(user);
                    return nb;
                });

        // 사업자번호 중복 체크(자기 자신 제외)
        if (businessNumber != null && !businessNumber.isBlank()) {
            Optional<Business> existing = businessRepository.findByBusinessNumber(businessNumber);
            if (existing.isPresent() && (biz.getId() == null || !existing.get().getId().equals(biz.getId()))) {
                throw new DuplicateBusinessNumberException("중복 사업자번호: " + businessNumber);
            }
        }

        // 기본 정보 반영
        if (companyName != null) biz.setCompanyName(companyName);
        if (address != null) biz.setAddress(address);
        if (description != null) biz.setDescription(description);
        if (representativeName != null) biz.setRepresentativeName(representativeName);
        if (representativeEmail != null) biz.setRepresentativeEmail(representativeEmail);
        if (businessNumber != null) biz.setBusinessNumber(businessNumber);

        // 참고: 아래 값들은 엔티티에 필드가 없다면 주석 해제 전 필드 추가/매핑 필요
        // if (phone != null) biz.setPhone(phone);
        // if (website != null) biz.setWebsite(website);
        // if (instagram != null) biz.setInstagram(instagram);
        // biz.setWifi(wifi);
        // biz.setOutlet(outlet);
        // biz.setParking(parking);

        return businessRepository.save(biz);
    }
}
