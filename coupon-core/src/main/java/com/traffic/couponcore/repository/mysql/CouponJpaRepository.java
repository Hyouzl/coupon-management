package com.traffic.couponcore.repository.mysql;

import com.traffic.couponcore.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {


}
