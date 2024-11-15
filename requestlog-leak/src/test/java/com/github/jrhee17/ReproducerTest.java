package com.github.jrhee17;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.client.grpc.GrpcClients;

import testing.grpc.Hello.HelloRequest;
import testing.grpc.TestServiceGrpc.TestServiceFutureStub;

class ReproducerTest {

    private static final Logger logger = LoggerFactory.getLogger(ReproducerTest.class);

    @Test
    void trySendRequests() {
        final AtomicInteger successCount = new AtomicInteger();
        final AtomicInteger failCount = new AtomicInteger();
        final int requestCount = 50_000;
        final TestServiceFutureStub stub = GrpcClients.newClient("http://localhost:8080",
                                                                 TestServiceFutureStub.class);
        for (int i = 0; i < requestCount; i++) {
            if (i % 1_000 == 0) {
                logger.info("Sending requests: {}/{}", i, requestCount);
            }
            stub.hello(HelloRequest.getDefaultInstance()).addListener(() -> {
                successCount.incrementAndGet();
            }, ForkJoinPool.commonPool());
        }
        await().atMost(Duration.ofSeconds(60)).untilAsserted(() -> assertThat(successCount.get() + failCount.get())
                .isEqualTo(requestCount));
    }
}
