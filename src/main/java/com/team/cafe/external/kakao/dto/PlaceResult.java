package com.team.cafe.external.kakao.dto;

public record PlaceResult(
        String id,
        String name,
        String address,
        String roadAddress,
        double lat,
        double lng,
        String url,
        Integer distanceMeters // null 또는 m단위
) {}