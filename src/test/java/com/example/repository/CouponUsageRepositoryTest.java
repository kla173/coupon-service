package com.example.repository;

import com.example.model.Coupon;
import com.example.model.CouponUsage;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class CouponUsageRepositoryTest {


    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUsageRepository couponUsageRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void clearDatabase() {
        couponUsageRepository.deleteAll();
        couponRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindCouponUsage() {
        // Given
        Coupon coupon = createCoupon();
        coupon = couponRepository.save(coupon);

        CouponUsage usage = CouponUsage.builder()
                .coupon(coupon)
                .userId("user-123")
                .build();

        // When
        CouponUsage saved = couponUsageRepository.save(usage);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsedAt()).isNotNull();
        assertThat(saved.getCoupon()).isEqualTo(coupon);
        assertThat(saved.getUserId()).isEqualTo("user-123");
    }

    @Test
    void shouldReturnTrueWhenUsageExists() {
        // Given
        Coupon coupon = couponRepository.save(createCoupon());
        couponUsageRepository.save(
                CouponUsage.builder()
                        .coupon(coupon)
                        .userId("user-abc")
                        .build());

        // When
        boolean exists = couponUsageRepository.existsByCouponAndUserId(coupon, "user-abc");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUsageDoesNotExist() {
        // Given
        Coupon coupon = couponRepository.save(createCoupon());

        // When
        boolean exists = couponUsageRepository.existsByCouponAndUserId(coupon, "user-does-not-exist");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldFailWhenSavingUsageWithoutCoupon() {
        // Given
        CouponUsage usage = CouponUsage.builder()
                .userId("user-xyz")
                .build();

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            couponUsageRepository.save(usage);
            entityManager.flush();
        });
    }

    @Test
    void shouldFailWhenSavingUsageWithoutUserId() {
        // Given
        Coupon coupon = couponRepository.save(createCoupon());

        CouponUsage usage = CouponUsage.builder()
                .coupon(coupon)
                .build();

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            couponUsageRepository.save(usage);
            entityManager.flush();
        });
    }

    private Coupon createCoupon() {
        return Coupon.builder()
                .code("CODE_" + UUID.randomUUID())
                .country("PL")
                .maxUses(5)
                .build();
    }
}