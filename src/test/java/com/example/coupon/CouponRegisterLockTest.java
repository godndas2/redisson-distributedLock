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
     * Feature: 쿠폰 등록 동시성 테스트
     * Background
     *     Given HIGH_COUPON_NEW_001 라는 이름의 쿠폰을 준비한다.
     * <p>
     * Scenario: 100명의 사용자가 동시에 접근해 쿠폰(HIGH_COUPON_NEW_001) 등록 요청
     *           Lock의 이름은 HIGH_COUPON_NEW_001 으로 한다.
     * <p>
     * Then 정상적으로 등록된 "NEW_001" 쿠폰 갯수는 단 하나이어야 한다
     */
    @DisplayName("반대로 테스트 하려면 CouponRegisterService.register() @DistributedLock 주석")
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
