package com.team.cafe.like;

import com.team.cafe.list.CafeListRepository;
import com.team.cafe.user.sjhy.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LikeService {
    private final CafeListRepository cafeListRepository;
    private final UserRepository siteUserRepository;

    /** 좋아요 여부 확인 (true=좋아요 상태, false=아님) */
    @Transactional(readOnly = true)
    public boolean isLiked(Long cafeId, Long userId) { // Integer -> Long
        if (userId == null || cafeId == null) return false; // 안전장치
        return cafeListRepository.existsByIdAndLikedUsers_Id(cafeId, userId);
    }

    /** 좋아요 토글 (true=좋아요 됨, false=좋아요 취소됨) */
    @Transactional
    public boolean toggle(Long cafeId, Long userId) { // Integer -> Long
        var cafe = cafeListRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페 없음: " + cafeId));
        var userRef = siteUserRepository.getReferenceById(userId);

        boolean liked = cafeListRepository.existsByIdAndLikedUsers_Id(cafeId, userId);
        if (liked) {
            cafe.getLikedUsers().remove(userRef);   // 취소
            return false;
        } else {
            cafe.getLikedUsers().add(userRef);     // 등록
            return true;
        }
    }

    @Transactional(readOnly = true)
    public long getLikeCount(Long cafeId) { // Integer -> Long
        return cafeListRepository.countLikes(cafeId);
    }
}
