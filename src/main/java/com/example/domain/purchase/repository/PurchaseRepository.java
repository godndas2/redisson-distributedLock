package com.example.domain.purchase.repository;

import com.example.domain.purchase.domain.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    boolean existsByCode(String code);
    Long countByCode(String code);

}
