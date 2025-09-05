package com.team.cafe.businessuser.sj;

import lombok.Data;

import java.util.List;

@Data
public class JusoApiResponse {
    private String status;                // 상태 코드
    private String message;               // 상태 메시지
    private JusoMeta meta;                // 페이징 정보
    private List<JusoAddress> results;    // 주소 리스트
}
