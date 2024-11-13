package com.github.jrhee17;

import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.micrometer.observation.ObservationTextPublisher;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class Main {

    @Bean
    ObservationTextPublisher otp() {
        return new ObservationTextPublisher();
    }

    @Component
    static class RequestTimeoutWebFilter implements WebFilter {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            return chain
                    .filter(exchange)
                    .timeout(Duration.ofMillis(50));
        }
    }

//    @Bean
//    public ArmeriaServerConfigurator armeriaServerConfigurator() {
//        // Customize the server using the given ServerBuilder. For example:
//        return builder -> {
//            // Add DocService that enables you to send Thrift and gRPC requests from web browser.
//            builder.serviceUnder("/docs", new DocService());
//
//            // Log every message which the server receives and responds.
//            builder.decorator(LoggingService.newDecorator());
//
//            // Write access log after completing a request.
//            builder.accessLogWriter(AccessLogWriter.combined(), false);
//
//            // You can also bind annotated HTTP services and asynchronous RPC services such as Thrift and gRPC:
////            builder.service("/hello", (ctx, req) -> HttpResponse.of("world"));
//        };
//    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}