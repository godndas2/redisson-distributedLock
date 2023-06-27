package com.example.coupon;

import com.example.domain.coupon.application.CouponDecreaseService;
import com.example.domain.coupon.domain.Coupon;
import com.example.domain.coupon.repository.CouponRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CouponDecreaseLockTest {

    @Autowired
    private CouponDecreaseService couponDecreaseService;

    @Autowired
    private CouponRepository couponRepository;

    private Coupon coupon;

    @BeforeEach
    void setup() {
        coupon = new Coupon("HIGH_COUPON_001", 100L);
        couponRepository.save(coupon);
    }

    @AfterEach
    void tearDown() {
        couponRepository.deleteAll();
    }

    /**
     * Feature: 쿠폰 차감 동시성 테스트
     * Background
     *     Given HIGH_001 라는 이름의 쿠폰 100장이 등록되어 있다
     * <p>
     * Scenario: 100장의 쿠폰을 100명의 사용자가 동시에 접근해 발급 요청
     *           Lock의 이름은 Coupon name으로 한다.
     * <p>
     * Then 사용자들의 요청만큼 정확히 쿠폰의 개수가 차감되어야 한다.
     */
    @Test
    void 쿠폰차감_분산락_적용_동시성100명_테스트() throws InterruptedException {
        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    // distributedLock method call (lock key : coupon name)
                    couponDecreaseService.couponDecrease(coupon.getName(), coupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Coupon persistCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow(IllegalArgumentException::new);

        assertThat(persistCoupon.getAvailableStock()).isZero();
        System.out.println("잔여 쿠폰 개수 = " + persistCoupon.getAvailableStock());
    }

    @Test
    void 쿠폰차감_분산락_미적용_동시성100명_테스트() throws InterruptedException {
        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    couponDecreaseService.couponDecrease(coupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Coupon persistCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow(IllegalArgumentException::new);

        assertThat(persistCoupon.getAvailableStock()).isZero();
        System.out.println("잔여 쿠폰 갯수 = " + persistCoupon.getAvailableStock());
    }
}
