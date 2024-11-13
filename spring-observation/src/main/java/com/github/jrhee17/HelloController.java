package com.github.jrhee17;

import java.time.Duration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import io.micrometer.observation.ObservationRegistry;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Mono;

/**
 * An example of a controller which uses {@link WebClient} inside.
 */
@RestController
public class HelloController {

    private final ObservationRegistry registry;

    HelloController(ObservationRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/hello")
    Mono<String> hello() {
        return Mono
                .just("world")
                .delayElement(Duration.ofMillis(1000));	//3
    }
}
