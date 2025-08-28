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
@Table(indexes = @Index(name="idx_review_cafe_created", columnList = "cafe_id, createdAt DESC"))
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Cafe cafe;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private SiteUser author;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column
    private Double rating;

    @Builder.Default
    private Long viewCount = 0L;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();
}
