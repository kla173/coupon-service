package com.example.service.impl;

import com.example.constant.ErrorCodes;
import com.example.exception.CouponException;
import com.example.model.CreateCouponRequest;
import com.example.model.Coupon;
import com.example.model.CouponResponse;
import com.example.model.UseCouponRequest;
import com.example.model.UseCouponResponse;
import com.example.model.CouponUsage;

import com.example.repository.CouponRepository;
import com.example.repository.CouponUsageRepository;
import com.example.service.CountryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository usageRepository;

    @Mock
    private CountryService countryService;

    @InjectMocks
    private CouponServiceImpl couponService;

    private final String CODE = "TEST2025";
    private final String USER_ID = "user123";
    private final String IP = "1.2.3.4";
    private final String COUNTRY = "PL";

    @Test
    void shouldCreateCouponSuccessfully() {
        // Given
        CreateCouponRequest request = new CreateCouponRequest(CODE, 5, COUNTRY);
        when(couponRepository.findByCodeIgnoreCase(CODE)).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CouponResponse response = couponService.createCoupon(request);

        // Then
        assertThat(response.code()).isEqualTo(CODE.toUpperCase());
        assertThat(response.maxUses()).isEqualTo(5);
        assertThat(response.country()).isEqualTo(COUNTRY);
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void shouldThrowExceptionWhenCouponAlreadyExists() {
        // Given
        CreateCouponRequest request = new CreateCouponRequest(CODE, 5, COUNTRY);
        when(couponRepository.findByCodeIgnoreCase(CODE)).thenReturn(Optional.of(new Coupon()));

        // Then
        assertThrows(CouponException.class, () -> couponService.createCoupon(request));
    }

    @Test
    void shouldUseCouponSuccessfully() {
        // Given
        Coupon coupon = getTestCoupon();
        UseCouponRequest request = new UseCouponRequest(USER_ID, IP);

        when(couponRepository.findByCodeIgnoreCase(CODE)).thenReturn(Optional.of(coupon));
        when(countryService.getCountryFromIp(IP)).thenReturn(Optional.of(COUNTRY));
        when(usageRepository.existsByCouponAndUserId(coupon, USER_ID)).thenReturn(false);

        // When
        UseCouponResponse response = couponService.useCoupon(CODE, request);

        // Then
        assertThat(response.message()).isEqualTo(ErrorCodes.SUCCESSFUL_USE_MESSAGE);
        verify(couponRepository).save(any(Coupon.class));
        verify(usageRepository).save(any(CouponUsage.class));
    }

    @Test
    void shouldThrowWhenCouponNotFound() {
        // Given
        when(couponRepository.findByCodeIgnoreCase(CODE)).thenReturn(Optional.empty());
        UseCouponRequest request = new UseCouponRequest(USER_ID, IP);

        // When & Then
        assertThrows(CouponException.class, () -> couponService.useCoupon(CODE, request));
    }

    @Test
    void shouldThrowWhenCountryUnavailable() {
        // Given
        Coupon coupon = getTestCoupon();
        when(couponRepository.findByCodeIgnoreCase(CODE)).thenReturn(Optional.of(coupon));
        when(countryService.getCountryFromIp(IP)).thenReturn(Optional.empty());
        UseCouponRequest request = new UseCouponRequest(USER_ID, IP);

        // When & Then
        assertThrows(CouponException.class, () -> couponService.useCoupon(CODE, request));
    }

    @Test
    void shouldThrowWhenCountryMismatch() {
        // Given
        Coupon coupon = getTestCoupon();
        when(couponRepository.findByCodeIgnoreCase(CODE)).thenReturn(Optional.of(coupon));
        when(countryService.getCountryFromIp(IP)).thenReturn(Optional.of("US"));
        UseCouponRequest request = new UseCouponRequest(USER_ID, IP);

        // When & Then
        assertThrows(CouponException.class, () -> couponService.useCoupon(CODE, request));
    }

    @Test
    void shouldThrowWhenCouponLimitReached() {
        // Given
        Coupon coupon = getTestCoupon();
        coupon.setCurrentUses(3);
        when(couponRepository.findByCodeIgnoreCase(CODE)).thenReturn(Optional.of(coupon));
        when(countryService.getCountryFromIp(IP)).thenReturn(Optional.of(COUNTRY));
        UseCouponRequest request = new UseCouponRequest(USER_ID, IP);

        // When & Then
        assertThrows(CouponException.class, () -> couponService.useCoupon(CODE, request));
    }

    @Test
    void shouldThrowWhenUserAlreadyUsedCoupon() {
        // Given
        Coupon coupon = getTestCoupon();
        when(couponRepository.findByCodeIgnoreCase(CODE)).thenReturn(Optional.of(coupon));
        when(countryService.getCountryFromIp(IP)).thenReturn(Optional.of(COUNTRY));
        when(usageRepository.existsByCouponAndUserId(coupon, USER_ID)).thenReturn(true);
        UseCouponRequest request = new UseCouponRequest(USER_ID, IP);

        // When & Then
        assertThrows(CouponException.class, () -> couponService.useCoupon(CODE, request));
    }

    private Coupon getTestCoupon() {
        return Coupon.builder()
                .id(1L)
                .code(CODE)
                .country(COUNTRY)
                .maxUses(3)
                .currentUses(1)
                .createdAt(Instant.now())
                .version(0L)
                .build();
    }
}