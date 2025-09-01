package com.team.cafe;

import com.team.cafe.domain.*;
import com.team.cafe.repository.CafeRepository;
import com.team.cafe.repository.ReviewRepository;
import com.team.cafe.repository.SiteUserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class CafeApplicationTests {


    @Autowired
    CafeRepository cafeRepository;
    @Autowired
    SiteUserRepository siteUserRepository;
    @Autowired
    ReviewRepository reviewRepository;

    private Cafe saveCafe(String name) {

        return cafeRepository.save(
                Cafe.builder()
                        .name(name)
                        .address("어딘가 123")
                        .lat(37.0)
                        .lng(127.0)
                        .category("커피")
                        .build()
        );
    }

    private SiteUser saveUser(String username) {
        return siteUserRepository.save(
                SiteUser.builder()
                        .username(username)
                        .password("{noop}pass") // 테스트에서는 인코딩 불요
                        .roles(Set.of("ROLE_USER"))
                        .build()
        );
    }

    private Review saveReview(Cafe cafe, SiteUser author, double rating, String content, LocalDateTime createdAt, long viewCount) {
        Review r = Review.builder()
                .cafe(cafe)
                .author(author)
                .rating(rating)
                .content(content)
                .viewCount(viewCount)
                .status(ReviewStatus.ACTIVE)
                .createdAt(createdAt)
                .modifiedAt(createdAt)
                .build();
        return reviewRepository.save(r);
    }

    @Test
    @DisplayName("카페의 리뷰를 최신순으로 페이징 조회한다")
    void findByCafeIdOrderByCreatedAtDesc_paging() {
        // given
        Cafe cafe = saveCafe("테스트카페");
        SiteUser u1 = saveUser("user1");
        SiteUser u2 = saveUser("user2");

        // createdAt이 최신일수록 나중 시간
        saveReview(cafe, u1, 4.5, "리뷰 A".repeat(25), LocalDateTime.now().minusDays(2), 0);
        saveReview(cafe, u2, 3.5, "리뷰 B".repeat(25), LocalDateTime.now().minusDays(1), 0);
        saveReview(cafe, u1, 5.0, "리뷰 C".repeat(25), LocalDateTime.now(), 0);

        Pageable pageable = PageRequest.of(0, 2); // 첫 페이지, 2개

        // when
        Page<Review> page = reviewRepository.findByCafe_IdOrderByCreatedAtDesc(cafe.getId(), pageable);

        // then
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
        // 최신순: C, B, A
        List<String> contents = page.getContent().stream().map(Review::getContent).toList();
        assertThat(contents.get(0)).contains("리뷰 C");
        assertThat(contents.get(1)).contains("리뷰 B");
    }

    @Test
    @DisplayName("리뷰가 없으면 평균 별점은 0.0을 반환한다")
    void averageRating_empty_returnsZero() {
        // given
        Cafe cafe = saveCafe("빈카페");

        // when
        Double avg = reviewRepository.averageRating(cafe.getId());

        // then
        assertThat(avg).isNotNull();
        assertThat(avg).isEqualTo(0.0);
    }

    @Test
    @DisplayName("리뷰가 있으면 평균 별점을 정확히 계산한다")
    void averageRating_calculates() {
        // given
        Cafe cafe = saveCafe("평균카페");
        SiteUser u = saveUser("userA");

        saveReview(cafe, u, 4.0, "내용".repeat(25), LocalDateTime.now(), 0);
        saveReview(cafe, u, 5.0, "내용".repeat(25), LocalDateTime.now(), 0);
        saveReview(cafe, u, 3.0, "내용".repeat(25), LocalDateTime.now(), 0);

        // when
        Double avg = reviewRepository.averageRating(cafe.getId());

        // then
        assertThat(avg).isNotNull();
        // (4 + 5 + 3) / 3 = 4.0
        assertThat(avg).isEqualTo(4.0);
    }

    @Test
    @DisplayName("조회수 증가 쿼리가 viewCount를 +1 한다")
    void incrementView_updatesCounter() {
        // given
        Cafe cafe = saveCafe("조회수카페");
        SiteUser u = saveUser("viewer");
        Review r = saveReview(cafe, u, 4.0, "본문".repeat(25), LocalDateTime.now(), 10);

        // when
        reviewRepository.incrementView(r.getId());

        // then
        Review updated = reviewRepository.findById(r.getId()).orElseThrow();
        assertThat(updated.getViewCount()).isEqualTo(11L);
    }
}