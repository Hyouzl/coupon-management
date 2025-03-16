package com.traffic.couponcore.service;

import com.traffic.couponcore.exception.CouponIssueException;
import com.traffic.couponcore.exception.ErrorCode;
import com.traffic.couponcore.model.Coupon;
import com.traffic.couponcore.model.CouponIssue;
import com.traffic.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.traffic.couponcore.repository.mysql.CouponIssueRepository;
import com.traffic.couponcore.repository.mysql.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;
    private final CouponIssueRepository couponIssueRepository;


    @Transactional
    public void issue(Long couponId, Long userId) {

        Coupon coupon = findCoupon(couponId);
        coupon.isssue(); // 전체 수량에 대한 검증과, 발급 기간에 대한 검증 후 이슈 발급 수 증가.
        saveCouponIssue(couponId, userId);

    }

    @Transactional(readOnly = true)
    public Coupon findCoupon(Long couponId) {
        return couponJpaRepository.findById(couponId).orElseThrow(()-> {
            throw new CouponIssueException(ErrorCode.COUPON_NOT_EXIST, "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId));
        });
    }

    @Transactional
    public CouponIssue saveCouponIssue(Long couponId, Long userId) {
        checkAlreadyIssuance(couponId, userId);
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();
        return couponIssueJpaRepository.save(couponIssue);
    }

    private void checkAlreadyIssuance(Long couponId, Long userId) {
        if (couponIssueRepository.findFirstCouponIssue(couponId, userId) != null) {
            throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다. user_id: %s, coupon_id: %s ".formatted(userId,couponId));
        }
    }

}
