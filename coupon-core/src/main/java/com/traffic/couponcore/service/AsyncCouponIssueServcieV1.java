package com.traffic.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic.couponcore.component.DistributeLockExecutor;
import com.traffic.couponcore.exception.CouponIssueException;
import com.traffic.couponcore.exception.ErrorCode;
import com.traffic.couponcore.repository.redis.RedisRepository;
import com.traffic.couponcore.repository.redis.dto.CouponIssueRequest;
import com.traffic.couponcore.repository.redis.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.traffic.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.traffic.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServcieV1 {

    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponCacheService couponCacheService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void issue(Long couponId, Long userId) {
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        // 발급 기한 검증 --> 캐시로부터 발급 기한 검증
        coupon.CheckdateIssuableCoupon();
        //동시성 제어.
        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            couponIssueRedisService.checkCouponIssueQuantity(coupon, userId);
            issueRequest(couponId, userId);
        });

    }

    /*
      1. totalQuantity > redisRepository.sCard(key) // 쿠폰 발급 수량 해제
      2. !redisRepository.sIsMember(key, String.valueOf(userId)) // 중복 쿠폰 발급 요청 제어
      3. redisRepository.sAdd // 쿠폰 발급 요청 저장
      4. redisRepository.rPush // 쿠폰 발급 큐에 적재
     */


    private void issueRequest(Long couponId, Long userId) {
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);

        try {
            String value = objectMapper.writeValueAsString(issueRequest);
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));
            redisRepository.rPush(getIssueRequestQueueKey(), value);
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST, "inputL %s".formatted(issueRequest));
        }

    }

}
