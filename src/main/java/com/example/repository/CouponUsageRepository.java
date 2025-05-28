package com.example.repository;

import com.example.model.Coupon;
import com.example.model.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    boolean existsByCouponAndUserId(Coupon coupon, String userId);
}