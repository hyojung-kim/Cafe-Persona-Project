package com.team.cafe.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(name="idx_review_cafe_created", columnList = "cafe_id, createdAt DESC")
})
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 카페 리뷰인지
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Cafe cafe;

    // 리뷰 작성자
    @ManyToOne(optional = false, fetch=FetchType.LAZY)
    private SiteUser author;

    // 별점
    @Column(nullable = false)
    private Double rating;

    // 리뷰 내용
    @Column
    private String content;

    // 조회수
    @Builder.Default
    private Long viewCount = 0L;

    // 작성일과 수정일
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.ACTIVE;

    @OneToMany(mappedBy = "review", cascade=CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();

}
