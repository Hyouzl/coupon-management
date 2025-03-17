package com.traffic.couponcore.service;

import com.traffic.couponcore.TestConfig;
import com.traffic.couponcore.exception.CouponIssueException;
import com.traffic.couponcore.exception.ErrorCode;
import com.traffic.couponcore.model.Coupon;
import com.traffic.couponcore.model.CouponIssue;
import com.traffic.couponcore.model.CouponType;
import com.traffic.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.traffic.couponcore.repository.mysql.CouponIssueRepository;
import com.traffic.couponcore.repository.mysql.CouponJpaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

class CouponIssueServiceTest extends TestConfig {

    @Autowired
    CouponIssueService sut;

    @Autowired
    CouponIssueJpaRepository couponIssueJpaRepository;

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @Autowired
    CouponIssueRepository couponIssueRepository;

    // 테스트 실행 전 데이터 정리.
    @BeforeEach
    void clean() {
        couponJpaRepository.deleteAllInBatch();
        couponIssueJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("쿠폰 발급 내역이 존재하면 예외 반환")
    void saveCouponIssue_test1() {
        //given
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(1L)
                .userId(1L)
                .build();

        couponIssueJpaRepository.save(couponIssue);

        //when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            sut.saveCouponIssue(couponIssue.getCouponId(), couponIssue.getUserId());
        });

        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.DUPLICATED_COUPON_ISSUE);

    }

    @Test
    @DisplayName("쿠폰 발급 내역이 존재하지 않는다면 쿠폰 발급")
    void saveCouponIssue_test2() {
        //given
        Long couponId = 1L;
        Long userId = 1L;

        //when
        CouponIssue result = sut.saveCouponIssue(couponId, userId);

        //then
        Assertions.assertTrue(couponIssueJpaRepository.findById(result.getId()).isPresent());

    }

    @Test
    @DisplayName("발급 수량, 기한, 중복 발급 문제가 없다면 쿠폰을 발급")
    void issue_1() {
        // given
        Long userId = 1L;
        Coupon coupon = Coupon.builder()
                .title("쿠폰 발급 테스트")
                .issuedQuantity(0)
                .totalQuantity(100)
                .couponType(CouponType.FIRST_)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);
        // when
        sut.issue(coupon.getId(), userId);
        // then
        Coupon couponResult = couponJpaRepository.findById(coupon.getId()).get();
        Assertions.assertEquals(couponResult.getIssuedQuantity(), 1);

        CouponIssue couponIssueResult = couponIssueRepository.findFirstCouponIssue(coupon.getId(), userId);
        Assertions.assertNotNull(couponIssueResult);
    }

    @Test
    @DisplayName("발급 수량에 문제가 있다면 오류 반환")
    void issue_2() {
        //given
        Long userId = 1L;
        Coupon coupon = Coupon.builder()
                .title("쿠폰 발급 테스트")
                .issuedQuantity(100)
                .totalQuantity(100)
                .couponType(CouponType.FIRST_)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);
        // when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            sut.issue(coupon.getId(), userId);
        });

        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("기한에 문제가 있다면 오류 반환")
    void issue_3() {
        //given
        Long userId = 1L;
        Coupon coupon = Coupon.builder()
                .title("쿠폰 발급 테스트")
                .issuedQuantity(99)
                .totalQuantity(100)
                .couponType(CouponType.FIRST_)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();
        couponJpaRepository.save(coupon);
        // when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            sut.issue(coupon.getId(), userId);
        });

        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }

    @Test
    @DisplayName("중복 발급 문제가 있다면 오류 반환")
    void issue_4() {
        //given
        Long userId = 1L;
        Coupon coupon = Coupon.builder()
                .title("쿠폰 발급 테스트")
                .issuedQuantity(99)
                .totalQuantity(100)
                .couponType(CouponType.FIRST_)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(coupon.getId())
                .userId(userId)
                .build();
        couponIssueJpaRepository.save(couponIssue);
        // when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            sut.issue(coupon.getId(), userId);
        });

        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.DUPLICATED_COUPON_ISSUE);

    }

    @Test
    @DisplayName("쿠폰이 존재하지 않으면 오류 반환")
    void issue_5() {
        //given
        Long userId = 1L;
        Long couponId = 1L;

        // when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            sut.issue(couponId, userId);
        });

        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.COUPON_NOT_EXIST);

    }








}