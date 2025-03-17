package com.traffic.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic.couponcore.component.DistributeLockExecutor;
import com.traffic.couponcore.exception.CouponIssueException;
import com.traffic.couponcore.exception.ErrorCode;
import com.traffic.couponcore.model.Coupon;
import com.traffic.couponcore.repository.redis.RedisRepositroy;
import com.traffic.couponcore.repository.redis.dto.CouponIssueRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.traffic.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.traffic.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServcie {

    private final RedisRepositroy redisRepositroy;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void issue(Long couponId, Long userId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);

        if (!coupon.avaliableIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE, "발급 가능한 일자가 아닙니다.");
        }

        // 레디스 동시성 제어.
        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            if (!couponIssueRedisService.avaliableTotalIssueQuantity(coupon.getTotalQuantity(), userId)) {
                throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다.");
            }

            if (!couponIssueRedisService.avaliableUserIssueQuantity(couponId, userId)) {
                throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급 요청이 처리됐습니다.");
            }

            issueRequest(couponId, userId);
        });

    }

    private void issueRequest(Long couponId, Long userId) {
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);

        try {
            String value = objectMapper.writeValueAsString(issueRequest);
            redisRepositroy.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));
            redisRepositroy.rPush(getIssueRequestQueueKey(), value);
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST, "inputL %s".formatted(issueRequest));
        }

    }



}
