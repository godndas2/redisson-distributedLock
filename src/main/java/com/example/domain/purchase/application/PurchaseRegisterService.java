package com.example.domain.purchase.application;

import com.example.global.common.DistributedLock;
import com.example.domain.purchase.domain.Purchase;
import com.example.domain.purchase.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseRegisterService {
    private final PurchaseRepository purchaseRepository;

    @DistributedLock(key = "#lockName")
    public void register(String lockName, String code) {
        boolean existsPurchase = purchaseRepository.existsByCode(code);
        if (existsPurchase) {
            throw new IllegalArgumentException();
        }

        Purchase purchase = new Purchase(code);
        purchaseRepository.save(purchase);
    }

    @Transactional
    public void register(String code) {
        boolean existsPurchase = purchaseRepository.existsByCode(code);
        if (existsPurchase) {
            throw new IllegalArgumentException();
        }

        Purchase purchase = new Purchase(code);
        purchaseRepository.save(purchase);
    }
}
