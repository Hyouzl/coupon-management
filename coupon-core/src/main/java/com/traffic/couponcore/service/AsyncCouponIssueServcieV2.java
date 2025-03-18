package com.traffic.couponcore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic.couponcore.repository.redis.RedisRepositroy;
import com.traffic.couponcore.repository.redis.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServcieV2 {

    private final RedisRepositroy redisRepositroy;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponCacheService couponCacheService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public void issue (Long couponId, Long userId) {
        CouponRedisEntity couponRedisEntity = couponCacheService.getCouponCache(couponId);
        couponRedisEntity.CheckdateIssuableCoupon();
        issueRequest(couponId, userId, couponRedisEntity.totalQuantity());
    }

    public void issueRequest(Long couponId, Long userId, Integer totalCouponQuantity) {
        if (totalCouponQuantity == null) {
            redisRepositroy.issueRequest(couponId, userId, Integer.MAX_VALUE);
        }
        redisRepositroy.issueRequest(couponId, userId, totalCouponQuantity);
    }

}
