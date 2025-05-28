package com.example.model;

import java.time.Instant;

public record CouponResponse(Long id, String code, Instant createdAt, int maxUses, int currentUses, String country) {
}