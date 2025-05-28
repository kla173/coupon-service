package com.example.repository;

import com.example.model.Coupon;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class CouponRepositoryTest {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void clearDatabase() {
        couponRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindCouponById() {
        // Given
        Coupon coupon = couponRepository.save(createUniqueCoupon());

        // When
        Optional<Coupon> foundCoupon = couponRepository.findById(coupon.getId());

        // Then
        assertThat(foundCoupon).isPresent();
        assertThat(foundCoupon.get().getCode()).isEqualTo(coupon.getCode());
        assertThat(foundCoupon.get().getCountry()).isEqualTo("PL");
        assertThat(foundCoupon.get().getMaxUses()).isEqualTo(5);
        assertThat(foundCoupon.get().getCurrentUses()).isEqualTo(0);
        assertThat(foundCoupon.get().getCreatedAt()).isNotNull();
        assertThat(foundCoupon.get().getVersion()).isEqualTo(0L);
    }

    @Test
    void shouldFindCouponByCodeIgnoringCase() {
        // Given
        Coupon coupon = couponRepository.save(createUniqueCoupon());

        // When
        Optional<Coupon> foundCoupon = couponRepository.findByCodeIgnoreCase(coupon.getCode());

        // Then
        assertThat(foundCoupon).isPresent();
        assertThat(foundCoupon.get().getCode()).isEqualTo(coupon.getCode());
        assertThat(foundCoupon.get().getCountry()).isEqualTo("PL");
    }

    @Test
    void shouldReturnEmptyWhenCouponNotFoundByCode() {
        Optional<Coupon> foundCoupon = couponRepository.findByCodeIgnoreCase("INVALID");

        assertThat(foundCoupon).isEmpty();
    }

    @Test
    void shouldUpdateCouponAndIncrementVersion() {
        // Given
        Coupon coupon = couponRepository.save(createUniqueCoupon());

        // When
        coupon.setCurrentUses(1);
        couponRepository.saveAndFlush(coupon);
        Optional<Coupon> updatedCoupon = couponRepository.findById(coupon.getId());

        // Then
        assertThat(updatedCoupon).isPresent();
        assertThat(updatedCoupon.get().getCurrentUses()).isEqualTo(1);
        assertThat(updatedCoupon.get().getVersion()).isEqualTo(1L);
    }

    @Test
    void shouldDeleteCoupon() {
        Coupon coupon = couponRepository.save(createUniqueCoupon());

        couponRepository.deleteById(coupon.getId());

        Optional<Coupon> deletedCoupon = couponRepository.findById(coupon.getId());
        assertThat(deletedCoupon).isEmpty();
    }

    @Test
    void shouldFailWhenSavingCouponWithDuplicateCode() {
        // Given
        String duplicateCode = "TEST2025";

        Coupon coupon1 = createCoupon(duplicateCode, "PL", 5);
        couponRepository.save(coupon1);

        Coupon coupon2 = createCoupon(duplicateCode, "US", 10);

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            couponRepository.save(coupon2);
            entityManager.flush();
        });
    }

    @Test
    void shouldFailWhenSavingCouponWithNullRequiredField() {
        Coupon coupon = Coupon.builder()
                .code(getUniqueCode())
                .maxUses(5)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            couponRepository.save(coupon);
            entityManager.flush();
        });
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldHandleOptimisticLockingFailure() {
        // Given
        Coupon savedCoupon = couponRepository.save(createUniqueCoupon());

        Optional<Coupon> couponInstance1 = couponRepository.findById(savedCoupon.getId());
        Optional<Coupon> couponInstance2 = couponRepository.findById(savedCoupon.getId());

        couponInstance1.get().setCurrentUses(1);
        couponRepository.saveAndFlush(couponInstance1.get());

        couponInstance2.get().setCurrentUses(2);

        // When & Then
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            couponRepository.saveAndFlush(couponInstance2.get());
        });
    }

    private String getUniqueCode() {
        return "TEST_" + UUID.randomUUID();
    }

    private Coupon createCoupon(String code, String country, int maxUses) {
        return Coupon.builder()
                .code(code)
                .country(country)
                .maxUses(maxUses)
                .build();
    }

    private Coupon createUniqueCoupon() {
        return createCoupon(getUniqueCode(), "PL", 5);
    }
}