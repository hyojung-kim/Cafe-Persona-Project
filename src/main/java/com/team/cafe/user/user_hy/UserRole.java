package com.team.cafe.user.user_hy;

import lombok.Getter;


@Getter
public enum UserRole {
    BUSINESS("ROLE_BUSINESS"),
    USER("ROLE_USER");

    UserRole(String value) {
        this.value = value;
    }

    private String value;
}