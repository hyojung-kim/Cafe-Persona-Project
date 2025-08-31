package com.team.cafe.config;
// 패키지: config 패키지 → "설정 관련 클래스"를 보관하는 공간


import org.springframework.ui.Model;
// Model: 뷰(HTML)로 데이터를 전달할 때 사용하는 객체
import org.springframework.web.bind.annotation.ControllerAdvice;
// @ControllerAdvice: 전역적으로 컨트롤러에서 발생하는 예외를 처리하게 해주는 어노테이션
import org.springframework.web.bind.annotation.ExceptionHandler;
// @ExceptionHandler: 특정 예외를 잡아서 처리할 수 있게 해주는 어노테이션
import org.springframework.web.bind.annotation.RequestHeader;
// @RequestHeader: HTTP 요청 헤더 값을 가져오기 위해 사용
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
// RedirectAttributes: 리다이렉트할 때 데이터(일회성 메시지)를 전달하는 객체


/**
 * GlobalExceptionHandler 클래스
 * - 프로젝트 전체 컨트롤러에서 발생하는 예외를 한곳에서 처리하는 클래스
 * - 개별 컨트롤러마다 try-catch 쓰지 않고, 전역적으로 공통 처리 가능
 */
@ControllerAdvice
// 모든 @Controller와 @RestController에서 발생하는 예외를 감지해서 처리할 수 있게 해줌
public class GlobalExceptionHandler {

    /**
     * IllegalStateException 처리 메서드
     * - 특정 상황에서 객체의 상태가 잘못되었을 때 발생하는 예외를 처리
     * - 예: "본인 리뷰에는 좋아요를 누를 수 없습니다" 같은 로직에서 IllegalStateException 발생 가능
     */
    @ExceptionHandler(IllegalStateException.class)
    // IllegalStateException 예외가 발생하면 이 메서드가 실행됨
    public String handleIllegalState(IllegalStateException e,
                                     @RequestHeader(value="Referer", required=false) String referer,
                                     // 예외가 발생한 요청의 "Referer"(이전 페이지 주소)를 가져옴. 없으면 null
                                     RedirectAttributes ra) {
        // RedirectAttributes → redirect할 때 임시 데이터(FlashAttribute)를 전달하는 용도
        ra.addFlashAttribute("alert", e.getMessage());
        // e.getMessage() → 예외 메시지를 alert라는 이름으로 저장
        // FlashAttribute는 리다이렉트 후 딱 1번만 전달됨 (세션에 잠깐 저장했다가 자동 삭제)

        return "redirect:" + (referer != null ? referer : "/");
        // referer(이전 페이지 주소)가 있으면 그곳으로 리다이렉트, 없으면 "/"(홈)으로 이동
    }

    /**
     * IllegalArgumentException 처리 메서드
     * - 잘못된 메서드 인자(파라미터)로 인해 발생하는 예외 처리
     * - 예: "별점은 0.0 ~ 5.0 사이여야 합니다" 같은 상황
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArg(IllegalArgumentException e, Model model) {
        model.addAttribute("message", e.getMessage());
        // 예외 메시지를 뷰로 전달 → error/common.html 같은 에러 페이지에서 출력 가능

        return "error/common";
        // "error/common" → templates/error/common.html 뷰를 반환
    }

    /**
     * 일반 Exception 처리 메서드
     * - 위에서 지정하지 않은 모든 예외를 처리하는 "최종 안전망"
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception e, Model model) {
        model.addAttribute("message", "예기치 못한 오류가 발생했습니다.");
        // 상세 메시지 대신 사용자가 이해할 수 있는 일반적인 오류 문구 전달

        return "error/common";
        // 공통 에러 페이지로 이동
    }
}

