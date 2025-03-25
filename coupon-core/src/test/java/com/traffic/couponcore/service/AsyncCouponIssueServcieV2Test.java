package com.traffic.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic.couponcore.TestConfig;
import com.traffic.couponcore.exception.CouponIssueException;
import com.traffic.couponcore.exception.ErrorCode;
import com.traffic.couponcore.model.Coupon;
import com.traffic.couponcore.model.CouponType;
import com.traffic.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.traffic.couponcore.repository.mysql.CouponJpaRepository;
import com.traffic.couponcore.repository.redis.dto.CouponIssueRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.IntStream;

import static com.traffic.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.traffic.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;
import static org.junit.jupiter.api.Assertions.*;

class AsyncCouponIssueServcieV2Test extends TestConfig {

    @Autowired
    AsyncCouponIssueServcieV1 sut;
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    CouponJpaRepository couponJpaRepository;
    @Autowired
    CouponIssueJpaRepository couponIssueJpaRepository;

    @BeforeEach
    void clear() {
        Collection<String> rediskeys = redisTemplate.keys("*");
        redisTemplate.delete(rediskeys);
    }

    // 쿠폰이 존재하지 않는다면 예외를 반환
    @Test
    @DisplayName("쿠폰 발급 - 쿠폰이 존재하지 않는다면 예외 반환")
    void issue_1() {
        // given
        Long couponId = 1L;
        Long userId = 1L;

        // when & then

        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            sut.issue(couponId, userId);
        });

        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.COUPON_NOT_EXIST);
    }

    // 쿠폰 발급 수량에 관한 검증
/*    @Test
    @DisplayName("쿠폰 발급 - 가능한 발급 수량이 없으면 예외 반환")
    void issue_2() {
        // given
        Long userId = 1L;
        Coupon coupon = Coupon.builder()
                .title("선착순 테스트 쿠폰")
                .totalQuantity(10)
                .couponType(CouponType.FIRST_)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .issuedQuantity(0)
                .build();
        couponJpaRepository.save(coupon);
        IntStream.range(0, coupon.getTotalQuantity()).forEach(idx -> {
            redisTemplate.opsForSet().add(getIssueRequestKey(coupon.getId()), String.valueOf(idx));
        });

        // when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            sut.issue(coupon.getId(), userId);
        });

        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);

    }*/


    // 쿠폰 발급 기한에 관한 검증
    @Test
    @DisplayName("쿠폰 발급 - 발급 기한이 아니면 예외를 반환")
    void issue_3() {
        // given
        Long userId = 1L;
        Coupon coupon = Coupon.builder()
                .title("선착순 테스트 쿠폰")
                .totalQuantity(10)
                .couponType(CouponType.FIRST_)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .issuedQuantity(0)
                .build();
        couponJpaRepository.save(coupon);

        // when&then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            sut.issue(coupon.getId(), userId);
        });

        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }

    // 쿠폰 중복 발급에 대한 검증
    @Test
    @DisplayName("쿠폰 발급 - 중복 발급이면 예외를 반환")
    void issue_4() {
        // given
        Long userId = 1L;
        Coupon coupon = Coupon.builder()
                .title("선착순 테스트 쿠폰")
                .totalQuantity(10)
                .couponType(CouponType.FIRST_)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .issuedQuantity(0)
                .build();
        couponJpaRepository.save(coupon);
        redisTemplate.opsForSet().add(getIssueRequestKey(coupon.getId()), String.valueOf(userId));

        // when&then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            sut.issue(coupon.getId(), userId);
        });

        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.DUPLICATED_COUPON_ISSUE);

    }

    // 쿠폰 발급이 됐을 때, set 에 잘 저장이 되었는지 확인
    @Test
    @DisplayName("쿠폰 발급 - 쿠폰 발급 기록이 되었는지 확인")
    void issue_5() {
        // given
        Long userId = 1L;
        Coupon coupon = Coupon.builder()
                .title("선착순 테스트 쿠폰")
                .totalQuantity(10)
                .couponType(CouponType.FIRST_)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .issuedQuantity(0)
                .build();
        couponJpaRepository.save(coupon);
        // when
        sut.issue(coupon.getId(), userId);
        // then
        boolean isSaved = redisTemplate.opsForSet().isMember(getIssueRequestKey(coupon.getId()),String.valueOf(userId));
        Assertions.assertTrue(isSaved);
    }
    @Test
    @DisplayName("쿠폰 발급 - 쿠폰 발급 요청이 성공하면, 쿠폰 발급 큐에 적재 되었는지 확인")
    void issue_6() throws JsonProcessingException {
        // given
        Long userId = 1L;
        Coupon coupon = Coupon.builder()
                .title("선착순 테스트 쿠폰")
                .totalQuantity(10)
                .couponType(CouponType.FIRST_)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .issuedQuantity(0)
                .build();
        couponJpaRepository.save(coupon);
        CouponIssueRequest requestIssue = new CouponIssueRequest(coupon.getId(), userId);
        // when
        sut.issue(coupon.getId(), userId);
        // then
        String isSavedIssueRequest = redisTemplate.opsForList().leftPop(getIssueRequestQueueKey());
        Assertions.assertEquals(new ObjectMapper().writeValueAsString(requestIssue), isSavedIssueRequest);
    }


}