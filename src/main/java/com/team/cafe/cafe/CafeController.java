package com.team.cafe.cafe;


import com.team.cafe.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cafes")
public class CafeController {

    private final CafeRepository cafeRepository;
    private final ReviewService reviewService;

    /** 카페 상세 + 하단 리뷰 리스트(페이지네이션) */
    @GetMapping("/{cafeId}")
    public String detail(@PathVariable Long cafeId,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size,
                         Model model) {
        Cafe cafe = cafeRepository.findById(cafeId).orElseThrow();
        model.addAttribute("cafe", cafe);
        model.addAttribute("reviews", reviewService.listByCafe(cafeId, page, size));
        return "cafe/detail";
    }

    /** 리뷰 리스트 페이지(더보기 경로) */
    @GetMapping("/{cafeId}/reviews")
    public String reviews(@PathVariable Long cafeId,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        Cafe cafe = cafeRepository.findById(cafeId).orElseThrow();
        model.addAttribute("cafe", cafe);
        model.addAttribute("reviews", reviewService.listByCafe(cafeId, page, size));
        return "review/list";
    }
}
