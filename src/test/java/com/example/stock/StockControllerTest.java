package com.example.stock;

import com.example.domain.stock.dto.StockRequest;
import com.example.domain.stock.dto.StockResponse;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StockControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    void concurrency_stock_decrease() throws InterruptedException, ExecutionException {
        StockResponse stock = stock_register(1L).as(StockResponse.class);

        int numberOfThreads = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        int failedCount = 0;
        List<Future<?>> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            Future<?> task = executorService.submit(() ->
                    stock_decrease(stock.getId(), 1L));

            tasks.add(task);
        }

        for (Future<?> task : tasks) {
            ExtractableResponse<Response> response = (ExtractableResponse<Response>) task.get();
            System.out.println("result = " + response.statusCode());
            System.out.println("result = " + response.asString());
            if (response.statusCode() == 500 &&
                    "이미 재고가 차감되었습니다.".equals(response.asString())) {
                failedCount++;
            }
        }

        assertThat(failedCount).isEqualTo(3);
    }

    @DisplayName("재고 생성")
    ExtractableResponse<Response> stock_register(Long stockCount) {
        StockRequest stockRequest = new StockRequest("STOCK_1", stockCount);

        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(stockRequest)
                .when().post("/stocks")
                .then().log().all()
                .extract();
    }

    @DisplayName("재고 차감")
    ExtractableResponse<Response> stock_decrease(Long stockId, Long pickingCount) {
        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("pickingCount", pickingCount)
                .when().put("/stocks/{stockId}/decrease", stockId)
                .then().log().all()
                .extract();
    }
}
