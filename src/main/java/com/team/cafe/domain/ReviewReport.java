package com.team.cafe.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 리뷰에 대한 신고인가?
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Review review;

    // 누가 신고했는가?
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SiteUser reporter;

    @Column(nullable = false, length = 1000)
    private String reason;   // 신고 사유

    private boolean resolved = false;  // 처리 여부
}