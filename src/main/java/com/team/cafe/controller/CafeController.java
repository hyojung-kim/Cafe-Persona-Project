package com.team.cafe.controller;

import com.team.cafe.domain.Cafe;
import com.team.cafe.repository.CafeRepository;
import com.team.cafe.service.CafeService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cafes")
public class CafeController {

    private final CafeService cafeService;
    private final CafeRepository cafeRepository;

    public CafeController(CafeService cafeService,
                          CafeRepository cafeRepository) {
        this.cafeService = cafeService;
        this.cafeRepository = cafeRepository;
    }

    // 목록 + (검색 시) 기존 서비스 경로 유지, 기본 목록은 통계 Projection을 컨트롤러에서 사용
    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "category", required = false) String category,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       Model model) {

        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        String kw = (keyword == null) ? "" : keyword.trim();
        String cat = (category == null) ? "" : category.trim();
        if (kw.length() > 100) kw = kw.substring(0, 100);
        if (cat.length() > 100) cat = cat.substring(0, 100);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (kw.isBlank() && cat.isBlank()) {
            var result = cafeRepository.findActiveCafesWithStats(pageable); // Projection 페이지
            model.addAttribute("page", result);
        } else {
            var result = kw.isBlank()
                    ? cafeService.searchActiveByCategory(cat, pageable)
                    : cafeService.searchActiveByName(kw, pageable);
            model.addAttribute("page", result);
        }

        model.addAttribute("keyword", kw);
        model.addAttribute("category", cat);
        return "cafe/list";
    }

    // 상세
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Cafe cafe = cafeService.getById(id);
        var stats = cafeRepository.getActiveStatsByCafeId(id);
        double avg = (stats != null && stats.getAvgRating() != null) ? stats.getAvgRating() : 0.0;
        long count = (stats != null && stats.getReviewCount() != null) ? stats.getReviewCount() : 0L;

        model.addAttribute("cafe", cafe);
        model.addAttribute("avgRating", avg);
        model.addAttribute("reviewCount", count);
        return "cafe/detail";
    }

    // 생성 폼 (예시 – 템플릿에서 name/address/lat/lng/phone/categoryCode를 입력받는다고 가정)
    @GetMapping("/new")
    public String createForm(Model model) {
        return "cafe/edit"; // 필요 시 템플릿 구성
    }

    // 생성 처리
    @PostMapping
    public String create(@RequestParam String name,
                         @RequestParam String address,
                         @RequestParam(required = false) BigDecimal lat,
                         @RequestParam(required = false) BigDecimal lng,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false, name = "categoryCode") String categoryCode,
                         RedirectAttributes ra) {

        Cafe saved = cafeService.createCafe(
                name, address, lat, lng, phone, categoryCode
        );

        ra.addFlashAttribute("message", "카페가 등록되었습니다.");
        return "redirect:/cafes/" + saved.getId();
    }

    // 속성 수정(예시 라우트 — SecurityConfig에서 POST 보호)
    @PostMapping("/{id}/phone")
    public String updatePhone(@PathVariable Long id,
                              @RequestParam String phone,
                              RedirectAttributes ra) {
        cafeService.updatePhone(id, phone);
        ra.addFlashAttribute("message", "전화번호가 변경되었습니다.");
        return "redirect:/cafes/" + id;
    }

    @PostMapping("/{id}/location")
    public String updateLocation(@PathVariable Long id,
                                 @RequestParam String address,
                                 RedirectAttributes ra) {
        cafeService.updateAddress(id, address);
        ra.addFlashAttribute("message", "주소가 변경되었습니다.");
        return "redirect:/cafes/" + id;
    }

    @PostMapping("/{id}/category")
    public String updateCategory(@PathVariable Long id,
                                 @RequestParam String categoryCode,
                                 RedirectAttributes ra) {
        cafeService.updateCategory(id, categoryCode);
        ra.addFlashAttribute("message", "카테고리가 변경되었습니다.");
        return "redirect:/cafes/" + id;
    }

    @PostMapping("/{id}/active")
    public String updateActive(@PathVariable Long id,
                               @RequestParam boolean active,
                               RedirectAttributes ra) {
        cafeService.updateActive(id, active);
        ra.addFlashAttribute("message", "활성 상태가 변경되었습니다.");
        return "redirect:/cafes/" + id;
    }
}

