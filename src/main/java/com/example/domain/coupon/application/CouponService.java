package com.example.domain.coupon.application;

import com.example.domain.coupon.dto.CouponRequest;
import com.example.domain.coupon.dto.CouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.domain.coupon.application.CouponRegisterService.COUPON_KEY_PREFIX;


@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRegisterService couponRegisterService;

    public CouponResponse registerCoupon(CouponRequest couponRequest) {
        String key = COUPON_KEY_PREFIX + couponRequest.getName();
        return couponRegisterService.register(key, couponRequest);
    }
}
