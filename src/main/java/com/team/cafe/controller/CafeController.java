package com.team.cafe.controller;

import com.team.cafe.domain.Cafe;
import com.team.cafe.domain.Review;
import com.team.cafe.service.CafeService;
import com.team.cafe.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    private final CafeService cafeService;
    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         Model model) {
        Cafe cafe = cafeService.getCafeOrThrow(id);
        Page<Review> reviews = reviewService.getReviewsByCafe(id, page, size);
        Double avgRating = reviewService.getAverageRating(id);

        model.addAttribute("cafe", cafe);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);
        return "cafe/detail";
    }
}
