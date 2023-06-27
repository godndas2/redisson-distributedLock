package com.example.coupon;

import com.example.domain.coupon.application.CouponService;
import com.example.domain.coupon.dto.CouponRequest;
import com.example.domain.coupon.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponRegisterLockTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    /**
     * 1. "NEW_001" 이라는 name을 가진 쿠폰을 준비한다
     * 2. 사용자 100명이 동시에 "NEW_001" 쿠폰을 등록 요청한다
     * 3. 정상적으로 등록된 "NEW_001" 쿠폰 갯수는 단 하나이어야 한다
     */
    @DisplayName("반대로 테스트 하려면 register() @DistributedLock 주석")
    @Test
    void 쿠폰등록_중복체크() throws InterruptedException {
        CouponRequest couponRequest = new CouponRequest("NEW_001", 10L);

        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(30);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i=0; i<numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    couponService.registerCoupon(couponRequest);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Long totalCount = couponRepository.countByName("NEW_001");
        assertThat(totalCount).isOne();
        System.out.println("등록 된 쿠폰 갯수 = " + totalCount);
    }
}
