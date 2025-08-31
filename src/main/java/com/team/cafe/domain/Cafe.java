package com.team.cafe.domain;

import jakarta.persistence.*;  // JPA 관련 어노테이션들 (Entity, Id, Column 등)
import lombok.*;              // 롬복: Getter, Setter, 생성자, Builder 등을 자동 생성

/**
 * Cafe 엔티티
 * - 카페 정보를 저장하는 데이터베이스 테이블과 매핑되는 클래스
 * - @Entity: JPA가 이 클래스를 "DB 테이블과 연결된 엔티티"로 인식
 */
@Entity
@Getter  // 모든 필드의 Getter 메서드 자동 생성
@Setter  // 모든 필드의 Setter 메서드 자동 생성
@NoArgsConstructor   // 파라미터 없는 기본 생성자 자동 생성 (JPA가 내부적으로 필요)
@AllArgsConstructor  // 모든 필드를 파라미터로 받는 생성자 자동 생성
@Builder             // 빌더 패턴 지원 (가독성 있게 객체 생성 가능)
@Table(indexes = @Index(name="idx_cafe_name", columnList = "name"))
// @Table: 이 엔티티가 어떤 테이블과 매핑될지 설정 가능
// indexes: name 컬럼에 인덱스를 생성해서 검색 성능 향상
public class Cafe {

    @Id // 기본키(Primary Key) 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY 전략: DB의 Auto Increment 기능 사용 (MySQL, MariaDB 등에서 흔히 사용)
    private Long id;

    @Column(nullable=false, length=120)
    // name 컬럼은 null 불가, 최대 길이 120자로 제한
    private String name;

    @Column(length=255)
    // address 컬럼은 최대 길이 255자 (nullable=true가 기본값 → 주소가 없어도 저장 가능)
    private String address;

    // 위도(latitude)
    private Double lat;

    // 경도(longitude)
    private Double lng;

    // 카페 카테고리(예: "커피전문점", "디저트카페")
    // 단일 문자열로 저장하지만, 나중에 확장이 필요하다면 별도 테이블로 분리 가능
    private String category;
}
