package com.team.cafe.list.hj;

import com.team.cafe.DataNotFoundException;
import com.team.cafe.review.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class CafeListService {
    private final CafeListRepository cafeListRepository;
    private final ReviewRepository reviewRepository; // ⬅️ 리뷰 조회용

//    public List<Cafe> getAllCafes() {
//        return cafeListRepository.findAll();
//    }

//    public Page<Cafe> getCafes2(String kw, int page, int size, String sort, String dir,
//                               Boolean parking, Boolean openNow) {
//        Sort sortSpec = buildSort(sort, dir);
//        Pageable pageable = PageRequest.of(page, size, sortSpec);
//        String kwTrim = (kw == null) ? null : kw.trim();
//
//        // 현재 시간(한국)
//        LocalTime now = null;
//        if (Boolean.TRUE.equals(openNow)) {
//            now = LocalTime.now(java.time.ZoneId.of("Asia/Seoul"));
//        }
//
//        return cafeListRepository.searchWithFilters(kwTrim, parking, now, pageable);
//    }

     //허용된 정렬 키만 사용
//    private Sort buildSort(String sort, String dir) {
//        String key = (sort == null || sort.isBlank()) ? "createdAt" : sort;
//        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
//
//        return switch (key) {
//            case "likeCount" -> Sort.by(direction, "likeCount");
//            case "hit" -> Sort.by(direction, "hitCount");
//            case "createdAt" -> Sort.by(direction, "createdAt");
//            default -> Sort.by(Sort.Direction.DESC, "createdAt");
//        };
//    }

    /**
     * PK 타입 Long 통일
     */
    public Cafe getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        return this.cafeListRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("cafe not found"));
    }

    /**
     * 기존 Integer 호출부 호환용   // hyo : 호환 수정했으니 이제 필요 없을 거에요 주석처리함
     */
//    public Cafe getById(Integer id) {
//        if (id == null) {
//            throw new IllegalArgumentException("id is null");
//        }
//        return getById(id.longValue());
//    }

    /**
     * 카페 평균 평점
     */
    public double getActiveAverageRating(Long cafeId) {
        Double avg = reviewRepository.calculateAverageRating(cafeId);
        return (avg != null) ? avg : 0.0;
    }

    /**
     * 카페 리뷰 개수
     */
    public long getActiveReviewCount(Long cafeId) {
        return reviewRepository.countByCafe_IdAndActiveTrue(cafeId);
    }

    /**
     * 카페 현재 영업 여부
     */
    public Boolean isOpenNow(Cafe cafe) {
        if (cafe.getOpenTime() == null || cafe.getCloseTime() == null) {
            return null;
        }
        LocalTime now = LocalTime.now();
        return !now.isBefore(cafe.getOpenTime()) && !now.isAfter(cafe.getCloseTime());
    }

    /** 같은 세션에서 같은 카페 상세는 1회만 카운트 */
    @Transactional
    public void increaseViewOncePerSession(Long cafeId, HttpSession session) {
        @SuppressWarnings("unchecked")
        Set<Long> viewed = (Set<Long>) session.getAttribute("viewed_cafes");
        if (viewed == null) {
            viewed = new HashSet<>();
            session.setAttribute("viewed_cafes", viewed);
        }
        if (viewed.add(cafeId)) {
            cafeListRepository.incrementHitCount(cafeId);
        }
    }

    public Page<CafeMatchDto> getCafes(String kw,
                                       int page, int size,
                                       String sort, String dir,
                                       Boolean parking,
                                       Boolean openNow,
                                       List<Long> keyList) {
        String kwTrim = (kw == null) ? null : kw.trim();

        // 현재 시간(한국)
        LocalTime now = null;
        if (Boolean.TRUE.equals(openNow)) {
            now = LocalTime.now(java.time.ZoneId.of("Asia/Seoul"));
        }
        if (keyList == null || keyList.isEmpty()) {
            keyList = null;
        }


        // ✅ 태그 모두일치 + 기존 필터 동시 적용
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1)); // 정렬은 쿼리로
        Long selectedSize = (keyList == null) ? 0L : keyList.stream().distinct().count();
        Page<Cafe> pageAll = cafeListRepository.findAllMatchAllWithFiltersCaseOrder(kw, parking, now, keyList, selectedSize, sort, dir, pageable);
        return pageAll.map(c -> new CafeMatchDto(c, selectedSize));
    }


}