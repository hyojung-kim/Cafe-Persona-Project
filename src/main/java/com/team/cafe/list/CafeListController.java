package com.team.cafe.list;

import com.team.cafe.like.LikeService;
import com.team.cafe.review.domain.Review;
import com.team.cafe.review.service.ReviewService;
import com.team.cafe.user.sjhy.SiteUser;
import com.team.cafe.user.sjhy.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequestMapping("/cafe")
@RequiredArgsConstructor
@Controller
public class CafeListController {

    private final CafeListService cafeListService;
    private final UserService userService;
    private final LikeService likeService;
    private final ReviewService reviewService;

    /** 카페 리스트 */
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "3") int size,
                       @RequestParam(required = false) String kw,
                       @RequestParam(defaultValue = "createdAt") String sort,
                       @RequestParam(defaultValue = "desc") String dir,
                       @RequestParam(required = false) Boolean parking,
                       @RequestParam(required = false) Boolean openNow,
                       Model model) {

        var paging = cafeListService.getCafes(kw, page, size, sort, dir, parking, openNow);

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("parking", parking);
        model.addAttribute("openNow", openNow);
        return "cafe/cafe_list";
    }

    /** 카페 상세 + 같은 페이지에서 리뷰 작성/리스트 */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(name = "rpage", defaultValue = "0") int reviewPage,
                         @RequestParam(name = "rsize", defaultValue = "5") int reviewSize,
                         Principal principal,
                         Model model) {

        Cafe cafe = cafeListService.getById(id);

        // 로그인 사용자
        SiteUser loginUser = null;
        if (principal != null) {
            loginUser = userService.getUser(principal.getName());
        }

        boolean liked = false;
        if (loginUser != null) {
            liked = likeService.isLiked(id, loginUser.getId());
        }

        long likeCount = likeService.getLikeCount(id);
        boolean openNow = cafeListService.isOpenNow(cafe);

        // 상단 배지용 통계
        double avgRating = cafeListService.getActiveAverageRating(id);
        long reviewCount = cafeListService.getActiveReviewCount(id);

        // 리뷰 페이지 (작성자/이미지 N+1 방지 메서드 사용)
        Pageable pageable = PageRequest.of(reviewPage, reviewSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewService.getActiveReviewsByCafeWithAuthorImages(id, pageable);

        model.addAttribute("cafe", cafe);
        model.addAttribute("liked", liked);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("openNow", openNow);

        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviewCount", reviewCount);

        // 템플릿에서 ${reviews}로 사용
        model.addAttribute("reviews", reviews);

        // 분리한 템플릿 경로와 일치
        return "cafe/cafe_detail";
    }

    /** 좋아요 토글 */
    @PostMapping("/detail/{id}/like")
    public String toggleLike(@PathVariable Long id, Principal principal) {
        if (principal == null) return "redirect:/user/login";
        SiteUser user = userService.getUser(principal.getName());
        likeService.toggle(id, user.getId());
        return "redirect:/cafe/detail/" + id;
    }
}
