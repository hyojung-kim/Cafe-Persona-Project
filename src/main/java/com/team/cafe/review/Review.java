package com.team.cafe.review;


import com.team.cafe.list.Cafe;
import com.team.cafe.user.sjhy.SiteUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "com/team/cafe/review",
        indexes = {
                @Index(name = "idx_review_cafe", columnList = "cafe_id"),
                @Index(name = "idx_review_user", columnList = "user_id"),
                @Index(name = "idx_review_created_at", columnList = "created_at DESC")
        },
        uniqueConstraints = {
                // 한 사용자가 같은 카페에 중복 리뷰 못 쓰게 막고 싶다면 유지, 아니면 제거하세요.
                @UniqueConstraint(name = "uk_review_cafe_user", columnNames = {"cafe_id", "user_id"})
        }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", columnDefinition = "BIGINT UNSIGNED")
    @Comment("리뷰 PK")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cafe_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_cafe"))
    @Comment("리뷰 대상 카페")
    private Cafe cafe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_user"))
    @Comment("리뷰 작성 사용자")
    private SiteUser user;

    @Column(name = "title", length = 150, nullable = false)
    @Comment("리뷰 제목")
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    @Comment("리뷰 본문")
    private String content;

    @Min(0) @Max(5)
    @Column(name = "rating", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    @Comment("별점(0~5)")
    private int rating;

    // 좋아요(추천) 누른 사용자들
    @ManyToMany
    @JoinTable(
            name = "review_like",
            joinColumns = @JoinColumn(name = "review_id",
                    foreignKey = @ForeignKey(name = "fk_review_like_review")),
            inverseJoinColumns = @JoinColumn(name = "user_id",
                    foreignKey = @ForeignKey(name = "fk_review_like_user"))
    )
    @Comment("좋아요 누른 사용자 목록")
    private Set<SiteUser> voters = new LinkedHashSet<>();

    // 이미지가 별도 엔티티라면 연결 (필요 없으면 제거)
//    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
//    @Comment("리뷰 이미지 목록")
//    private List<ReviewImage> images;

    @Column(name = "like_count", nullable = false, columnDefinition = "INT UNSIGNED")
    @Comment("좋아요 수(캐시용)")
    private int likeCount = 0;

    @Column(name = "created_at", nullable = false)
    @Comment("작성 일시")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    @Comment("수정 일시")
    private LocalDateTime modifiedAt;


}
