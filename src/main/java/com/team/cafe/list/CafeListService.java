package com.team.cafe.list;

import com.team.cafe.DataNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class CafeListService {
    private final CafeListRepository cafeListRepository;

    public List<Cafe> getAllCafes() {
        return cafeListRepository.findAll();
    }

    public Page<Cafe> getCafes(String kw, int page, int size, String sort, String dir,
                               Boolean parking, Boolean openNow) {
        Sort sortSpec = buildSort(sort, dir);
        Pageable pageable = PageRequest.of(page, size, sortSpec);
        String kwTrim = (kw == null) ? null : kw.trim();

        // 현재 시간(한국)
        LocalTime now = null;
        if (Boolean.TRUE.equals(openNow)) {
            now = LocalTime.now(java.time.ZoneId.of("Asia/Seoul"));
        }

        return cafeListRepository.searchWithFilters(kwTrim, parking, now, pageable);
    }


    // 허용된 정렬 키만 사용 (예외/보안/실수 방지)
    private Sort buildSort(String sort, String dir) {
        // 기본값: 최신순 (createdAt desc)
        String key = (sort == null || sort.isBlank()) ? "createdAt" : sort;
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // 화이트리스트 매핑 (엔티티 필드명 기준!)
        return switch (key) {
            case "name"      -> Sort.by(direction, "name");        // 이름순
            case "hit"       -> Sort.by(direction, "hitCount");    // 인기순(조회수)
            case "createdAt" -> Sort.by(direction, "createdAt");   // 최신순
            default          -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    public Cafe getById(Integer id) {
        Optional<Cafe> cafe = this.cafeListRepository.findById(id);
        if (cafe.isPresent()) {
            return cafe.get();
        } else {
            throw new DataNotFoundException("cafe not found");
        }
    }

    public Boolean  isOpenNow(Cafe cafe) {
        if (cafe.getOpenTime() == null || cafe.getCloseTime() == null) {
            return null; // 영업시간 미등록 → 상태 판단 불가
        }
        LocalTime now = LocalTime.now();
        return !now.isBefore(cafe.getOpenTime()) && !now.isAfter(cafe.getCloseTime());
    }

    /** 같은 세션에서 같은 카페 상세는 1회만 카운트 */
    @Transactional
    public void increaseViewOncePerSession(Integer cafeId, HttpSession session) {
        @SuppressWarnings("unchecked")
        Set<Integer> viewed = (Set<Integer>) session.getAttribute("viewed_cafes");
        if (viewed == null) {
            viewed = new HashSet<>();
            session.setAttribute("viewed_cafes", viewed);
        }
        if (viewed.add(cafeId)) {
            cafeListRepository.incrementHitCount(cafeId);
        }
    }
}
