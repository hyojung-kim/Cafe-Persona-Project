package com.team.cafe.businessuser.sj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JusoAddress {
    private String roadAddr;      // 도로명 주소
    private String jibunAddr;     // 지번 주소
    private String zipNo;         // 우편번호
    private String admCd;         // 행정구역 코드
    private String rn;            // 도로명
    private String bdNm;          // 건물명
    private String siNm;          // 시
    private String sggNm;         // 구/군
    private String emdNm;         // 읍/면/동
}
