package com.team.cafe.list;

import com.team.cafe.like.LikeService;
import com.team.cafe.user.sjhy.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/cafe")
@RequiredArgsConstructor
@Controller
public class CafeListController {
    //더미용으로 개발한 @AuthenticationPrincipal SiteUser user 사용 추후에 제거해야 합니다 !!!
    private final CafeListService cafeListService;
    private final LikeService likeService;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "3") int size,
                       @RequestParam(required = false) String kw,
                       @RequestParam(defaultValue = "createdAt") String sort,
                       @RequestParam(defaultValue = "desc") String dir,
                       @RequestParam(required = false) Boolean parking,  // true면 가능만
                       @RequestParam(required = false) Boolean openNow,  // true면 영업중만
                       Model model,
                       @AuthenticationPrincipal SiteUser user) {
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


    @GetMapping("detail/{id}")
    public String detail(@PathVariable Integer id,
                         @AuthenticationPrincipal SiteUser user,
                         Model model) {
        Cafe cafe = cafeListService.getById(id);

        boolean liked = false;
        if (user != null) {
            // 빠른 체크: existsBy... 좋아요가 눌러져있는지 확인
            liked = likeService.isLiked(id, user.getId());
            // (위 메서드 안 쓰려면) liked = cafe.getLikedUsers().contains(user);
        }
        long likeCount = likeService.getLikeCount(id); // 좋아요 수

        boolean openNow = cafeListService.isOpenNow(cafe); //영업상태

        model.addAttribute("cafe", cafe);
        model.addAttribute("liked", liked);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("openNow", openNow);
        return "cafe/cafe_detail";
    }

    //@PreAuthorize("isAuthenticated()") 써야할까 고민 중
    @PostMapping("/detail/{id}/like")
    public String toggleLike(@PathVariable Integer id,
                             @AuthenticationPrincipal SiteUser user) {
        if (user == null) return "redirect:/dummy/login/1?next=/cafe/" + id; // 로컬 편의
        likeService.toggle(id, user.getId());
        return "redirect:/cafe/detail/" + id; // 상세로 복귀
    }



}
