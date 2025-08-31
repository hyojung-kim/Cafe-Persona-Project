package com.team.cafe;

import com.team.cafe.domain.Cafe;
import com.team.cafe.domain.Review;
import com.team.cafe.domain.SiteUser;
import com.team.cafe.repository.CafeRepository;
import com.team.cafe.repository.ReviewRepository;
import com.team.cafe.repository.SiteUserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@Transactional
@SpringBootTest
class CafeApplicationTests {

    @Autowired
    private CafeRepository cafeRepository;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void insertSampleData() {
        // 1) 회원 추가
        SiteUser user1 = SiteUser.builder()
                .username("user1")
                .password("pass1")   // 실제 서비스에서는 BCrypt 같은 해시 필요
                .build();
        siteUserRepository.save(user1);

        SiteUser user2 = SiteUser.builder()
                .username("user2")
                .password("pass2")
                .build();
        siteUserRepository.save(user2);

        // 2) 카페 추가
        Cafe cafe1 = Cafe.builder()
                .name("테스트카페")
                .address("서울 어딘가 123")
                .lat(37.5665)
                .lng(126.9780)
                .category("디저트")
                .build();
        cafeRepository.save(cafe1);

        // 3) 리뷰 추가
        Review review1 = Review.builder()
                .cafe(cafe1)
                .author(user1)
                .content("커피 맛있고 분위기 좋아요. 추천합니다!".repeat(5)) // 50자 이상
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
        reviewRepository.save(review1);

        Review review2 = Review.builder()
                .cafe(cafe1)
                .author(user2)
                .content("조용해서 공부하기 좋아요. 디저트도 훌륭합니다.".repeat(4))
                .rating(5.0)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
        reviewRepository.save(review2);
    }
}
