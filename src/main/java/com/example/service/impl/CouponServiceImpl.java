package com.example.service.impl;

import com.example.constant.ErrorCodes;
import com.example.exception.CouponException;
import com.example.model.*;
import com.example.repository.CouponRepository;
import com.example.repository.CouponUsageRepository;
import com.example.service.CountryService;
import com.example.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements CouponService {
    private static final int ONE_USAGE = 1;
    private final CouponRepository couponRepository;
    private final CouponUsageRepository usageRepository;
    private final CountryService countryService;

    @Override
    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request) {
        log.info("Creating coupon with code: {}", request.code());
        String codeUpperCase = request.code().toUpperCase();
        if (couponRepository.findByCodeIgnoreCase(codeUpperCase).isPresent()) {
            log.warn("Coupon with code {} already exists", codeUpperCase);
            throw new CouponException(ErrorCodes.COUPON_CODE_ALREADY_EXISTS_MESSAGE, ErrorCodes.COUPON_CODE_ALREADY_EXISTS_CODE);
        }
        Coupon coupon = Coupon.builder()
                .code(codeUpperCase)
                .maxUses(request.maxUses())
                .country(request.country())
                .build();
        coupon = couponRepository.save(coupon);
        log.info("Coupon created with ID: {}", coupon.getId());
        return mapToResponse(coupon);
    }

    @Override
    @Transactional
    public UseCouponResponse useCoupon(String code, UseCouponRequest request) {
        log.info("Attempting to use coupon {} by user {}", code, request.userId());

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new CouponException(ErrorCodes.COUPON_NOT_FOUND_MESSAGE, ErrorCodes.COUPON_NOT_FOUND_CODE));

        Optional<String> userCountryOpt = countryService.getCountryFromIp(request.ipAddress());
        String userCountry = userCountryOpt.orElseThrow(() -> {
            log.error("Unable to verify country for IP: {}", request.ipAddress());
            return new CouponException(ErrorCodes.COUNTRY_SERVICE_UNAVAILABLE_MESSAGE, ErrorCodes.COUNTRY_SERVICE_UNAVAILABLE_CODE);
        });

        if (!coupon.getCountry().equals(userCountry)) {
            log.warn("Coupon {} not valid for country {} (expected: {})", code, userCountry, coupon.getCountry());
            throw new CouponException(ErrorCodes.INVALID_COUNTRY_MESSAGE, ErrorCodes.INVALID_COUNTRY_CODE);
        }

        if (coupon.getCurrentUses() >= coupon.getMaxUses()) {
            log.warn("Coupon {} has reached its usage limit", code);
            throw new CouponException(ErrorCodes.COUPON_LIMIT_REACHED_MESSAGE, ErrorCodes.COUPON_LIMIT_REACHED_CODE);
        }

        if (usageRepository.existsByCouponAndUserId(coupon, request.userId())) {
            log.warn("User {} already used coupon {}", request.userId(), code);
            throw new CouponException(ErrorCodes.COUPON_ALREADY_USED_MESSAGE, ErrorCodes.COUPON_ALREADY_USED_CODE);
        }

        coupon.setCurrentUses(coupon.getCurrentUses() + ONE_USAGE);
        couponRepository.save(coupon);

        CouponUsage usage = CouponUsage.builder()
                .coupon(coupon)
                .userId(request.userId())
                .build();
        usageRepository.save(usage);

        log.info("Coupon {} used successfully by user {}", code, request.userId());
        return new UseCouponResponse(ErrorCodes.SUCCESSFUL_USE_MESSAGE);
    }

    private CouponResponse mapToResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(), coupon.getCode(), coupon.getCreatedAt(),
                coupon.getMaxUses(), coupon.getCurrentUses(), coupon.getCountry());
    }
}