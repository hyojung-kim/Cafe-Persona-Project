package com.team.cafe.controller;

import com.team.cafe.domain.Cafe;
import com.team.cafe.domain.Review;
import com.team.cafe.service.CafeService;
import com.team.cafe.service.ReviewService;
import org.springframework.stereotype.Controller;
// @Controller: 이 클래스가 웹 요청을 처리하는 컨트롤러임을 표시
import lombok.RequiredArgsConstructor;
// @RequiredArgsConstructor: final 필드(서비스들)를 자동으로 생성자 주입 해줌
import org.springframework.web.bind.annotation.*;
// @RequestMapping, @GetMapping, @PathVariable, @RequestParam 같은 어노테이션 사용
import org.springframework.ui.Model;
// Model: 뷰(HTML)에 데이터를 전달하기 위한 객체
import org.springframework.data.domain.Page;
// Page: 스프링 데이터 JPA에서 제공하는 페이징 결과 타입

@Controller
@RequiredArgsConstructor
// @RequiredArgsConstructor: final로 선언된 필드(cafeService, reviewService)를 자동으로 생성자 주입
@RequestMapping("/cafes")
// "/cafes" 경로로 들어오는 요청을 처리하는 컨트롤러
public class CafeController {

    // 서비스 객체 주입 (비즈니스 로직 처리 담당)
    private final CafeService cafeService;   // 카페 정보 관련 로직
    private final ReviewService reviewService; // 리뷰 관련 로직

    /**
     * 카페 상세 페이지 조회
     * URL 예: /cafes/5?page=0&size=10
     */
    @GetMapping("/{id}")
    // {id} → 카페 ID (경로 변수)
    public String detail(@PathVariable Long id,
                         // URL 경로에 있는 {id} 값을 메서드 매개변수로 받음
                         @RequestParam(defaultValue = "0") int page,
                         // 쿼리 파라미터 ?page= 값 (기본값 0)
                         @RequestParam(defaultValue = "10") int size,
                         // 쿼리 파라미터 ?size= 값 (기본값 10)
                         Model model) {
        // 뷰(템플릿)으로 데이터를 전달하는 객체

        // 1. 카페 정보 조회 (id가 없으면 예외 발생 → GlobalExceptionHandler에서 처리)
        Cafe cafe = cafeService.getCafeOrThrow(id);

        // 2. 해당 카페에 대한 리뷰 목록 가져오기 (페이징 적용)
        Page<Review> reviews = reviewService.getReviewsByCafe(id, page, size);

        // 3. 카페 리뷰의 평균 별점 계산
        Double avgRating = reviewService.getAverageRating(id);

        // 4. 뷰로 데이터 전달
        model.addAttribute("cafe", cafe);          // 카페 정보
        model.addAttribute("reviews", reviews);    // 리뷰 목록 (페이징 포함)
        model.addAttribute("avgRating", avgRating);// 평균 별점

        // 5. cafe/detail.html 뷰 렌더링
        return "cafe/detail";
    }
}
