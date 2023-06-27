package com.example.purchase;

import com.example.domain.purchase.application.PurchaseRegisterService;
import com.example.domain.purchase.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PurchaseRegisterLockTest {
    @Autowired
    private PurchaseRegisterService purchaseRegisterService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    /**
     * Feature: 발주 등록 동시성 테스트
     * <p>
     * Scenario: HIGH_COUPON_001 라는 이름의 발주 10개가 동시에 등록 요청된다.
     * <p>
     * Then 중복된 발주 10개가 동시에 들어오더라도 한 건만 정상 등록 되어야 한다.
     */
    @Test
    void 발주등록_분산락_적용_테스트() throws InterruptedException {
        String purchaseCode = "HIGH_COUPON_001";

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    // distributedLock method call
                    purchaseRegisterService.register(purchaseCode, purchaseCode);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Long totalCount = purchaseRepository.countByCode(purchaseCode);

        System.out.println("등록된 발주 = " + totalCount);
        assertThat(totalCount).isOne();
    }

    @Test
    void 발주등록_분산락_미적용_테스트() throws InterruptedException {
        String purchaseCode = "HIGH_COUPON_001";

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    purchaseRegisterService.register(purchaseCode);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Long totalCount = purchaseRepository.countByCode(purchaseCode);

        System.out.println("등록된 발주 = " + totalCount);
        assertThat(totalCount).isOne();
    }
}