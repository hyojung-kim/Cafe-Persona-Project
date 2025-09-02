package com.team.cafe.service;

import com.team.cafe.domain.Cafe;
import com.team.cafe.repository.CafeRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@Transactional
public class CafeService {

    private final CafeRepository cafeRepository;

    public CafeService(CafeRepository cafeRepository) {
        this.cafeRepository = cafeRepository;
    }

    // ========================= 생성 =========================
    /**
     * 컨트롤러가 기대하는 시그니처:
     * createCafe(String name, String address, BigDecimal latitude, BigDecimal longitude, String phone, String description)
     * - 현 시점 Cafe 엔티티에 description 필드는 없으므로 파라미터만 수용하고 저장은 생략
     */
    public Cafe createCafe(String name,
                           String address,
                           BigDecimal latitude,
                           BigDecimal longitude,
                           String phone,
                           String description) {
        // --- 입력값 정리/검증 ---
        String nm   = (name == null) ? "" : name.trim();
        String addr = (address == null) ? "" : address.trim();
        String ph   = (phone == null) ? "" : phone.trim();
        // description 은 현재 엔티티에 필드가 없어 저장 생략

        // 엔티티 제약과 일치하도록 검증 (name: 1~120, address: 1~255)
        if (nm.isEmpty() || nm.length() > 120) {
            throw new IllegalArgumentException("카페 이름은 1~120자로 입력하세요.");
        }
        if (addr.isEmpty() || addr.length() > 255) {
            throw new IllegalArgumentException("주소는 1~255자로 입력하세요.");
        }
        if (!ph.isEmpty() && (ph.length() < 3 || ph.length() > 30)) {
            throw new IllegalArgumentException("전화번호는 3~30자 또는 비워둘 수 있습니다.");
        }

        if (latitude != null) {
            double lat = latitude.doubleValue();
            if (lat < -90.0 || lat > 90.0) {
                throw new IllegalArgumentException("위도는 -90 ~ 90 범위여야 합니다.");
            }
        }
        if (longitude != null) {
            double lng = longitude.doubleValue();
            if (lng < -180.0 || lng > 180.0) {
                throw new IllegalArgumentException("경도는 -180 ~ 180 범위여야 합니다.");
            }
        }

        // --- 엔티티 생성 (빌더 없이) ---
        Cafe cafe = new Cafe(nm, addr); // 편의 생성자 사용
        if (!ph.isEmpty()) cafe.setPhone(ph);
        if (latitude  != null) cafe.setLatitude(latitude);
        if (longitude != null) cafe.setLongitude(longitude);
        // categoryCode/active 등은 별도 API에서 수정

        // --- 저장 & 반환 ---
        return cafeRepository.save(cafe);
    }

    // ========================= 조회(읽기 전용) =========================

    /** 활성 카페 목록 페이징 */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Cafe> getActiveCafes(Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable is required");
        return cafeRepository.findByActiveTrue(pageable);
    }

    /** 이름 부분검색 (활성만) */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Cafe> searchActiveByName(String keyword, Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable is required");
        String kw = (keyword == null) ? "" : keyword.trim();
        return cafeRepository.findByActiveTrueAndNameContainingIgnoreCase(kw, pageable);
    }

    /** 카테고리 부분검색 (활성만) */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Cafe> searchActiveByCategory(String categoryCode, Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable is required");
        String cat = (categoryCode == null) ? "" : categoryCode.trim();
        return cafeRepository.findByActiveTrueAndCategoryCodeContainingIgnoreCase(cat, pageable);
    }

    /** 단건 조회 */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Cafe getById(Long cafeId) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        return cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다. id=" + cafeId));
    }

    /** 활성 리뷰 평균 평점 (없으면 0.0) */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public double getActiveAverageRating(Long cafeId) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        var stats = cafeRepository.getActiveStatsByCafeId(cafeId);
        return (stats != null && stats.getAvgRating() != null) ? stats.getAvgRating() : 0.0;
    }

    /** 활성 리뷰 개수 (없으면 0) */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public long getActiveReviewCount(Long cafeId) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        var stats = cafeRepository.getActiveStatsByCafeId(cafeId);
        return (stats != null && stats.getReviewCount() != null) ? stats.getReviewCount() : 0L;
    }

    // ========================= 수정(쓰기 트랜잭션) =========================

    /** 전화번호 변경 */
    public void updatePhone(Long cafeId, String phone) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        Cafe cafe = getById(cafeId);

        String p = (phone == null) ? "" : phone.trim();
        if (p.length() < 3 || p.length() > 30) {
            throw new IllegalArgumentException("전화번호는 3~30자로 입력하세요.");
        }
        cafe.setPhone(p);
        // JPA 변경감지로 저장됨
    }

    /** 주소(텍스트) 변경 */
    public void updateAddress(Long cafeId, String address) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        Cafe cafe = getById(cafeId);

        String addr = (address == null) ? "" : address.trim();
        if (addr.isEmpty() || addr.length() > 255) {
            throw new IllegalArgumentException("주소는 1~255자로 입력하세요.");
        }
        cafe.setAddress(addr);
    }

    /** 카테고리 코드 변경 */
    public void updateCategory(Long cafeId, String categoryCode) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        Cafe cafe = getById(cafeId);

        String code = (categoryCode == null) ? "" : categoryCode.trim();
        if (code.isEmpty() || code.length() > 50) {
            throw new IllegalArgumentException("카테고리 코드는 1~50자로 입력하세요.");
        }
        cafe.setCategoryCode(code);
    }

    /** 활성/비활성 전환 */
    public void updateActive(Long cafeId, boolean active) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        Cafe cafe = getById(cafeId);
        cafe.setActive(active);
    }

    /** 위치(위도/경도) 변경 */
    public void updateLocation(Long cafeId, BigDecimal latitude, BigDecimal longitude) {
        Objects.requireNonNull(cafeId, "cafeId is required");
        Cafe cafe = getById(cafeId);

        if (latitude != null) {
            double lat = latitude.doubleValue();
            if (lat < -90.0 || lat > 90.0) {
                throw new IllegalArgumentException("위도는 -90 ~ 90 범위여야 합니다.");
            }
            cafe.setLatitude(latitude);
        }

        if (longitude != null) {
            double lng = longitude.doubleValue();
            if (lng < -180.0 || lng > 180.0) {
                throw new IllegalArgumentException("경도는 -180 ~ 180 범위여야 합니다.");
            }
            cafe.setLongitude(longitude);
        }
        // JPA 변경감지로 자동 저장
    }
}
