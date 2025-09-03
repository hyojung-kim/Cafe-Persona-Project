package com.team.cafe.list;

import com.team.cafe.like.LikeService;
import com.team.cafe.user.sjhy.SiteUser;
import com.team.cafe.user.sjhy.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequestMapping("/cafe")
@RequiredArgsConstructor
@Controller
public class CafeListController {
    //더미용으로 개발한 @AuthenticationPrincipal SiteUser user 사용 추후에 제거해야 합니다 !!!
    private final CafeListService cafeListService;
    private final UserService userService;
    private final LikeService likeService;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "3") int size,
                       @RequestParam(required = false) String kw,
                       @RequestParam(defaultValue = "createdAt") String sort,
                       @RequestParam(defaultValue = "desc") String dir,
                       @RequestParam(required = false) Boolean parking,  // true면 가능만
                       @RequestParam(required = false) Boolean openNow,  // true면 영업중만
                       Model model
                       ) {
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
    public String detail(@PathVariable Long id,
                         Principal principal,
                         Model model) {
        Cafe cafe = cafeListService.getById(id);

        // 로그인 사용자 조회
        SiteUser loginUser = null;
        if (principal != null) {
            String username = principal.getName();
            loginUser = userService.getUser(username);

        }
        boolean liked = false;

        if (loginUser != null) {
            Long cafeId = id.longValue();
            // 성능/안정성: 엔티티 equals/hashCode에 의존하지 말고 ID로 체크
            liked = likeService.isLiked(cafeId, loginUser.getId());
        }

        long likeCount = likeService.getLikeCount(id.longValue()); // 좋아요 수
        boolean openNow = cafeListService.isOpenNow(cafe); //영업상태

        model.addAttribute("cafe", cafe);
        model.addAttribute("liked", liked);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("openNow", openNow);
        return "cafe/cafe_detail";
    }

    //@PreAuthorize("isAuthenticated()") 써야할까 고민 중
    @PostMapping("/detail/{id}/like")
    public String toggleLike(@PathVariable Long id,
                             Principal principal) {
        if (principal == null) {
            return "redirect:/user/login";
        }
        // username → 사용자 ID 조회(엔티티 통째로 안 가져와도 되게 메서드 준비 권장)
        SiteUser user = userService.getUser(principal.getName());

        likeService.toggle(id.longValue(), user.getId());

        return "redirect:/cafe/detail/" + id; // 상세로 복귀
    }



}
