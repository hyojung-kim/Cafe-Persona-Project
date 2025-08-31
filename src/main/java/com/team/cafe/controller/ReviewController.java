package com.team.cafe.controller;

import com.team.cafe.domain.Review;
import com.team.cafe.dto.ReviewCreateRequest;
import com.team.cafe.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.model.IModel;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public String create(@Valid @ModelAttribute ReviewCreateRequest req,
                         @RequestParam(name = "images", required = false)List<MultipartFile> images,
                         Principal principal) throws IOException {
        reviewService.createReview(principal.getName(), req, images);
        return "redirect:/cafes/" + req.cafeId();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/like")
    public String like(@PathVariable Long id, Principal principal,
                       @RequestHeader(value="Referer", required=false) String referer,
                       Model model) {
        try {
            reviewService.toggleLike(principal.getName(), id);
        } catch (IllegalStateException e) {
            return "redirect:" + (referer != null ? referer : "/") + "?alert=" + e.getMessage();
        }
        return "redirect:" + (referer != null ? referer : "/");
    }

    /* AJAX 엔드 포인트를 쓸 경우 위를 아래를 응용해서 수정하도록 하자 */

//    @PreAuthorize("isAuthenticated()")
//    @PostMapping("/{id}/like-toggle")
//    @ResponseBody
//    public ResponseEntity<?> likeToggle(@PathVariable Long id, Principal principal) {
//        long count = reviewService.toggleLike(principal.getName(), id);
//        return ResponseEntity.ok(Map.of("likedCount", count));
//    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/like-toggle")
    @ResponseBody
    public ResponseEntity<?> likeToggle(@PathVariable Long id, Principal principal) {
        long count = reviewService.toggleLike(principal.getName(), id);
        return ResponseEntity.ok(Map.of("likedCount", count));
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Review review = reviewService.getAndIncreaseView(id);
        model.addAttribute("review", review);
        model.addAttribute("likeCount", reviewService.getLikeCount(id));
        return "review/detail";
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        Review review = reviewService.getAndIncreaseView(id); // 조회수 증가 원치 않으면 별도 find 메서드 사용
        if (!review.getAuthor().getUsername().equals(principal.getName())) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }
        model.addAttribute("review", review);
        return "review/edit";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/edit")
    public String editSubmit(@PathVariable Long id,
                             @RequestParam String content,
                             @RequestParam Double rating,
                             Principal principal) {
        Review updated = reviewService.updateReview(principal.getName(), id, content, rating);
        return "redirect:/reviews/" + updated.getId();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        // 삭제 후 카페 상세로 이동
        Review r = reviewService.getAndIncreaseView(id);
        Long cafeId = r.getCafe().getId();
        reviewService.deleteReview(principal.getName(), id);
        return "redirect:/cafes/" + cafeId;
    }
}
