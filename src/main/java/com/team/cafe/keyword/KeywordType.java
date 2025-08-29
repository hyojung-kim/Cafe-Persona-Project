package com.team.cafe.keyword;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "keyword_type",
        uniqueConstraints = @UniqueConstraint(name="uk_keyword_type_name", columnNames = "type_name"))
@Getter
@Setter
public class KeywordType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="type_name", length=30, nullable=false)
    private String typeName; // persona / space / menu 등

    @OneToMany(mappedBy = "type", fetch = FetchType.LAZY)
    private List<Keyword> keywords = new ArrayList<>();
}