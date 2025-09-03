package com.team.cafe.businessuser.sj;

import lombok.Data;

@Data
public class JusoMeta {
    private int totalCount;       // 총 검색 결과 수
    private int currentPage;      // 현재 페이지
    private int countPerPage;     // 페이지당 출력 수
}
