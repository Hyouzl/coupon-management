package com.traffic.couponapi.service;

import com.traffic.couponapi.controller.dto.CouponIssueRequestDTO;
import com.traffic.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponIssueRequestService {

    private final CouponIssueService couponIssueService;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public void issueRequest_V1(CouponIssueRequestDTO couponIssueRequestDTO) {
        couponIssueService.issue(couponIssueRequestDTO.couponId(), couponIssueRequestDTO.userId());
        log.info("쿠폰 발급 완료. couponId: %s, userId: %s ".formatted(couponIssueRequestDTO.couponId(), couponIssueRequestDTO.userId()));
    }
}
