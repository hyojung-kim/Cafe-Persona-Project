package com.team.cafe.review.controller;

import com.team.cafe.list.hj.CafeListService;
import com.team.cafe.review.ImageStorageService; // ✅ 업로드→URL 변환 서비스
import com.team.cafe.review.domain.Review;
import com.team.cafe.review.repository.ReviewRepository;
import com.team.cafe.review.service.CurrentUserService;
import com.team.cafe.review.service.ReviewService;
import com.team.cafe.user.sjhy.SiteUser;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;   // ✅ 파일 업로드
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ReviewController {

    private static final int MAX_IMAGES = 5; // ✅ 서버 측에서도 최대 장수 강제

    private final ReviewService reviewService;
    private final CafeListService cafeService;
    private final ReviewRepository reviewRepository;
    private final CurrentUserService currentUserService;
    private final ImageStorageService imageStorageService; // ✅ 주입

    public ReviewController(ReviewService reviewService,
                            CafeListService cafeService,
                            ReviewRepository reviewRepository,
                            CurrentUserService currentUserService,
                            ImageStorageService imageStorageService) {
        this.reviewService = reviewService;
        this.cafeService = cafeService;
        this.reviewRepository = reviewRepository;
        this.currentUserService = currentUserService;
        this.imageStorageService = imageStorageService;
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

    /* ===== 작성 처리 (파일 + URL 모두 지원) ===== */
    @PostMapping("/cafes/{cafeId}/reviews")
    public String create(@PathVariable Long cafeId,
                         @ModelAttribute("form") CreateReviewForm form,
                         BindingResult bindingResult,
                         RedirectAttributes ra,
                         Model model,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images // ✅ 추가
    ) {
        SiteUser me = currentUserService.getCurrentUserOrThrow();
        var cafe = cafeService.getById(cafeId);

        // 기본 검증
        if (form.getContent() == null || form.getContent().trim().length() < 5) {
            bindingResult.reject("content.tooShort", "리뷰 내용은 5자 이상이어야 합니다.");
        }
        if (form.getRating() == null || form.getRating() < 1.0 || form.getRating() > 5.0) {
            bindingResult.reject("rating.range", "별점은 1.0 ~ 5.0 사이여야 합니다.");
        }

        // URL + 파일 개수 합산 최대 5장
        int urlCount = safeSize(form.getImageUrl());
        int fileCount = safeSize(images);
        if (urlCount + fileCount > MAX_IMAGES) {
            bindingResult.reject("images.tooMany", "이미지는 최대 " + MAX_IMAGES + "장까지만 가능합니다.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("cafe", cafe);
            model.addAttribute("mode", "create");
            model.addAttribute("author", me);
            return "review/edit";
        }

        // 1) URL 정리(공백 제거/빈 값 제거)
        List<String> urls = normalizeUrls(form.getImageUrl());

        // 2) 업로드 파일 → URL 변환 (남은 슬롯만큼)
        int remaining = MAX_IMAGES - urls.size();
        if (images != null && remaining > 0) {
            int added = 0;
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) continue;
                if (added >= remaining) break;
                String url = imageStorageService.store(file); // ✅ 파일 저장 → 공개 URL
                urls.add(url);
                added++;
            }
        }

        Review saved = reviewService.createReview(
                cafeId, me, form.getRating(), form.getContent().trim(), urls
        );

        ra.addFlashAttribute("message", "리뷰가 등록되었습니다.");
        return "redirect:/reviews/" + saved.getId();
    }

    /** ✅ AJAX 전용: 리뷰 생성 (파일 + URL 지원, FormData로 전송) */
    @PostMapping(value = "/cafes/{cafeId}/reviews", headers = "X-Requested-With=XMLHttpRequest")
    @ResponseBody
    public ResponseEntity<?> createAjax(@PathVariable Long cafeId,
                                        @ModelAttribute("form") CreateReviewForm form,
                                        BindingResult bindingResult,
                                        @RequestParam(value = "images", required = false) List<MultipartFile> images // ✅ 추가
    ) {
        // 1차 검증
        if (form.getContent() == null || form.getContent().trim().length() < 5) {
            bindingResult.reject("content.tooShort", "리뷰 내용은 5자 이상이어야 합니다.");
        }
        if (form.getRating() == null || form.getRating() < 1.0 || form.getRating() > 5.0) {
            bindingResult.reject("rating.range", "별점은 1.0 ~ 5.0 사이여야 합니다.");
        }
        int urlCount = safeSize(form.getImageUrl());
        int fileCount = safeSize(images);
        if (urlCount + fileCount > MAX_IMAGES) {
            bindingResult.reject("images.tooMany", "이미지는 최대 " + MAX_IMAGES + "장까지만 가능합니다.");
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    java.util.Map.of("ok", false, "message", "유효성 오류", "errors", bindingResult.getAllErrors())
            );
        }

        SiteUser me = currentUserService.getCurrentUserOrThrow();

        // URL 정리 + 파일 업로드→URL
        List<String> urls = normalizeUrls(form.getImageUrl());
        int remaining = MAX_IMAGES - urls.size();
        if (images != null && remaining > 0) {
            int added = 0;
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) continue;
                if (added >= remaining) break;
                String url = imageStorageService.store(file);
                urls.add(url);
                added++;
            }
        }

        Review saved = reviewService.createReview(
                cafeId, me, form.getRating(), form.getContent().trim(), urls
        );

        return ResponseEntity.ok(java.util.Map.of(
                "ok", true,
                "id", saved.getId(),
                "message", "리뷰가 등록되었습니다."
        ));
    }

    /** ✅ 리뷰 섹션 프래그먼트: 카페 상세 하단 리스트만 다시 그릴 때 사용 */
    @GetMapping("/cafe/detail/{id}/reviews/section")
    public String reviewsSection(@PathVariable Long id,
                                 @RequestParam(name = "rpage", defaultValue = "0") int reviewPage,
                                 @RequestParam(name = "rsize", defaultValue = "5") int reviewSize,
                                 Model model) {

        var cafe = cafeService.getById(id);

        double avgRating = cafeService.getActiveAverageRating(id);
        long reviewCount = cafeService.getActiveReviewCount(id);

        var pageable = org.springframework.data.domain.PageRequest.of(
                reviewPage, reviewSize, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        );
        Page<Review> reviews = reviewService.getActiveReviewsByCafeWithAuthorImages(id, pageable);

        model.addAttribute("cafe", cafe);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("reviews", reviews);

        return "cafe/detail :: reviews_section";
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

    /* ===== 수정 처리 (파일 + URL 지원) ===== */
    @PostMapping("/reviews/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("form") CreateReviewForm form,
                         BindingResult bindingResult,
                         RedirectAttributes ra,
                         Model model,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images // ✅ 추가
    ) {
        SiteUser me = currentUserService.getCurrentUserOrThrow();

        if (form.getContent() == null || form.getContent().trim().length() < 5) {
            bindingResult.reject("content.tooShort", "리뷰 내용은 5자 이상이어야 합니다.");
        }
        if (form.getRating() == null || form.getRating() < 1.0 || form.getRating() > 5.0) {
            bindingResult.reject("rating.range", "별점은 1.0 ~ 5.0 사이여야 합니다.");
        }

        // 기존 URL + 새 파일의 합이 5장 이하인지 확인
        int urlCount = safeSize(form.getImageUrl());
        int fileCount = safeSize(images);
        if (urlCount + fileCount > MAX_IMAGES) {
            bindingResult.reject("images.tooMany", "이미지는 최대 " + MAX_IMAGES + "장까지만 가능합니다.");
        }

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + id));

        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("review", review);
            model.addAttribute("cafe", review.getCafe());
            return "review/edit";
        }

        // URL 정리 + 파일 업로드→URL
        List<String> urls = normalizeUrls(form.getImageUrl());
        int remaining = MAX_IMAGES - urls.size();
        if (images != null && remaining > 0) {
            int added = 0;
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) continue;
                if (added >= remaining) break;
                String url = imageStorageService.store(file);
                urls.add(url);
                added++;
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
        @Size(min = 5, message = "리뷰 내용은 5자 이상이어야 합니다.")
        private String content;

        /** 프런트에서 직접 넣는 이미지 URL들 (선택) */
        private List<String> imageUrl = new ArrayList<>();

        public CreateReviewForm() {}

        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public List<String> getImageUrl() { return imageUrl; }
        public void setImageUrl(List<String> imageUrl) { this.imageUrl = imageUrl; }
    }

    // ---------- private helpers ----------
    private static int safeSize(List<?> list) {
        return list == null ? 0 : (int) list.stream().filter(e -> {
            if (e == null) return false;
            if (e instanceof String s) return !s.isBlank();
            if (e instanceof MultipartFile f) return f != null && !f.isEmpty();
            return true;
        }).count();
    }

    private static List<String> normalizeUrls(List<String> src) {
        List<String> out = new ArrayList<>();
        if (src == null) return out;
        for (String s : src) {
            if (s == null) continue;
            String t = s.trim();
            if (!t.isEmpty()) out.add(t);
            if (out.size() >= MAX_IMAGES) break;
        }
        return out;
    }
}
