package com.example.stock;

import com.example.domain.stock.application.StockService;
import com.example.domain.stock.domain.Stock;
import com.example.domain.stock.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
class StockRegisterLockTest {

    @Autowired
    StockService stockService;

    @Autowired
    StockRepository stockRepository;

    @Test
    void stock_preoccupy_test_1() throws InterruptedException {
        Stock savedStock = registerStock();
        int numberOfThreads = 3;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        Future<?> future = executorService.submit(
                () -> stockService.decrease(savedStock.getId(), 1L));
        Future<?> future2 = executorService.submit(
                () -> stockService.decrease(savedStock.getId(), 1L));
        Future<?> future3 = executorService.submit(
                () -> stockService.decrease(savedStock.getId(), 1L));

        Exception result = new Exception();

        try {
            future.get();
            Thread.sleep(5000);

            future2.get();
            future3.get();
        } catch (ExecutionException e) {
            result = (Exception) e.getCause();
        }

        assertTrue(result instanceof OptimisticLockingFailureException);
    }

    @Test
    void stock_preoccupy_test_2() throws InterruptedException {
        Stock savedStock = registerStock();
        int numberOfThreads = 3;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        List<Future<?>> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            Future<?> future = executorService.submit(
                    () -> stockService.decrease(savedStock.getId(), 1L));
            tasks.add(future);
        }

        List<Exception> exceptions = new ArrayList<>();
        for (Future<?> task : tasks) {
            try {
                task.get();
            } catch (ExecutionException e) {
                exceptions.add((Exception) e.getCause());
            }
        }

        assertThat(exceptions).hasSize(numberOfThreads - 1);
        for (Exception e : exceptions) {
            assertTrue(e instanceof OptimisticLockingFailureException);
        }
    }

    @DisplayName("재고 1개 생성")
    Stock registerStock() {
        Stock stock = Stock.createStock("STOCK_1", 1L);
        stockRepository.save(stock);
        System.out.println("Stock Info = " + stock);
        return stock;
    }
}
