package com.team.cafe.controller;

import com.team.cafe.domain.Review;                 // 도메인 엔티티: 리뷰의 상태/데이터를 담는 객체
import com.team.cafe.dto.ReviewCreateRequest;       // 리뷰 생성 요청 DTO (검증 대상)
import com.team.cafe.service.ReviewService;         // 비즈니스 로직(트랜잭션) 담당 서비스 계층
import jakarta.validation.Valid;                    // @Valid: DTO 필드 제약(@NotNull 등) 검증을 활성화
import lombok.RequiredArgsConstructor;              // final 필드 기반 생성자 자동 생성(생성자 주입)
import org.springframework.http.ResponseEntity;     // REST 응답 편의 객체 (상태코드+바디)
import org.springframework.security.access.prepost.PreAuthorize; // 메서드 단위 보안: 인증/권한 체크
import org.springframework.stereotype.Controller;   // MVC 컨트롤러(템플릿 반환)
import org.springframework.ui.Model;               // 뷰(Thymeleaf)로 값 전달
import org.springframework.web.bind.annotation.*;   // 요청 매핑(@GetMapping, @PostMapping 등)
import org.springframework.web.multipart.MultipartFile; // 업로드된 파일(이미지) 수신 타입

import java.io.IOException;
import java.security.Principal;                    // 현재 로그인한 사용자 정보(주체)의 이름(username) 접근
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor // 생성자 주입: final 필드(reviewService)를 스프링이 자동 주입
@RequestMapping("/reviews") // 이 컨트롤러의 모든 메서드는 "/reviews" 하위 URL을 다룸
public class ReviewController {

    private final ReviewService reviewService; // 리뷰 관련 비즈니스 로직(검증/권한/트랜잭션)

    /**
     * 리뷰 생성(작성) 처리
     * - POST /reviews
     * - 회원만 가능(@PreAuthorize("isAuthenticated()"))
     * - @Valid 붙은 DTO(ReviewCreateRequest)에 대해 Bean Validation 수행 후 서비스로 전달
     * - 이미지 업로드(Optional): MultipartFile 리스트로 받음
     * - 성공 시 해당 카페 상세 페이지로 리다이렉트
     */
    @PreAuthorize("isAuthenticated()") // 로그인 사용자만 접근 허용
    @PostMapping
    public String create(@Valid @ModelAttribute ReviewCreateRequest req, // 폼 데이터 바인딩+검증
                         @RequestParam(name = "images", required = false) List<MultipartFile> images, // 업로드 이미지들
                         Principal principal) throws IOException { // 로그인 사용자 식별: principal.getName() == username
        reviewService.createReview(principal.getName(), req, images);
        return "redirect:/cafes/" + req.cafeId(); // 작성 후 카페 상세로 이동(리뷰 리스트가 아래에 있음)
    }

    /**
     * (동기 폼 전송용) 리뷰 좋아요 토글
     * - POST /reviews/{id}/like
     * - 로그인 사용자만 가능
     * - 본인 리뷰에 좋아요 시도 시, 서비스에서 IllegalStateException을 던지도록 설계 가능
     * - 여기서는 try-catch로 잡아 쿼리스트링에 alert 메시지를 붙여 리다이렉트
     *   (전역 예외처리(GlobalExceptionHandler)도 있으니, 방식 통일을 고려)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/like")
    public String like(@PathVariable Long id,                // 좋아요를 누를 리뷰 ID
                       Principal principal,                  // 현재 로그인 사용자
                       @RequestHeader(value="Referer", required=false) String referer, // 이전 페이지로 돌아가기 위함
                       Model model) {
        try {
            reviewService.toggleLike(principal.getName(), id); // 성공 시 liked<->unliked 토글
        } catch (IllegalStateException e) {
            // 본인 리뷰에 좋아요 등 비즈니스 제약 위반 시
            // 쿼리스트링으로 메시지 전달(템플릿에서 읽어 alert 출력 가능)
            return "redirect:" + (referer != null ? referer : "/") + "?alert=" + e.getMessage();
        }
        return "redirect:" + (referer != null ? referer : "/");
    }

    /* -------------------------------------------------------------------------
       (AJAX용) 좋아요 토글 REST 엔드포인트
       - 위 동기 방식과 별개로, JS fetch/XHR로 호출 가능한 JSON 응답 버전 제공
       - 프론트에서 응답의 likedCount를 받아 버튼/카운트 UI만 부분 갱신하기 좋음
       ------------------------------------------------------------------------- */

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/like-toggle")
    @ResponseBody // 뷰 이름이 아니라 JSON 바디로 응답
    public ResponseEntity<?> likeToggle(@PathVariable Long id, Principal principal) {
        long count = reviewService.toggleLike(principal.getName(), id); // 토글 후 총 좋아요 수 반환
        return ResponseEntity.ok(Map.of("likedCount", count)); // {"likedCount": 12}
    }

