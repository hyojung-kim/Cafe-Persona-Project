package com.team.cafe.controller;

import com.team.cafe.domain.Cafe;
import com.team.cafe.domain.Review;
import com.team.cafe.domain.ReviewImage;
import com.team.cafe.repository.ReviewImageRepository;
import com.team.cafe.service.CafeService;
import com.team.cafe.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 카페 상세 페이지 컨트롤러
 * - 리뷰는 페이징으로 가져오고
 * - 리뷰 이미지들은 리뷰 ID 묶음으로 배치 조회하여 Map<Long, List<ReviewImage>> 로 뷰에 전달
 *   → 템플릿에서 rv.images를 직접 접근하지 않아 LazyInitializationException 방지
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/cafes")
public class CafeController {

    private final CafeService cafeService;
    private final ReviewService reviewService;
    private final ReviewImageRepository reviewImageRepository;

    /**
     * 카페 상세 페이지
     * 예: /cafes/5?page=0&size=10
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         Model model) {

        // 1) 카페 기본 정보
        Cafe cafe = cafeService.getCafeOrThrow(id);

        // 2) 리뷰 페이지 (author는 @EntityGraph 등으로 미리 로딩되어 있어야 rv.author.username 안전)
        Page<Review> reviews = reviewService.getReviewsByCafe(id, page, size);

        // 3) 평균 별점
        Double avgRating = reviewService.getAverageRating(id);

        // 4) 리뷰 ID 목록 추출 (페이징 결과의 content 기준)
        List<Long> reviewIds = reviews.getContent().stream()
                .map(Review::getId)
                .toList();

        // 5) 이미지 배치 조회 → reviewId 기준으로 그룹핑
        //    엔티티에 sortOrder 필드가 있으면 아래 메서드 사용:
        List<ReviewImage> allImages = reviewIds.isEmpty()
                ? Collections.emptyList()
                : reviewImageRepository.findAllByReviewIdsOrderByReviewIdAndSortOrder(reviewIds);

        //    (대안) sortOrder가 없다면 ID 기준 정렬 메서드 사용:
        // List<ReviewImage> allImages = reviewIds.isEmpty()
        //        ? Collections.emptyList()
        //        : reviewImageRepository.findAllByReviewIdsOrderByReviewIdAndId(reviewIds);

        Map<Long, List<ReviewImage>> imagesByReview = allImages.stream()
                .collect(Collectors.groupingBy(
                        ri -> ri.getReview().getId(),
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            // 필요시 불변 리스트로 감싸서 뷰에서 수정 방지
                            return List.copyOf(list);
                        })
                ));

        // 6) 이미지 개수 맵 (조건부 표시용) — 배치 count 프로젝션 활용
        Map<Long, Long> imageCountMap;
        if (reviewIds.isEmpty()) {
            imageCountMap = Collections.emptyMap();
        } else {
            imageCountMap = reviewImageRepository.countByReviewIds(reviewIds).stream()
                    .collect(Collectors.toMap(
                            ReviewImageRepository.ImageCountPerReview::getReviewId,
                            ReviewImageRepository.ImageCountPerReview::getCnt
                    ));
        }

        // 7) 모델 바인딩
        model.addAttribute("cafe", cafe);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);

        // rv.images 대신 사용할 안전한 자료구조
        model.addAttribute("imagesByReview", imagesByReview); // Map<Long, List<ReviewImage>>
        model.addAttribute("imageCountMap", imageCountMap);   // Map<Long, Long>

        return "cafe/detail";
    }
}
