package com.team.cafe.review.controller;

import com.team.cafe.list.hj.CafeListService;
import com.team.cafe.review.domain.Review;
import com.team.cafe.review.repository.ReviewRepository;
import com.team.cafe.review.service.CurrentUserService;
import com.team.cafe.review.service.ReviewService;
import com.team.cafe.user.sjhy.SiteUser;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final CafeListService cafeService;
    private final ReviewRepository reviewRepository;
    private final CurrentUserService currentUserService;

    public ReviewController(ReviewService reviewService,
                            CafeListService cafeService,
                            ReviewRepository reviewRepository,
                            CurrentUserService currentUserService) {
        this.reviewService = reviewService;
        this.cafeService = cafeService;
        this.reviewRepository = reviewRepository;
        this.currentUserService = currentUserService;
    }

    /* 폼 백업 객체: 어떤 경로로 들어와도 th:object="${form}" 보장 */
    @ModelAttribute("form")
    public CreateReviewForm formBacking() {
        return new CreateReviewForm();
    }

    /* ===== 예전 리뷰 리스트 URL → 카페 상세로 리다이렉트(리스트 템플릿 제거) ===== */
    @GetMapping("/cafes/{cafeId}/reviews")
    public String redirectListToCafeDetail(@PathVariable Long cafeId,
                                           @RequestParam(defaultValue = "0", name = "page") int page,
                                           @RequestParam(defaultValue = "10", name = "size") int size) {
        return "redirect:/cafe/detail/" + cafeId + "?rpage=" + page + "&rsize=" + size;
    }

    @GetMapping("/cafes/{cafeId}/reviews/section")
    public String redirectSectionToCafeDetail(@PathVariable Long cafeId,
                                              @RequestParam(defaultValue = "0", name = "page") int page,
                                              @RequestParam(defaultValue = "10", name = "size") int size) {
        return "redirect:/cafe/detail/" + cafeId + "?rpage=" + page + "&rsize=" + size;
    }

    /* ===== 리뷰 상세 ===== */
    @GetMapping("/reviews/{id}")
    public String detail(@PathVariable Long id, Model model) {
        reviewService.increaseViewCount(id);
        Review review = reviewRepository.findWithAuthorAndImagesById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + id));
        model.addAttribute("review", review);
        return "review/detail";
    }

    /* ===== 작성 폼 ===== */
    @GetMapping("/cafes/{cafeId}/reviews/new")
    public String createForm(@PathVariable Long cafeId, Model model) {
        SiteUser me = currentUserService.getCurrentUserOrThrow();
        var cafe = cafeService.getById(cafeId);
        model.addAttribute("cafe", cafe);
        model.addAttribute("mode", "create");
        model.addAttribute("author", me);
        return "review/edit";
    }

    /* ===== 작성 처리 ===== */
    @PostMapping("/cafes/{cafeId}/reviews")
    public String create(@PathVariable Long cafeId,
                         @ModelAttribute("form") CreateReviewForm form,
                         BindingResult bindingResult,
                         RedirectAttributes ra,
                         Model model) {
        SiteUser me = currentUserService.getCurrentUserOrThrow();
        var cafe = cafeService.getById(cafeId);

        if (form.getContent() == null || form.getContent().trim().length() < 10) {
            bindingResult.reject("content.tooShort", "리뷰 내용은 10자 이상이어야 합니다.");
        }
        if (form.getRating() == null || form.getRating() < 1.0 || form.getRating() > 5.0) {
            bindingResult.reject("rating.range", "별점은 1.0 ~ 5.0 사이여야 합니다.");
        }
        if (form.getImageUrl() != null && form.getImageUrl().size() > 5) {
            bindingResult.reject("images.tooMany", "이미지는 최대 5장까지만 가능합니다.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("cafe", cafe);
            model.addAttribute("mode", "create");
            model.addAttribute("author", me);
            return "review/edit";
        }

        List<String> urls = new ArrayList<>();
        if (form.getImageUrl() != null) {
            for (String u : form.getImageUrl()) {
                if (u != null && !u.isBlank()) urls.add(u.trim());
            }
        }

        Review saved = reviewService.createReview(
                cafeId, me, form.getRating(), form.getContent().trim(), urls
        );

        ra.addFlashAttribute("message", "리뷰가 등록되었습니다.");
        return "redirect:/reviews/" + saved.getId();
    }

    /* ===== 좋아요/취소 ===== */
    @PostMapping("/reviews/{id}/like")
    public String like(@PathVariable Long id, RedirectAttributes ra) {
        SiteUser me = currentUserService.getCurrentUserOrThrow();
        reviewService.likeReview(id, me);
        ra.addFlashAttribute("message", "좋아요를 눌렀습니다.");
        return "redirect:/reviews/" + id;
    }

    @PostMapping("/reviews/{id}/unlike")
    public String unlike(@PathVariable Long id, RedirectAttributes ra) {
        SiteUser me = currentUserService.getCurrentUserOrThrow();
        reviewService.unlikeReview(id, me);
        ra.addFlashAttribute("message", "좋아요를 취소했습니다.");
        return "redirect:/reviews/" + id;
    }

    /* ===== 수정 폼 ===== */
    @GetMapping("/reviews/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        SiteUser me = currentUserService.getCurrentUserOrThrow();
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + id));

        boolean isAuthor = review.getAuthor() != null && review.getAuthor().getId().equals(me.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(me.getRole()) || "ROLE_ADMIN".equalsIgnoreCase(me.getRole());
        if (!isAuthor && !isAdmin) {
            ra.addFlashAttribute("message", "작성자 또는 관리자만 수정할 수 있습니다.");
            return "redirect:/reviews/" + id;
        }

        CreateReviewForm form = new CreateReviewForm();
        form.setRating(review.getRating());
        form.setContent(review.getContent());
        if (review.getImages() != null) {
            review.getImages().forEach(img -> form.getImageUrl().add(img.getImageUrl()));
        }

        model.addAttribute("mode", "edit");
        model.addAttribute("form", form);
        model.addAttribute("review", review);
        model.addAttribute("cafe", review.getCafe());
        return "review/edit";
    }

    /* ===== 수정 처리 ===== */
    @PostMapping("/reviews/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("form") CreateReviewForm form,
                         BindingResult bindingResult,
                         RedirectAttributes ra,
                         Model model) {
        SiteUser me = currentUserService.getCurrentUserOrThrow();

        if (form.getContent() == null || form.getContent().trim().length() < 10) {
            bindingResult.reject("content.tooShort", "리뷰 내용은 10자 이상이어야 합니다.");
        }
        if (form.getRating() == null || form.getRating() < 1.0 || form.getRating() > 5.0) {
            bindingResult.reject("rating.range", "별점은 1.0 ~ 5.0 사이여야 합니다.");
        }
        if (form.getImageUrl() != null && form.getImageUrl().size() > 5) {
            bindingResult.reject("images.tooMany", "이미지는 최대 5장까지만 가능합니다.");
        }

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + id));

        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("review", review);
            model.addAttribute("cafe", review.getCafe());
            return "review/edit";
        }

        List<String> urls = new ArrayList<>();
        if (form.getImageUrl() != null) {
            for (String u : form.getImageUrl()) {
                if (u != null && !u.isBlank()) urls.add(u.trim());
            }
        }

        reviewService.updateReview(id, me, form.getRating(), form.getContent().trim(), urls);
        ra.addFlashAttribute("message", "리뷰가 수정되었습니다.");
        return "redirect:/reviews/" + id;
    }

    /* ===== 삭제 ===== */
    @PostMapping("/reviews/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        SiteUser me = currentUserService.getCurrentUserOrThrow();

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + id));
        Long cafeId = (review.getCafe() != null && review.getCafe().getId() != null)
                ? Long.valueOf(review.getCafe().getId())
                : null;

        reviewService.deleteReview(id, me);
        ra.addFlashAttribute("message", "리뷰가 삭제되었습니다.");
        return (cafeId != null) ? "redirect:/cafe/detail/" + cafeId : "redirect:/";
    }

    /* ===== 내부 DTO: 자바빈 형태 ===== */
    public static class CreateReviewForm {
        @DecimalMin(value = "1.0", message = "별점은 1.0 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "별점은 5.0 이하여야 합니다.")
        private Double rating;

        @NotBlank(message = "내용을 입력하세요.")
        @Size(min = 10, message = "리뷰 내용은 10자 이상이어야 합니다.")
        private String content;

        private List<String> imageUrl = new ArrayList<>();

        public CreateReviewForm() {}

        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public List<String> getImageUrl() { return imageUrl; }
        public void setImageUrl(List<String> imageUrl) { this.imageUrl = imageUrl; }
    }
}
