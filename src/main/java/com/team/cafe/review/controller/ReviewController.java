package com.team.cafe.review.controller;

import com.team.cafe.list.CafeListService;
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

    /* ========================= (변경) 리뷰 리스트/섹션 → 카페 디테일로 리다이렉트 ========================= */

    /** 예전 경로 호환: 이제 별도 리스트 화면은 없고 카페 상세 내에서 리뷰를 보여줌 */
    @GetMapping("/cafes/{cafeId}/reviews")
    public String redirectListToCafeDetail(@PathVariable Long cafeId,
                                           @RequestParam(defaultValue = "0", name = "page") int page,
                                           @RequestParam(defaultValue = "10", name = "size") int size) {
        // rpage/rsize 쿼리로 카페 상세에서 같은 위치로 이동
        return "redirect:/cafe/detail/" + cafeId + "?rpage=" + page + "&rsize=" + size;
    }

    /** 예전 섹션 프래그먼트 엔드포인트도 모두 카페 상세로 보냄 */
    @GetMapping("/cafes/{cafeId}/reviews/section")
    public String redirectSectionToCafeDetail(@PathVariable Long cafeId,
                                              @RequestParam(defaultValue = "0", name = "page") int page,
                                              @RequestParam(defaultValue = "10", name = "size") int size) {
        return "redirect:/cafe/detail/" + cafeId + "?rpage=" + page + "&rsize=" + size;
    }

    /* ========================= 리뷰 상세 ========================= */
    @GetMapping("/reviews/{id}")
    public String detail(@PathVariable Long id, Model model) {
        reviewService.increaseViewCount(id);

        Review review = reviewRepository.findWithAuthorAndImagesById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + id));

        model.addAttribute("review", review);
        return "review/detail";
    }

    /* ========================= 작성 폼 (로그인 필요) ========================= */
    @GetMapping("/cafes/{cafeId}/reviews/new")
    public String createForm(@PathVariable Long cafeId, Model model) {
        SiteUser me = currentUserService.getCurrentUserOrThrow();

        var cafe = cafeService.getById(cafeId);
        model.addAttribute("cafe", cafe);
        model.addAttribute("mode", "create");
        model.addAttribute("form", new CreateReviewForm());
        model.addAttribute("author", me);
        return "review/edit";
    }

    /* ========================= 작성 처리 (로그인 필요) ========================= */
    @PostMapping("/cafes/{cafeId}/reviews")
    public String create(@PathVariable Long cafeId,
                         @ModelAttribute("form") CreateReviewForm form,
                         BindingResult bindingResult,
                         RedirectAttributes ra,
                         Model model) {
        SiteUser me = currentUserService.getCurrentUserOrThrow();
        var cafe = cafeService.getById(cafeId);

        // 1차 검증
        if (form.content == null || form.content.trim().length() < 50) {
            bindingResult.reject("content.tooShort", "리뷰 내용은 50자 이상이어야 합니다.");
        }
        if (form.rating == null || form.rating < 1.0 || form.rating > 5.0) {
            bindingResult.reject("rating.range", "별점은 1.0 ~ 5.0 사이여야 합니다.");
        }
        if (form.imageUrl != null && form.imageUrl.size() > 5) {
            bindingResult.reject("images.tooMany", "이미지는 최대 5장까지만 가능합니다.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("cafe", cafe);
            model.addAttribute("mode", "create");
            model.addAttribute("author", me);
            return "review/edit";
        }

        // 빈/공백 URL 제거
        List<String> urls = new ArrayList<>();
        if (form.imageUrl != null) {
            for (String u : form.imageUrl) {
                if (u != null && !u.isBlank()) urls.add(u.trim());
            }
        }

        Review saved = reviewService.createReview(
                cafeId, me, form.rating, form.content.trim(), urls
        );

        ra.addFlashAttribute("message", "리뷰가 등록되었습니다.");
        return "redirect:/reviews/" + saved.getId();
    }

    /* ========================= 좋아요/취소 (로그인 필요) ========================= */
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

    /* ========================= 수정 폼 (로그인 + 권한) ========================= */
    @GetMapping("/reviews/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        SiteUser me = currentUserService.getCurrentUserOrThrow();

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + id));

        boolean isAuthor = review.getAuthor() != null && review.getAuthor().getId().equals(me.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(me.getRole())
                || "ROLE_ADMIN".equalsIgnoreCase(me.getRole());
        if (!isAuthor && !isAdmin) {
            ra.addFlashAttribute("message", "작성자 또는 관리자만 수정할 수 있습니다.");
            return "redirect:/reviews/" + id;
        }

        CreateReviewForm form = new CreateReviewForm();
        form.rating = review.getRating();
        form.content = review.getContent();
        if (review.getImages() != null) {
            review.getImages().forEach(img -> form.imageUrl.add(img.getImageUrl()));
        }

        model.addAttribute("mode", "edit");
        model.addAttribute("form", form);
        model.addAttribute("review", review);
        model.addAttribute("cafe", review.getCafe());
        return "review/edit";
    }

    /* ========================= 수정 처리 (로그인 + 권한) ========================= */
    @PostMapping("/reviews/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("form") CreateReviewForm form,
                         BindingResult bindingResult,
                         RedirectAttributes ra,
                         Model model) {
        SiteUser me = currentUserService.getCurrentUserOrThrow();

        // 1차 검증
        if (form.content == null || form.content.trim().length() < 50) {
            bindingResult.reject("content.tooShort", "리뷰 내용은 50자 이상이어야 합니다.");
        }
        if (form.rating == null || form.rating < 1.0 || form.rating > 5.0) {
            bindingResult.reject("rating.range", "별점은 1.0 ~ 5.0 사이여야 합니다.");
        }
        if (form.imageUrl != null && form.imageUrl.size() > 5) {
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

        // 빈/공백 URL 제거
        List<String> urls = new ArrayList<>();
        if (form.imageUrl != null) {
            for (String u : form.imageUrl) {
                if (u != null && !u.isBlank()) urls.add(u.trim());
            }
        }

        reviewService.updateReview(id, me, form.rating, form.content.trim(), urls);

        ra.addFlashAttribute("message", "리뷰가 수정되었습니다.");
        return "redirect:/reviews/" + id;
    }

    /* ========================= 삭제 (로그인 + 권한) ========================= */
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

    /* ========================= 내부 DTO ========================= */
    public static class CreateReviewForm {
        @DecimalMin(value = "1.0", message = "별점은 1.0 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "별점은 5.0 이하여야 합니다.")
        private Double rating;

        @NotBlank(message = "내용을 입력하세요.")
        @Size(min = 50, message = "리뷰 내용은 50자 이상이어야 합니다.")
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

    // ReviewController 내부에 추가
    // 폼업 객체 보장
    @ModelAttribute("form")
    public CreateReviewForm formBacking() {
        return new CreateReviewForm();
    }

}
