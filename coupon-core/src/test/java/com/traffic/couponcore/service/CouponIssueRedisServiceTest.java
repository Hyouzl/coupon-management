package com.traffic.couponcore.service;

import com.traffic.couponcore.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.traffic.couponcore.util.CouponRedisUtils.getIssueRequestKey;

class CouponIssueRedisServiceTest extends TestConfig {

    @Autowired
    CouponIssueRedisService sut;

    @Autowired
    RedisTemplate redisTemplate;

    @BeforeEach
    void clear() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);
    }

    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 존재하면 true를 반환한다.")
    void avaliableTotalIssueQuantity_1() {
        // given
        int totalIssueQuantity = 10;
        Long couponId = 1L;
        // when
        boolean result = sut.avaliableTotalIssueQuantity(totalIssueQuantity, couponId);
        // then
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 존재하지 않으면 false를 반환한다.")
    void avaliableTotalIssueQuantity_2() {
        // given
        int totalIssueQuantity = 10;
        Long couponId = 1L;
        Set<String> userIds = IntStream.range(0, totalIssueQuantity)
                .mapToObj(String::valueOf)
                .collect(Collectors.toSet());
        String issueKey = getIssueRequestKey(couponId);
        redisTemplate.opsForSet().add(issueKey, userIds.toArray());
        // when
        boolean result = sut.avaliableTotalIssueQuantity(totalIssueQuantity, couponId);
        // then
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("쿠폰 중복 발급 검증 - 발급된 내역에 유저가 존재하지 않으면 true 를 반환한다.")
    void avaliableUserIssueQuantity_1() {
        //given
        Long couponId = 1L;
        Long userId = 1L;
        //when
        boolean result = sut.avaliableUserIssueQuantity(couponId, userId);
        System.out.println(result);
        //then
        Assertions.assertTrue(result);

    }

    @Test
    @DisplayName("쿠폰 중복 발급 검증 - 발급된 내역에 유저가 존재하면 false 를 반환한다.")
    void avaliableUserIssueQuantity_2() {
        //given
        Long couponId = 1L;
        Long userId = 1L;
        redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(userId));
        //when
        boolean result = sut.avaliableUserIssueQuantity(couponId, userId);
        System.out.println(result);
        //then
        Assertions.assertFalse(result);

    }
}