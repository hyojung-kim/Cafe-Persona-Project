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
    @Column(nullable = false, length = 225)
    private String urlPath;

    @Column(length = 255)
    private String originalFilename;

    private Long sizeBytes;
}
