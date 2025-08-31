package com.team.cafe.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = @Index(name="idx_cafe_name", columnList = "name"))
public class Cafe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String name;

    @Column(length=255)
    private String address;

    private Double lat;
    private Double lng;

    // 단일 카테고리(필요 시 다중 테이블로 확장)
    private String category;
}
