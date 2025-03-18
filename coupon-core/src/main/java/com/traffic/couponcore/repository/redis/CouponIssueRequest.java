package com.traffic.couponcore.repository.redis;

import com.traffic.couponcore.exception.CouponIssueException;
import com.traffic.couponcore.exception.ErrorCode;
import com.traffic.couponcore.model.Coupon;

public enum CouponIssueRequest {
    SUCCESS(1),
    DUPLICATED_COUPON_ISSUE(2),
    INVALID_COUPON_ISSUE_QUANTITY(3);

    CouponIssueRequest(int code) {

    }

    public static CouponIssueRequest find(String code) {
        int codeValue = Integer.valueOf(code);
        if (codeValue == 1) return SUCCESS;
        if (codeValue == 2) return DUPLICATED_COUPON_ISSUE;
        if (codeValue == 3) return INVALID_COUPON_ISSUE_QUANTITY;
        throw new IllegalArgumentException("존재하지 않는 코드입니다.");
    }

    public static void checkRequestResult(CouponIssueRequest code) {
        if (code == INVALID_COUPON_ISSUE_QUANTITY) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다.");
        }
        if (code == DUPLICATED_COUPON_ISSUE) {
            throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다.");
        }
    }
}
