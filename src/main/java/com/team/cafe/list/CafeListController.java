package com.team.cafe.list;

import com.team.cafe.like.LikeService;
import com.team.cafe.user.sjhy.SiteUser;
import com.team.cafe.user.sjhy.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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

        // 검색어가 공백 문자열이면 null로 정규화
        if (kw != null && kw.isBlank()) kw = null;

        var paging = cafeListService.getCafes(kw, page, size, sort, dir, parking, openNow);

        // 데이터가 null 값이면 html 타임리프에서 파싱하는 과정에서 isEmpty()에서 터져서 수정 조금 했습니다
        // null 전달하지 않게 빈 페이지로 대체하는 코드입니다
        if (paging == null) {
            try {
                paging = Page.empty(PageRequest.of(page, size));
            } catch (Throwable t) {
                paging = new PageImpl<Cafe>(List.of());
            }
        }

        // 템플릿에서 바로 쓰기 편하게 content도 별도 제공
        List<Cafe> cafes;
        try {
            cafes = (List<Cafe>) paging.getContent(); // Page인 경우
        } catch (Throwable t) {
            // 커스텀 페이징 타입이라면 서비스에서 content 꺼내는 메서드 사용
            cafes = (paging != null && paging.getContent() != null) ? paging.getContent() : List.of();
        }


        model.addAttribute("paging", paging);
        model.addAttribute("cafes", cafes); // 리스트 전용
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
            // 성능/안정성: 엔티티 equals/hashCode에 의존하지 말고 ID로 체크
            liked = likeService.isLiked(id, loginUser.getId());
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
                             Principal principal) {
        if (principal == null) {
            return "redirect:/user/login";
        }
        // username → 사용자 ID 조회(엔티티 통째로 안 가져와도 되게 메서드 준비 권장)
        SiteUser user = userService.getUser(principal.getName());

        likeService.toggle(id, user.getId());

        return "redirect:/cafe/detail/" + id; // 상세로 복귀
    }



}
