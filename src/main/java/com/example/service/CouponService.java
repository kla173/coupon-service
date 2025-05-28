package com.example.service;

import com.example.model.CouponResponse;
import com.example.model.CreateCouponRequest;
import com.example.model.UseCouponRequest;
import com.example.model.UseCouponResponse;

public interface CouponService {
    CouponResponse createCoupon(CreateCouponRequest request);

    UseCouponResponse useCoupon(String code, UseCouponRequest request);
}