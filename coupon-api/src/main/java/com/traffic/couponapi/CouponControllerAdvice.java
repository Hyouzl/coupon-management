package com.traffic.couponapi;

import com.traffic.couponapi.controller.dto.CouponIssueResponseDTO;
import com.traffic.couponcore.exception.CouponIssueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CouponControllerAdvice {

    @ExceptionHandler(CouponIssueException.class)
    public CouponIssueResponseDTO couponExceptionHandler(CouponIssueException exception) {
        return new CouponIssueResponseDTO(false, exception.getMessage());
    }
}
