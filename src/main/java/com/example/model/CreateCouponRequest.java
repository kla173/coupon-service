package com.example.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCouponRequest(@NotBlank(message = "Coupon code cannot be empty") String code,
                                  @NotNull(message = "Maximum uses must be specified") @Min(value = 1, message = "Maximum uses must be greater than 0") Integer maxUses,
                                  @NotBlank(message = "Country code cannot be empty") String country) {
}