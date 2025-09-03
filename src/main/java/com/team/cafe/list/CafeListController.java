package com.team.cafe.list;

import com.team.cafe.cafeListImg.hj.CafeImageService;
import com.team.cafe.like.LikeService;
import com.team.cafe.user.sjhy.SiteUser;
import com.team.cafe.user.sjhy.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/cafe")
@RequiredArgsConstructor
@Controller
public class CafeListController {
    //더미용으로 개발한 @AuthenticationPrincipal SiteUser user 사용 추후에 제거해야 합니다 !!!
    private final CafeListService cafeListService;
    private final UserService userService;
    private final LikeService likeService;
    private final CafeImageService cafeImageService;

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

        // 이번 페이지의 카페 ID들만 모아서
        List<Long> ids = paging.getContent().stream()
                .map(Cafe::getId)
                .collect(Collectors.toList());

        // 대표 이미지 URL 맵 생성
        Map<Long, String> imageMap = cafeImageService.getImageUrlMap(ids);

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("parking", parking);
        model.addAttribute("openNow", openNow);
        model.addAttribute("imageMap", imageMap);
        return "cafe/cafe_list";
    }


    @GetMapping("detail/{cafeId}")
    public String detail(@PathVariable Long cafeId,
                         Principal principal,
                         Model model) {
        Cafe cafe = cafeListService.getById(cafeId);

        // 로그인 사용자 조회
        SiteUser loginUser = null;
        if (principal != null) {
            String username = principal.getName();
            loginUser = userService.getUser(username);

        }
        boolean liked = false;

        if (loginUser != null) {
            //Long cafeId = id;
            // 성능/안정성: 엔티티 equals/hashCode에 의존하지 말고 ID로 체크
            liked = likeService.isLiked(cafeId, loginUser.getId());
        }

        long likeCount = likeService.getLikeCount(cafeId); // 좋아요 수
        boolean openNow = cafeListService.isOpenNow(cafe); //영업상태

        model.addAttribute("cafe", cafe);
        model.addAttribute("liked", liked);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("openNow", openNow);
        return "cafe/cafe_detail";
    }


    // Ajax 컨트롤러
    // @PreAuthorize("isAuthenticated()")
    // 상태 변경은 POST로
    @PostMapping(value = "/like/{cafeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> toggle(@PathVariable Long cafeId, Principal principal) {
        if (principal == null) {
            // 비로그인 → 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SiteUser user = userService.getUser(principal.getName());

        // toggle이 true/false(현재 상태) 반환하도록 만들면 최고
        boolean liked = likeService.toggle(cafeId, user.getId());

        long count = likeService.getLikeCount(cafeId);

        return ResponseEntity.ok(Map.of(
                "count", count,
                "liked", liked
        ));
    }



}
