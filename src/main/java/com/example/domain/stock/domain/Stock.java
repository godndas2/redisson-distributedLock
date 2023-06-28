package com.example.domain.stock.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long availableStock;

    @Version
    private Long version;

    public Stock(String name, Long availableStock) {
        this.name = name;
        this.availableStock = availableStock;
    }

    public static Stock createStock(String name, Long availableStock) {
        return new Stock(name, availableStock);
    }

    public void decrease(Long pickingCount) {
        validateStockCount(pickingCount);
        availableStock -= pickingCount;
    }

    private void validateStockCount(Long pickingCount) {
        if (pickingCount > availableStock) {
            throw new IllegalArgumentException();
        }
    }
}
