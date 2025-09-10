package com.team.cafe.list.hj;

import com.team.cafe.bookmark.BookmarkService;
import com.team.cafe.bookmark.LikeBookmarkFacade;
import com.team.cafe.cafeListImg.hj.CafeImageService;
import com.team.cafe.keyword.hj.Keyword;
import com.team.cafe.keyword.hj.KeywordService;
import com.team.cafe.keyword.hj.KeywordType;
import com.team.cafe.like.LikeService;
import com.team.cafe.review.domain.Review;
import com.team.cafe.review.service.ReviewService;
import com.team.cafe.user.sjhy.SiteUser;
import com.team.cafe.user.sjhy.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/cafe")
@RequiredArgsConstructor
@Controller
public class CafeListController {
    //더미용으로 개발한 @AuthenticationPrincipal SiteUser user 사용 추후에 제거해야 합니다 !!!
    private final CafeListService cafeListService;
    private final UserService userService;
    private final LikeService likeService;
    private final CafeImageService cafeImageService;
    private final ReviewService reviewService;
    private final LikeBookmarkFacade likeBookmarkFacade;
    private final BookmarkService bookmarkService;
    private final KeywordService keywordService;

    @Value("{kakao.api.key}")
    private String kakaoApiKey;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "3") int size,
                       @RequestParam(required = false) String kw,
                       @RequestParam(defaultValue = "createdAt") String sort,
                       @RequestParam(defaultValue = "desc") String dir,
                       @RequestParam(required = false) Boolean parking,  // true면 가능만
                       @RequestParam(required = false) Boolean openNow,  // true면 영업중만
                       @RequestParam(name = "keyList", required = false) List<Long> keyList,
                       Model model
    ) {
        // 기존 코드 : var paging = cafeListService.getCafes(kw, page, size, sort, dir, parking, openNow);
        Page<CafeMatchDto> paging = cafeListService.getCafes(kw, page, size, sort, dir, parking, openNow, keyList);

        List<Keyword> all = keywordService.findAllOrderByTypeAndName(); // 네 엔티티/레포에 맞춰 사용
        Map<KeywordType, List<Keyword>> keywordsByType  = new LinkedHashMap<>();
        for (Keyword k : all) {
            KeywordType key = k.getType();
            keywordsByType.computeIfAbsent(key, t -> new ArrayList<>()).add(k);
        }

        // 이번 페이지의 카페 ID들만 모아서
        List<Long> ids = paging.getContent().stream()
               .map(CafeMatchDto::getId)
               .toList();

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
        //키워드 모델
        model.addAttribute("keywordsByType", keywordsByType);
        model.addAttribute("selectedKeys", keyList);

        return "cafe/cafe_list";
    }


    /** 카페 상세 + 같은 페이지에서 리뷰 작성/리스트 */
    @GetMapping("/detail/{cafeId}")
    public String detail(@PathVariable Long cafeId,
                         @RequestParam(name = "rpage", defaultValue = "0") int reviewPage,
                         @RequestParam(name = "rsize", defaultValue = "5") int reviewSize,
                         Principal principal,
                         HttpSession session,
                         Model model) {

        Cafe cafe = cafeListService.getById(cafeId);
        boolean bookmarked = false;

        cafeListService.increaseViewOncePerSession(cafeId, session);

        // 로그인 사용자
        SiteUser loginUser = null;
        if (principal != null) {
            loginUser = userService.getUser(principal.getName());
        }

        boolean liked = false;
        if (loginUser != null) {
            liked = likeService.isLiked(cafeId, loginUser.getId());
            //북마크
            bookmarked = bookmarkService.existsByUser_IdAndCafe_Id(loginUser.getId(), cafeId);
         
        }

        
        long likeCount = likeService.getLikeCount(cafeId);
        boolean openNow = cafeListService.isOpenNow(cafe);

        // 상단 배지용 통계
        double avgRating = cafeListService.getActiveAverageRating(cafeId);
        long reviewCount = cafeListService.getActiveReviewCount(cafeId);

        // 리뷰 페이지 (작성자/이미지 N+1 방지 메서드 사용)
        Pageable pageable = PageRequest.of(reviewPage, reviewSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewService.getActiveReviewsByCafeWithUserImages(cafeId, pageable);

        model.addAttribute("cafe", cafe);
        model.addAttribute("liked", liked);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("openNow", openNow);
        model.addAttribute("bookmarked", bookmarked);

        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviewCount", reviewCount);

        model.addAttribute("reviewSectionUrl",
                String.format("/cafe/detail/%d/reviews/section?rpage=%d&rsize=%d", cafeId, reviewPage, reviewSize));

        // 템플릿에서 ${reviews}로 사용
        model.addAttribute("reviews", reviews);

        // 분리한 템플릿 경로와 일치
        return "cafe/cafe_detail";
    }

    @GetMapping("/map/{cafeId}")
    public String map(@PathVariable Long cafeId, Model model) {
        Cafe cafe = cafeListService.getById(cafeId);
        model.addAttribute("cafe", cafe);
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        return "cafe/cafe_map";
    }

    // Ajax 컨트롤러
    // @PreAuthorize("isAuthenticated()")
    // 상태 변경은 POST로
    @PostMapping(value = "/like/{cafeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody  // 이 메서드만 JSON으로
    public ResponseEntity<?> toggle(@PathVariable Long cafeId, Principal principal) {
        if (principal == null) {
            // 비로그인 → 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SiteUser user = userService.getUser(principal.getName());

        // toggle이 true/false(현재 상태) 반환하도록 만들면 최고
        //boolean liked = likeService.toggle(cafeId, user.getId());
        boolean likedNow = likeBookmarkFacade.toggleAndSync(cafeId, user.getId(), true);

        long count = likeService.getLikeCount(cafeId);

        // 북마크 정책이 “좋아요 ON == 북마크 보장”이면 결과는 likedNow와 동일하게 안내 가능
        return ResponseEntity.ok(Map.of(
                "count", count,
                "liked", likedNow,
                "bookmarked", likedNow   // 정책상 보장됨(OFF도 유지 정책이면 exists 쿼리로 반환)
        ));
    }

    // 리뷰 목록 프래그먼트 (HTML 조각만 반환)
    @GetMapping(value = "/detail/{cafeId}/reviews/section", produces = MediaType.TEXT_HTML_VALUE)
    public String reviewsSection(@PathVariable Long cafeId,
                                 @RequestParam(name = "rpage", defaultValue = "0") int reviewPage,
                                 @RequestParam(name = "rsize", defaultValue = "5") int reviewSize,
                                 Model model) {

        Cafe cafe = cafeListService.getById(cafeId);
        double avgRating = cafeListService.getActiveAverageRating(cafeId);
        long reviewCount = cafeListService.getActiveReviewCount(cafeId);

        Pageable pageable = PageRequest.of(reviewPage, reviewSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewService.getActiveReviewsByCafeWithUserImages(cafeId, pageable);

        model.addAttribute("cafe", cafe);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("reviews", reviews);

        // 템플릿 선택: 프로젝트에 있는 프래그먼트로 바꿔도 됨
        // return "review/list :: section";
        return "cafe/reviews_section :: reviews_section";
    }
}