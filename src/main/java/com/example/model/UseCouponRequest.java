package com.example.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UseCouponRequest(@NotBlank(message = "User ID cannot be empty") String userId,
                               @NotBlank(message = "IP address cannot be empty") @Pattern(regexp = "^([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})|localhost$", message = "Invalid IP address format") String ipAddress) {
}