    /**
     * 리뷰 상세 페이지
     * - GET /reviews/{id}
     * - 누구나 조회 가능(컨트롤러 레벨에 @PreAuthorize 없음 → SecurityConfig에서 조회 허용)
     * - 조회 시 조회수 증가(getAndIncreaseView)
     *   (수정/삭제 폼 등에서는 조회수 증가가 불필요하므로 주의)
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Review review = reviewService.getAndIncreaseView(id);     // 조회수 1 증가 + 리뷰 조회
        model.addAttribute("review", review);
        model.addAttribute("likeCount", reviewService.getLikeCount(id)); // 좋아요 수 별도 조회
        return "review/detail"; // templates/review/detail.html 렌더링
    }

    /**
     * 리뷰 수정 폼
     * - GET /reviews/{id}/edit
     * - 로그인 사용자만 접근
     * - 본인 리뷰인지 확인(작성자만 수정 가능)
     * - 현재는 getAndIncreaseView를 사용 → 폼 진입만으로 조회수가 증가함 (주의)
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        Review review = reviewService.getAndIncreaseView(id); // ⚠️ 폼 진입 시 조회수 증가. 별도 find 메서드 권장.
        if (!review.getAuthor().getUsername().equals(principal.getName())) {
            // 본인 글이 아니면 수정 불가 → 전역 예외 처리기로 안내 문구 전달 가능
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }
        model.addAttribute("review", review);
        return "review/edit";
    }

    /**
     * 리뷰 수정 제출
     * - POST /reviews/{id}/edit
     * - 로그인 사용자만 가능
     * - 본인 글인지 서비스에서 보안 검증하는 편이 안전(서버측 신뢰)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/edit")
    public String editSubmit(@PathVariable Long id,
                             @RequestParam String content, // 수정된 내용
                             @RequestParam Double rating,  // 수정된 별점(0.0~5.0, 0.5 단위) → 서비스/검증에서 체크
                             Principal principal) {
        Review updated = reviewService.updateReview(principal.getName(), id, content, rating);
        return "redirect:/reviews/" + updated.getId(); // 수정 후 상세 페이지로 이동
    }

    /**
     * 리뷰 삭제
     * - POST /reviews/{id}/delete
     * - 로그인 사용자만 가능
     * - 성공 후 해당 카페 상세로 리다이렉트
     * - 참고: 현재 카페 ID를 얻으려고 getAndIncreaseView를 사용 → 조회수 증가됨(주의)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        // ⚠️ 조회수 증가 없이 카페 ID만 필요하다면, 별도 조회 메서드로 대체하는 게 바람직
        Review r = reviewService.getAndIncreaseView(id);
        Long cafeId = r.getCafe().getId();

        reviewService.deleteReview(principal.getName(), id); // 서비스에서 본인 여부/권한 검증 포함 권장
        return "redirect:/cafes/" + cafeId;
    }
}