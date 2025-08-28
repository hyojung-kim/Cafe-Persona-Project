package com.team.cafe.review;

import com.team.cafe.domain.ReviewImage;
import com.team.cafe.domain.SiteUser;
import com.team.cafe.service.ReviewService;
import com.team.cafe.user.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final SiteUserRepository siteUserRepository;

    /** 리뷰 상세 (조회수 증가) */
    @GetMapping("/reviews/{id}")
    public String detail(@PathVariable Long id, Model model) {
        reviewService.increaseView(id);
        Review review = reviewService.getDetail(id);
        model.addAttribute("review", review);
        // 이미지 목록 전달 (템플릿에서 썸네일/슬라이드에 사용)
        // ※ Review ↔ ReviewImage 연관을 직접 매핑하지 않았으므로, 리포지토리로 조회해서 전달하는 방식
        //   (원한다면 Review에 @OneToMany(mappedBy="review") 추가 후 EntityGraph로 최적화 가능)
        List<ReviewImage> images = reviewImageRepository.findByReview_IdOrderBySortOrderAsc(id);
        model.addAttribute("images", images);
        return "review/detail";
    }

    /** 리뷰 등록 (회원만) */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cafes/{cafeId}/reviews")
    public String create(@PathVariable Long cafeId,
                         @RequestParam double rating,
                         @RequestParam String content,
                         @RequestParam(required = false, name = "images") MultipartFile[] images,
                         Principal principal) {
        SiteUser me = siteUserRepository.findByUsername(principal.getName()).orElseThrow();
        List<MultipartFile> imgList = images == null ? List.of() : Arrays.asList(images);
        reviewService.create(cafeId, me.getId(), rating, content, imgList);
        return "redirect:/cafes/" + cafeId;
    }

    /** 리뷰 수정 (작성자만) */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reviews/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam double rating,
                       @RequestParam String content,
                       Principal principal) {
        SiteUser me = siteUserRepository.findByUsername(principal.getName()).orElseThrow();
        reviewService.update(id, me.getId(), rating, content, List.of());
        return "redirect:/reviews/" + id;
    }

    /** 삭제 요청 (작성자만) */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reviews/{id}/delete-request")
    public String deleteRequest(@PathVariable Long id, Principal principal) {
        SiteUser me = siteUserRepository.findByUsername(principal.getName()).orElseThrow();
        reviewService.requestDeletion(id, me.getId());
        return "redirect:/reviews/" + id;
    }

    /** 좋아요 토글 (회원만, 본인 리뷰 불가) — JSON 반환으로 프론트에서 alert 처리 */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reviews/{id}/like")
    @ResponseBody
    public ResponseEntity<?> like(@PathVariable Long id, Principal principal) {
        SiteUser me = siteUserRepository.findByUsername(principal.getName()).orElseThrow();
        try {
            int count = reviewService.toggleLike(id, me.getId());
            return ResponseEntity.ok(new LikeResponse(true, count));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new LikeResponse(false, -1, e.getMessage()));
        }
    }

    /** 신고 */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reviews/{id}/report")
    public String report(@PathVariable Long id, @RequestParam String reason, Principal principal) {
        SiteUser me = siteUserRepository.findByUsername(principal.getName()).orElseThrow();
        reviewService.report(id, me.getId(), reason);
        return "redirect:/reviews/" + id;
    }

    private record LikeResponse(boolean ok, int likeCount, String message) {
        public LikeResponse(boolean ok, int likeCount) { this(ok, likeCount, null); }
    }
}
