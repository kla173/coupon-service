package com.example.exception;

import lombok.Getter;

@Getter
public class CouponException extends RuntimeException {
    private final String errorCode;

    public CouponException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}