package com.example.domain.coupon.dto;

import com.example.domain.coupon.domain.Coupon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CouponResponse {
    private Long id;
    private String name;
    private Long availableStock;

    public static CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getAvailableStock()
        );
    }
}
