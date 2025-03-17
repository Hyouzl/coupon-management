package com.traffic.couponcore.service;


import com.traffic.couponcore.repository.redis.RedisRepositroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.traffic.couponcore.util.CouponRedisUtils.getIssueRequestKey;

@RequiredArgsConstructor
@Service
public class CouponIssueRedisService {

    private final RedisRepositroy redisRepositroy;

    public boolean avaliableTotalIssueQuantity(Integer totalQuantity, Long couponId) {
        if(totalQuantity == null) {
            return true; // 발급 수량 제한 x
        }
        String key = getIssueRequestKey(couponId);
        return totalQuantity > redisRepositroy.sCard(key);
    }

    public boolean avaliableUserIssueQuantity(Long couponId, Long userId) {
        String key = getIssueRequestKey(couponId);
        boolean result = redisRepositroy.sIsMember(key, String.valueOf(userId));
        System.out.println(result);
        return !result;
    }

}
