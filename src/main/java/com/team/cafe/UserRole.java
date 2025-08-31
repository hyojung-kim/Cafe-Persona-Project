package com.team.cafe;

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