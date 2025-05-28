package com.example.service.impl.service;

import com.example.constant.ErrorCodes;
import com.example.exception.CouponException;
import com.example.model.Coupon;
import com.example.model.CreateCouponRequest;
import com.example.model.UseCouponRequest;
import com.example.model.UseCouponResponse;
import com.example.repository.CouponRepository;
import com.example.repository.CouponUsageRepository;
import com.example.service.CountryService;
import com.example.service.impl.CouponServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class CouponServiceIntegrationTest {
    @Autowired
    private CouponServiceImpl couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUsageRepository usageRepository;

    @MockitoBean
    private CountryService countryService;

    @BeforeEach
    void setUp() {
        usageRepository.deleteAll();
        couponRepository.deleteAll();
    }

    @Test
    void shouldCreateAndUseCouponSuccessfully() {
        // Given
        CreateCouponRequest createRequest = new CreateCouponRequest("TESTCODE", 3, "PL");
        couponService.createCoupon(createRequest);

        when(countryService.getCountryFromIp("1.1.1.1")).thenReturn(Optional.of("PL"));

        UseCouponRequest useRequest = new UseCouponRequest("user123", "1.1.1.1");

        // When
        UseCouponResponse response = couponService.useCoupon("TESTCODE", useRequest);

        // Then
        assertThat(response.message()).isEqualTo(ErrorCodes.SUCCESSFUL_USE_MESSAGE);
        Coupon coupon = couponRepository.findByCodeIgnoreCase("TESTCODE").orElseThrow();
        assertThat(coupon.getCurrentUses()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenCouponDoesNotExist() {
        UseCouponRequest request = new UseCouponRequest("user1", "1.1.1.1");

        assertThrows(CouponException.class, () -> couponService.useCoupon("NOT_EXISTING", request));
    }

    @Test
    void shouldThrowWhenCountryServiceUnavailable() {
        // Given
        couponService.createCoupon(new CreateCouponRequest("TEST", 1, "PL"));
        when(countryService.getCountryFromIp("2.2.2.2")).thenReturn(Optional.empty());

        UseCouponRequest request = new UseCouponRequest("user1", "2.2.2.2");

        // When & Then
        assertThrows(CouponException.class, () -> couponService.useCoupon("TEST", request));
    }

    @Test
    void shouldThrowWhenWrongCountry() {
        // Given
        couponService.createCoupon(new CreateCouponRequest("TEST", 1, "PL"));
        when(countryService.getCountryFromIp("3.3.3.3")).thenReturn(Optional.of("DE"));

        UseCouponRequest request = new UseCouponRequest("user1", "3.3.3.3");

        // When & Then
        assertThrows(CouponException.class, () -> couponService.useCoupon("TEST", request));
    }

    @Test
    void shouldThrowWhenUserAlreadyUsedCoupon() {
        // Given
        couponService.createCoupon(new CreateCouponRequest("SINGLEUSE", 2, "PL"));
        when(countryService.getCountryFromIp("4.4.4.4")).thenReturn(Optional.of("PL"));

        UseCouponRequest request = new UseCouponRequest("userX", "4.4.4.4");
        couponService.useCoupon("SINGLEUSE", request);

        // When & Then
        assertThrows(CouponException.class, () -> couponService.useCoupon("SINGLEUSE", request));
    }

    @Test
    void shouldThrowWhenCouponLimitReached() {
        // Given
        couponService.createCoupon(new CreateCouponRequest("LIMIT", 1, "PL"));
        when(countryService.getCountryFromIp("5.5.5.5")).thenReturn(Optional.of("PL"));

        couponService.useCoupon("LIMIT", new UseCouponRequest("user1", "5.5.5.5"));

        UseCouponRequest anotherUser = new UseCouponRequest("user2", "5.5.5.5");

        // When & Then
        assertThrows(CouponException.class, () -> couponService.useCoupon("LIMIT", anotherUser));
    }
}