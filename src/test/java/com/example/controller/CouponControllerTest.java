package com.example.controller;

import com.example.model.CouponResponse;
import com.example.model.CreateCouponRequest;
import com.example.model.UseCouponRequest;
import com.example.model.UseCouponResponse;
import com.example.service.CouponService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CouponService couponService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateCouponSuccessfully() throws Exception {
        // Given
        CreateCouponRequest request = new CreateCouponRequest("TEST2025", 5, "PL");

        CouponResponse response = new CouponResponse(1L, "TEST2025", Instant.parse("2025-05-26T20:00:00Z"), 5, 0, "PL");

        when(couponService.createCoupon(any(CreateCouponRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("TEST2025"))
                .andExpect(jsonPath("$.createdAt").value("2025-05-26T20:00:00Z"))
                .andExpect(jsonPath("$.maxUses").value(5))
                .andExpect(jsonPath("$.currentUses").value(0))
                .andExpect(jsonPath("$.country").value("PL"));
    }

    @Test
    void shouldUseCouponSuccessfully() throws Exception {
        // Given
        String code = "TEST2025";
        UseCouponRequest request = new UseCouponRequest("user123", "192.168.1.1");

        UseCouponResponse response = new UseCouponResponse("Coupon used successfully");

        when(couponService.useCoupon(eq(code), any(UseCouponRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/coupons/{code}/use", code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("Coupon used successfully"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateCouponRequestIsInvalid() throws Exception {
        // Given
        CreateCouponRequest invalidRequest = new CreateCouponRequest(null, 5, "PL");

        // When & Then
        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnInternalServerErrorWhenCouponServiceThrowsException() throws Exception {
        // Given
        String code = "INVALID";
        UseCouponRequest request = new UseCouponRequest("user123", "192.168.1.1");

        when(couponService.useCoupon(eq(code), any(UseCouponRequest.class)))
                .thenThrow(new RuntimeException("Coupon not found"));

        // When & Then
        mockMvc.perform(post("/coupons/{code}/use", code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}