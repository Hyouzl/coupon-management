package com.traffic.couponcore.repository.mysql;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.traffic.couponcore.model.CouponIssue;
import com.traffic.couponcore.model.QCouponIssue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CouponIssueRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QCouponIssue qCouponIssue = QCouponIssue.couponIssue;

    public CouponIssue findFirstCouponIssue(Long couponId, Long userId) {

        return jpaQueryFactory.selectFrom(qCouponIssue)
                .where(qCouponIssue.couponId.eq(couponId))
                .where(qCouponIssue.userId.eq(userId))
                .fetchFirst();

    }
}
