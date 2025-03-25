package com.traffic.couponcore.service;


import com.traffic.couponcore.repository.redis.RedisRepository;
import com.traffic.couponcore.repository.redis.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServcieV2 {

    private final RedisRepository redisRepository;
    private final CouponCacheService couponCacheService;


    public void issue (Long couponId, Long userId) {

        // 레디스로 들어가는 트래픽을 로컬로 돌리기 -> 로컬 캐시로 변경
        // 로컬 캐시가 존재하는 동안은 레디스로 들어가는 트래픽 제한 가능. -> 성능 최적화
        CouponRedisEntity couponRedisEntity = couponCacheService.getCouponLocalCache(couponId);
        couponRedisEntity.CheckdateIssuableCoupon();
        issueRequest(couponId, userId, couponRedisEntity.totalQuantity());
    }

    public void issueRequest(Long couponId, Long userId, Integer totalCouponQuantity) {
        if (totalCouponQuantity == null) {
            redisRepository.issueRequest(couponId, userId, Integer.MAX_VALUE);
        }
        redisRepository.issueRequest(couponId, userId, totalCouponQuantity);
    }

}
