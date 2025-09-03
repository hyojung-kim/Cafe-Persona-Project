package com.team.cafe.list;


import com.team.cafe.bookmark.Bookmark;
import com.team.cafe.keyword.CafeKeyword;
import com.team.cafe.review.Review;
import com.team.cafe.user.sjhy.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class Cafe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cafe_id")
    @Comment("카페 PK")
    private Integer id;

    @Column(name = "cafe_name", length = 100, nullable = false)
    @Comment("카페 이름")
    private String name;

    @Column(name = "phone_num", length = 30)
    @Comment("전화번호")
    private String phoneNum;

    @Column(name = "site_url", length = 255)
    @Comment("웹/블로그 URL")
    private String siteUrl;

    @Column(name = "address1", length = 120)
    @Comment("도로명 주소")
    private String address1;

    @Column(name = "address2", length = 120)
    @Comment("상세 주소")
    private String address2;

    @Column(name = "district", length = 60)
    @Comment("구/군")
    private String district;

    @Column(name = "city", length = 60)
    @Comment("시/도")
    private String city;

    @Column(name = "lat", precision = 10, scale = 7)
    @Comment("위도")
    private BigDecimal lat;

    @Column(name = "lng", precision = 10, scale = 7)
    @Comment("경도")
    private BigDecimal lng;

    @Column(name = "open_time")
    @Comment("영업 시작")
    private LocalTime openTime;

    @Column(name = "close_time")
    @Comment("영업 종료")
    private LocalTime closeTime;

    @Column(name = "parking_yn", nullable = false, columnDefinition = "TINYINT(1)")
    @Comment("주차 가능(0/1)")
    private boolean parkingYn;

    @Column(name = "hit_count", nullable = false)
    @Comment("조회 수")
    private int hitCount;

//    @Column(name = "like_count", nullable = false)
//    @Comment("좋아요 수")
//    private int likeCount;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME")
    @Comment("생성 시각")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME")
    @Comment("수정 시각")
    private LocalDateTime updatedAt;

    //관계구조

    @ManyToMany
    @JoinTable(
            name = "likedUsers",
            joinColumns = @JoinColumn(name = "cafe_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<SiteUser> likedUsers;

    // Review.cafe (N:1)

    // Bookmark.cafe (N:1)

    // CafeTag.cafe (N:1)  -> Cafe ⟷ Tag 다대다 중간 엔티티
    @OneToMany(mappedBy = "cafe")
    private List<CafeKeyword> cafeKeyword;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Comment("리뷰 목록")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Comment("북마크 목록")
    private List<Bookmark> bookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Comment("카페 키워드 목록")
    private List<CafeKeyword> cafeKeywords = new ArrayList<>();

    /* =========================
       생성자
       ========================= */
    protected Cafe() { }

    public Cafe(String name, String address1) {
        this.name = name;
        this.address1 = address1;
    }

    /* =========================
       편의 메서드
       ========================= */
    public void increaseHit() {
        this.hitCount += 1;
    }

    public boolean addLike(SiteUser user) {
        return this.likedUsers.add(user);
    }

    public boolean removeLike(SiteUser user) {
        return this.likedUsers.remove(user);
    }

    /* =========================
       Getter / Setter
       ========================= */
    public Integer getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNum() { return phoneNum; }
    public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }

    public String getSiteUrl() { return siteUrl; }
    public void setSiteUrl(String siteUrl) { this.siteUrl = siteUrl; }

    public String getAddress1() { return address1; }
    public void setAddress1(String address1) { this.address1 = address1; }

    public String getAddress2() { return address2; }
    public void setAddress2(String address2) { this.address2 = address2; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }

    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }

    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }

    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }

    public boolean isParkingYn() { return parkingYn; }
    public void setParkingYn(boolean parkingYn) { this.parkingYn = parkingYn; }

    public int getHitCount() { return hitCount; }
    public void setHitCount(int hitCount) { this.hitCount = hitCount; }

    public Set<SiteUser> getLikedUsers() { return likedUsers; }
    public List<Review> getReviews() { return reviews; }
    public List<Bookmark> getBookmarks() { return bookmarks; }
    public List<CafeKeyword> getCafeKeywords() { return cafeKeywords; }
}

