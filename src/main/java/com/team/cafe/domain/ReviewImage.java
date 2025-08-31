package com.team.cafe.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch=FetchType.LAZY)
    private Review review;

    //uploads/reviews/{reviewId}/{uuid}.ext
    @Column(nullable = false, length = 500)
    private String urlPath;

    private String originalFilename;
    private Long sizeBytes;

    // ✅ 정렬용 필드 추가
    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
