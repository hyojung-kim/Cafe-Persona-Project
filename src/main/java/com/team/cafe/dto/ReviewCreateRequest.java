package com.team.cafe.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest (
        @NotNull Long cafeId,
        @NotNull @Size (min = 50, max = 4000) String content,
        @NotNull @DecimalMin("0, 0") @DecimalMax("5, 0") Double rating
) {
}
