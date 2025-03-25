package com.traffic.couponapi.controller;


import com.traffic.couponapi.controller.dto.CouponIssueRequestDTO;
import com.traffic.couponapi.controller.dto.CouponIssueResponseDTO;
import com.traffic.couponapi.service.CouponIssueRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponIssueController {

    private final CouponIssueRequestService couponIssueRequestService;

    @PostMapping("/v1/issue")
    public CouponIssueResponseDTO issueV1(@RequestBody CouponIssueRequestDTO requestDTO) {
        couponIssueRequestService.issueRequest_V1(requestDTO);
        return new CouponIssueResponseDTO(true, null);
    }


    @PostMapping("/v1/asyncIssue")
    public CouponIssueResponseDTO asyncIssueV1(@RequestBody CouponIssueRequestDTO requestDTO) {
        couponIssueRequestService.asyncIssueRequest_V1(requestDTO);
        return new CouponIssueResponseDTO(true, null);
    }

    // redis 스크립트 버전
    @PostMapping("/v2/asyncIssue")
    public CouponIssueResponseDTO asyncIssueV2(@RequestBody CouponIssueRequestDTO requestDTO) {
        couponIssueRequestService.asyncIssueRequest_V2(requestDTO);
        return new CouponIssueResponseDTO(true, null);
    }

}
