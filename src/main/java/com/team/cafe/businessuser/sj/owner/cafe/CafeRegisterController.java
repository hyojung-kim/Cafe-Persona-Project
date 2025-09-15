package com.team.cafe.businessuser.sj.owner.cafe;

import com.team.cafe.cafeListImg.hj.CafeImageService;
import com.team.cafe.list.hj.Cafe;
import com.team.cafe.list.hj.CafeListRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/cafe")
public class CafeRegisterController {

    private final CafeListRepository cafeListRepository;
    private final CafeImageService cafeImageService;

    // 폼 열기 (GET)
    @GetMapping("/register")
    public String showCafeRegister(Principal principal,
                                   HttpServletResponse response,
                                   Model model) {
        // 로그인 체크
        if (principal == null) {
            return "redirect:/user/login";
        }

        // 캐시 방지(선택)
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");

        // 폼 바인딩 객체
        model.addAttribute("form", new CafeRegisterRequest());
        return "mypage/cafe-register";
    }

    // 저장 (POST) - 가게 + 사진 함께
    @PostMapping(value = "/register", consumes = "multipart/form-data")
    @Transactional
    public String registerCafe(
            @ModelAttribute("form") CafeRegisterRequest req,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            RedirectAttributes ra,
            Principal principal
    ) {
        if (principal == null) {
            return "redirect:/user/login";
        }

        try {
            // 1) Cafe 저장
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
            cafe.setHitCount(0);

            cafe = cafeListRepository.save(cafe); // ID 확보
            log.info("[CafeRegister] saved cafe id={}", cafe.getId());

            // 2) 이미지 저장
            if (photos != null && !photos.isEmpty()) {
                cafeImageService.saveCafeImages(cafe.getId(), photos);
                log.info("[CafeRegister] saved {} images for cafe {}", photos.size(), cafe.getId());
            }

            ra.addFlashAttribute("toast", "사업장 등록 완료");
            return "redirect:/mypage/cafe/manage?cafeId=" + cafe.getId();

        } catch (IOException e) {
            log.error("Image save failed", e);
            ra.addFlashAttribute("error", "이미지 저장 실패: " + e.getMessage());
            return "redirect:/mypage/cafe/register";
        } catch (Exception e) {
            log.error("Register failed", e);
            ra.addFlashAttribute("error", "등록 중 오류: " + e.getMessage());
            return "redirect:/mypage/cafe/register";
        }
    }

    // 요청 DTO (HTML name과 매칭)
    @Getter
    @Setter
    public static class CafeRegisterRequest {
        private String name;
        private String phoneNum;
        private String siteUrl;
        private String address1;
        private String address2;
        private String district;
        private String city;
        private BigDecimal lat;
        private BigDecimal lng;

        @DateTimeFormat(pattern="HH:mm")
        private LocalTime openTime;

        @DateTimeFormat(pattern="HH:mm")
        private LocalTime closeTime;

        private boolean parkingYn;
    }
}
