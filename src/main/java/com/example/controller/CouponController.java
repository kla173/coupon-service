package com.example.controller;

import com.example.model.CouponResponse;
import com.example.model.CreateCouponRequest;
import com.example.model.UseCouponRequest;
import com.example.model.UseCouponResponse;
import com.example.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{code}/use")
    public ResponseEntity<UseCouponResponse> useCoupon(@PathVariable String code, @Valid @RequestBody UseCouponRequest request) {
        UseCouponResponse response = couponService.useCoupon(code, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}