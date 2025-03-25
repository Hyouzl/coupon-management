package com.traffic.couponcore.repository.redis.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.traffic.couponcore.exception.CouponIssueException;
import com.traffic.couponcore.exception.ErrorCode;
import com.traffic.couponcore.model.Coupon;
import com.traffic.couponcore.model.CouponType;

import java.time.LocalDateTime;

public record CouponRedisEntity(Long id,
                                CouponType couponType,
                                Integer totalQuantity,
                                boolean avaliableIssueQuantity,
                                @JsonSerialize(using = LocalDateTimeSerializer.class)
                                @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                LocalDateTime dateIssueStart,
                                @JsonSerialize(using = LocalDateTimeSerializer.class)
                                @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                LocalDateTime dateIssueEnd) {

    public CouponRedisEntity(Coupon coupon) {
        this(
                coupon.getId(),
                coupon.getCouponType(),
                coupon.getTotalQuantity(),
                coupon.avaliableIssueQuantity(),
                coupon.getDateIssueStart(),
                coupon.getDateIssueEnd()
        );
    }

    private boolean avaliableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    public void CheckdateIssuableCoupon() {

        if(!avaliableIssueQuantity) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량이 없습니다.");
        }

        if(!avaliableIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE, "발급 가능한 일자가 아닙니다.");
        }
    }


}
