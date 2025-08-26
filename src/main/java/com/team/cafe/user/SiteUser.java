package com.team.cafe.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class SiteUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userid;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    private String password_hash;

//    @Enumerated(EnumType.STRING)
//    private Role role;

//    @Builder
//    public SiteUser(String username, String password_hash, Role role) {
//        this.username = username;
//        this.password_hash= password_hash;
//        this.role= role;
//    }
}

