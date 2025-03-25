package com.traffic.couponcore.service;


import com.traffic.couponcore.exception.CouponIssueException;
import com.traffic.couponcore.exception.ErrorCode;

import com.traffic.couponcore.repository.redis.RedisRepository;
import com.traffic.couponcore.repository.redis.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.traffic.couponcore.util.CouponRedisUtils.getIssueRequestKey;

@RequiredArgsConstructor
@Service
public class CouponIssueRedisService {

    private final RedisRepository redisRepository;


    public void checkCouponIssueQuantity(CouponRedisEntity coupon, Long userId) {
        if (!avaliableTotalIssueQuantity(coupon.totalQuantity(), userId)) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다.");
        }

        if (!avaliableUserIssueQuantity(coupon.id(), userId)) {
            throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급 요청이 처리됐습니다.");
        }
    }


    public boolean avaliableTotalIssueQuantity(Integer totalQuantity, Long couponId) {
        if(totalQuantity == null) {
            return true; // 발급 수량 제한 x
        }
        String key = getIssueRequestKey(couponId);
        return totalQuantity > redisRepository.sCard(key);
    }

    public boolean avaliableUserIssueQuantity(Long couponId, Long userId) {
        String key = getIssueRequestKey(couponId);
        boolean result = redisRepository.sIsMember(key, String.valueOf(userId));
        System.out.println(result);
        return !result;
    }

}
