package com.traffic.couponcore.model;

import com.traffic.couponcore.exception.CouponIssueException;
import com.traffic.couponcore.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "coupons")
public class Coupon extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CouponType couponType;

    private Integer totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity; // 발급된 수량

    @Column(nullable = false)
    private int  discountAmount;

    @Column(nullable = false)
    private int minAvaliableAmount;

    @Column(nullable = false)
    private LocalDateTime dateIssueStart; // 발급 시작 일시

    @Column(nullable = false)
    private LocalDateTime dateIssueEnd; // 발급 종료 일시


    public boolean isIssueComplete() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueEnd.isBefore(now) || !avaliableIssueQuantity();

    }


    // 발급 가능 수량 검증
    public boolean avaliableIssueQuantity() {

        if (totalQuantity == null) { // 전체 수량 제한 없음.
            return true;
        }
        return totalQuantity > issuedQuantity;
    }

    // 발급 기한 검증
    public boolean avaliableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }



    public void isssue() {
        if (!avaliableIssueQuantity()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다. total : %s, issued : %s".formatted(totalQuantity, issuedQuantity));
        }
        if (!avaliableIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE, "발급 가능한 기한이 아닙니다. now : %s, issueStart : %s, issueEnd : %s".formatted(LocalDateTime.now(), dateIssueStart, dateIssueEnd));
        }
        issuedQuantity ++;
    }

}
