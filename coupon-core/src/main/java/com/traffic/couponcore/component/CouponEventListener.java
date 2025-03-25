package com.traffic.couponcore.component;

import com.traffic.couponcore.model.event.CouponIssueCompleteEvent;
import com.traffic.couponcore.service.CouponCacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class CouponEventListener {

    private final CouponCacheService couponCacheService;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void issueComplete(CouponIssueCompleteEvent completeEvent) {
        log.info("issue complete. cache refresh start couponId: %s".formatted(completeEvent.couponId()));
        couponCacheService.putCouponCache(completeEvent.couponId());
        couponCacheService.putCouponLocalCache(completeEvent.couponId());
        log.info("issue complete. cache refresh end couponId: %s".formatted(completeEvent.couponId()));
    }

}
