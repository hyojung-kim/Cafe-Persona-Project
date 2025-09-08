package com.team.cafe.list.hj;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CafeMatchDto {
    private Long id;              // @Id
    private String name;
    private String phoneNum;
    private String siteUrl;
    private String address1;
    private String address2;
    private String district;
    private String city;
    private BigDecimal lat;
    private BigDecimal lng;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean parkingYn;
    private int hitCount;

    // === BaseEntity(감사 필드) - 프로젝트에 맞게 이름 맞춰서 사용 ===
    private LocalDateTime createdAt;   // BaseEntity에 존재하면 세팅
    private LocalDateTime updatedAt;   // BaseEntity에 존재하면 세팅

    // === 뷰 편의/파생 필드 (템플릿·UI용) ===
    private Boolean openNow;       // 현재 영업중 여부
    private String imageUrl;       // 대표 이미지 URL
    private boolean liked;         // 로그인 사용자 좋아요 여부
    private boolean bookmarked;    // 로그인 사용자 북마크 여부
    private Long matchCount;       // 태그 일치 개수(모두일치 검증/정렬 표시 등)

    // (선택) 파생 카운트들 – 필요 시 채워서 사용
    private Long reviewCount;
    private Long bookmarkCount;
    private Long likeCount;

    // 선택: 태그 일치 개수 표시가 필요하면
    private Long selectedCount;

    //선택한 키워드 카운트, Cafe > CAfeMatchDto 변환위해서 오버로딩 사용함
    public CafeMatchDto(Cafe c, Long selectedCount) {
        // 엔티티 스칼라/감사 필드 풀 매핑
        this.id = c.getId();
        this.name = c.getName();
        this.phoneNum = c.getPhoneNum();
        this.siteUrl = c.getSiteUrl();
        this.address1 = c.getAddress1();
        this.address2 = c.getAddress2();
        this.district = c.getDistrict();
        this.city = c.getCity();
        this.lat = c.getLat();
        this.lng = c.getLng();
        this.openTime = c.getOpenTime();
        this.closeTime = c.getCloseTime();
        this.parkingYn = c.isParkingYn();
        this.hitCount = c.getHitCount();
        this.createdAt = c.getCreatedAt();   // BaseEntity에 맞게 이름 다르면 수정
        this.updatedAt = c.getUpdatedAt();



    }
}