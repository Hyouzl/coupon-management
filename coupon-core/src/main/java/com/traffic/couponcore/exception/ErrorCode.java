package com.traffic.couponcore.exception;

public enum ErrorCode {


    INVALID_COUPON_ISSUE_QUANTITY("발급 가능 수량을 초과합니다."),
    INVALID_COUPON_ISSUE_DATE("발급 가능 기한이 유효하지 않습니다."),
    COUPON_NOT_EXIST("존재하지 않는 쿠폰입니다."),
    DUPLICATED_COUPON_ISSUE("이미 발급된 쿠폰입니다."),
    FAIL_COUPON_ISSUE_REQUEST("쿠폰 발급에 실패하였습니다.")

    ;

    public final String message;

    ErrorCode(String message) {
        this.message = message;
    }


}
