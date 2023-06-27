package com.example.domain.coupon.application;

import com.example.domain.coupon.domain.Coupon;
import com.example.domain.coupon.dto.CouponRequest;
import com.example.domain.coupon.dto.CouponResponse;
import com.example.domain.coupon.repository.CouponRepository;
import com.example.global.common.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponRegisterService {

    public static final String COUPON_KEY_PREFIX = "HIGH_COUPON_";
    private final CouponRepository couponRepository;


    @DistributedLock(key = "#key")
    public CouponResponse register(final String key, CouponRequest request) {
        validateAlreadyExist(request);

        Coupon coupon = request.toCoupon();
        couponRepository.save(coupon);
        return CouponResponse.toResponse(coupon);
    }

    private void validateAlreadyExist(CouponRequest request) {
        couponRepository.findByName(request.getName()).ifPresent(x -> {
            throw new IllegalArgumentException();
        });
    }


}
