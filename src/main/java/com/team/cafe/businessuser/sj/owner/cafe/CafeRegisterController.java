package com.team.cafe.businessuser.sj.owner.cafe;

import com.team.cafe.cafeListImg.hj.CafeImageService;
import com.team.cafe.list.hj.Cafe;
import com.team.cafe.list.hj.CafeListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

// 예: CafeRegisterController.java
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/cafe")
public class CafeRegisterController {

    private final CafeListRepository cafeListRepository; // Cafe JPA
    private final CafeImageService cafeImageService;     // 이미 구현됨

    @PostMapping("/register")
    @Transactional
    public String registerCafe(
            @ModelAttribute CafeRegisterRequest req,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            RedirectAttributes ra
    ) {
        // 1) Cafe 엔티티 생성/저장
        Cafe cafe = new Cafe();
        cafe.setName(req.getName());
        cafe.setPhoneNum(req.getPhoneNum());
        cafe.setSiteUrl(req.getSiteUrl());
        cafe.setAddress1(req.getAddress1());
        cafe.setAddress2(req.getAddress2());
        cafe.setDistrict(req.getDistrict());
        cafe.setCity(req.getCity());
        cafe.setLat(req.getLat());
        cafe.setLng(req.getLng());
        cafe.setOpenTime(req.getOpenTime());
        cafe.setCloseTime(req.getCloseTime());
        cafe.setParkingYn(req.isParkingYn());
        cafe.setHitCount(0); // 초기값

        cafe = cafeListRepository.save(cafe); // ID 획득

        // 2) 이미지 저장 (기존 서비스 재사용)
        if (photos != null && !photos.isEmpty()) {
            try {
                cafeImageService.saveCafeImages(cafe.getId(), photos);
            } catch (IOException e) {
                ra.addFlashAttribute("error", "이미지 저장 실패: " + e.getMessage());
                // 필요시 로깅
            }
        }

        ra.addFlashAttribute("toast", "사업장 등록 완료");
        return "redirect:/mypage/cafe/manage?cafeId=" + cafe.getId();
    }
}

