package com.example.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorCodes {
    public static final String COUPON_NOT_FOUND_MESSAGE = "Coupon not found";
    public static final String COUPON_NOT_FOUND_CODE = "COUPON_NOT_FOUND";
    public static final String COUPON_LIMIT_REACHED_MESSAGE = "Coupon usage limit reached";
    public static final String COUPON_LIMIT_REACHED_CODE = "COUPON_EXHAUSTED";
    public static final String COUPON_ALREADY_USED_MESSAGE = "Coupon already used by this user";
    public static final String COUPON_ALREADY_USED_CODE = "COUPON_ALREADY_USED";
    public static final String COUPON_CODE_ALREADY_EXISTS_MESSAGE = "Coupon with code already exists";
    public static final String COUPON_CODE_ALREADY_EXISTS_CODE = "COUPON_CODE_ALREADY_EXISTS";
    public static final String INVALID_COUNTRY_MESSAGE = "Coupon not valid for your country";
    public static final String INVALID_COUNTRY_CODE = "INVALID_COUNTRY";
    public static final String COUNTRY_SERVICE_UNAVAILABLE_MESSAGE = "Unable to verify country at this time";
    public static final String COUNTRY_SERVICE_UNAVAILABLE_CODE = "COUNTRY_SERVICE_UNAVAILABLE";
    public static final String OPTIMISTIC_LOCKING_FAILURE_MESSAGE = "Coupon was modified by another transaction";
    public static final String OPTIMISTIC_LOCKING_FAILURE_CODE = "OPTIMISTIC_LOCKING_FAILURE";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "An unexpected error occurred";
    public static final String INTERNAL_SERVER_ERROR_CODE = "INTERNAL_SERVER_ERROR";
    public static final String SUCCESSFUL_USE_MESSAGE = "Coupon used successfully";
}