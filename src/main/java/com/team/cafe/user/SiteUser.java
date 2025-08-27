package com.team.cafe.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@Getter
@Setter
@Entity
public class SiteUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userid;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String password;

//    @Enumerated(EnumType.STRING)
//    private Role role;
//
//    @Builder
//    public SiteUser(String username, String password, Role role) {
//        this.username = username;
//        this.password= password;
//        this.role= role;
//    }
}

