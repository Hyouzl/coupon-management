package com.traffic.couponconsumer.component;

import com.traffic.couponconsumer.TestConfig;
import com.traffic.couponcore.repository.redis.RedisRepository;
import com.traffic.couponcore.service.CouponIssueService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collection;

class CouponIssueListenerTest extends TestConfig {

    @Autowired
    CouponIssueListener sut;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    RedisRepository redisRepository;

    @MockitoBean
    CouponIssueService couponIssueService;

    @BeforeEach
    void clear() {
        Collection<String> rediskeys = redisTemplate.keys("*");
        redisTemplate.delete(rediskeys);
    }




}