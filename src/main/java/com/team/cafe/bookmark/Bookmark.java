package com.team.cafe.bookmark;

import com.team.cafe.list.Cafe;
import com.team.cafe.user.sjhy.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "bookmark", //인덱스 용 !
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_bookmark_user_cafe", columnNames = {"user_id", "cafe_id"})
        },
        indexes = {
                @Index(name = "idx_bookmark_user", columnList = "user_id"),
                @Index(name = "idx_bookmark_cafe", columnList = "cafe_id")
        })

public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id", columnDefinition = "BIGINT UNSIGNED")
    @Comment("북마크 PK")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_bookmark_user"))
    @Comment("북마크한 사용자")
    private SiteUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_bookmark_cafe"))
    @Comment("북마크된 카페")
    private Cafe cafe;

    @Column(name = "created_at", nullable = false)
    @Comment("북마크 생성일시")
    private LocalDateTime createdAt;

//    @PrePersist
//    protected void onCreate() {
//        this.createdAt = LocalDateTime.now();
//    }
}