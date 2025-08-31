package com.team.cafe.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * ReviewImage 엔티티
 * - 리뷰에 업로드된 이미지 한 장을 표현하는 도메인 모델
 * - DB 테이블과 매핑되어 각 이미지의 경로/원본 이름/크기/정렬순서 등을 관리
 */
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // PK (Auto Increment). 각 이미지마다 고유 ID를 자동 생성
    private Long id;

    // (이미지 ↔ 리뷰) N:1 관계
    // 여러 이미지가 하나의 리뷰에 속함
    @ManyToOne(optional = false, fetch=FetchType.LAZY)
    // optional=false → 반드시 리뷰에 속해야 함 (고아 이미지 불가)
    // LAZY → 실제로 필요할 때만 Review 엔티티를 가져옴 (성능 최적화)
    private Review review;

    // 파일 저장 경로 (예: uploads/reviews/{reviewId}/{uuid}.jpg)
    // - uuid를 붙여서 파일명이 중복되지 않도록 설계
    @Column(nullable = false, length = 500)
    private String urlPath;

    // 원본 파일명 (사용자가 업로드한 실제 이름)
    private String originalFilename;

    // 파일 크기(바이트 단위)
    private Long sizeBytes;

    // 이미지 정렬 순서 (0부터 시작 → 첫 번째 이미지, 두 번째 이미지...)
    // UI에서 대표 이미지/슬라이드 순서 등을 표시할 때 사용
    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
