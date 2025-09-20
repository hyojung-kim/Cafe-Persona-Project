package com.team.cafe.businessuser.sj.owner.cafe;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;


@Getter
@Setter
public class CafeRegisterRequest {
    private String name;
    private String phoneNum;
    private String siteUrl;
    private String address1;
    private String address2;
    private String district;
    private String city;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean parkingYn;
    private String intro;

}

