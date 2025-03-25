package com.traffic.couponcore.service;

import com.traffic.couponcore.model.Coupon;
import com.traffic.couponcore.repository.redis.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponCacheService {

    private final CouponIssueService couponIssueService;


    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(Long couponId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);
        return new CouponRedisEntity(coupon);
    }


    // 기본적으로 로컬 캐시에서 조회하고, 없을 시에는 레디스캐시 조회
    @Cacheable(cacheNames = "coupon", cacheManager = "localcacheManager")
    public CouponRedisEntity getCouponLocalCache(Long couponId) {
        return proxy().getCouponCache(couponId);
    }

    @CachePut(cacheNames = "coupon")
    public CouponRedisEntity putCouponCache(Long couponId) {
        return getCouponCache(couponId);

    }

    @CachePut(cacheNames = "coupon", cacheManager = "localcacheManager")
    public CouponRedisEntity putCouponLocalCache(Long couponId) {
        return getCouponLocalCache(couponId);
    }

    private CouponCacheService proxy() {
        return ((CouponCacheService) AopContext.currentProxy());
    }

}
