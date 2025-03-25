package com.traffic.couponcore.model;

import com.traffic.couponcore.exception.CouponIssueException;
import com.traffic.couponcore.exception.ErrorCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CouponTest {
    
    @Test
    @DisplayName("발급 수량이 남아있다면 true를 반환한다.")
    void availableIssueQuantity_1() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .build();
        
        //when
        boolean result = coupon.avaliableIssueQuantity();

        //then
        Assertions.assertTrue(result);

    }
    @Test
    @DisplayName("발급 수량이 남아있다면 false를 반환한다.")
    void availableIssueQuantity_2() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .build();

        //when
        boolean result = coupon.avaliableIssueQuantity();

        //then
        Assertions.assertFalse(result);

    }

    @Test
    @DisplayName("최대 발급 수량이 설정되지 않았다면 true 를 반환한다.")
    void availableIssueQuantity_3() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(null)
                .issuedQuantity(99)
                .build();

        //when
        boolean result = coupon.avaliableIssueQuantity();

        //then
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("발급 기간에 해당되지 않으면 false를 반환")
    void availableIssueDate_1() {
        //given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        //when
        boolean result = coupon.avaliableIssueDate();

        //then
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("발급 기간에 해당되면 true 를 반환")
    void availableIssueDate_2() {
        //given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        //when
        boolean result = coupon.avaliableIssueDate();

        //then
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("발급 기간이 종료되면 false 를 반환")
    void availableIssueDate_3() {
        //given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();

        //when
        boolean result = coupon.avaliableIssueDate();

        //then
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("발급 수량과 발급 기간이 유효하다면, true 를 반환")
    void issue_1() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        //when
        coupon.isssue();

        //then
        Assertions.assertEquals(coupon.getIssuedQuantity(),100);
    }
    @Test
    @DisplayName("발급 수량을 초과하면, 예외를 반환.")
    void issue_2() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        //when & then
      CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, coupon::isssue);
      Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("발급 기간이 아니면, 예외를 반환.")
    void issue_3() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        //when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, coupon::isssue);
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }


    @Test
    @DisplayName("발급 기한이 종료되면 true를 반환")
    void issue_4() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();
        //when & then
        boolean result = coupon.isIssueComplete();
        Assertions.assertTrue(result);

    }

    @Test
    @DisplayName("발급 수량이 없다면 true를 반환")
    void issue_5() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        //when & then
        boolean result = coupon.isIssueComplete();
        Assertions.assertTrue(result);

    }

    @Test
    @DisplayName("발급 기한과 수량이 유효하면, false를 반환")
    void issue_6() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        //when & then
        boolean result = coupon.isIssueComplete();
        Assertions.assertFalse(result);
    }

}