package com.github.jrhee17;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.client.ClientRequestContext;
import com.linecorp.armeria.client.ClientRequestContextCaptor;
import com.linecorp.armeria.client.Clients;
import com.linecorp.armeria.client.retry.Backoff;
import com.linecorp.armeria.client.retry.RetryRule;
import com.linecorp.armeria.client.retry.RetryingClient;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.logging.RequestLog;
import com.linecorp.armeria.common.stream.ClosedStreamException;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;

class ClosedStreamTest {

    private static final Logger logger = LoggerFactory.getLogger(ClosedStreamTest.class);

    @RegisterExtension
    static ServerExtension server = new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
            sb.service("/", (ctx, req) -> HttpResponse.streaming());
            sb.decorator(LoggingService.newDecorator());
        }
    };

    @Test
    void testAsdf() throws Exception {
        final RetryRule retryImmediatelyOnGoAway = RetryRule.onUnprocessed(Backoff.withoutDelay());
        final RetryRule retryImmediatelyOnClosedStream =
                RetryRule.onException(ClosedStreamException.class, Backoff.withoutDelay());

        ClientRequestContext clientCtx;
        try (ClientRequestContextCaptor captor = Clients.newContextCaptor()) {
            final HttpResponse response = server.webClient(cb -> cb.decorator(RetryingClient.newDecorator(retryImmediatelyOnGoAway.orElse(retryImmediatelyOnClosedStream))))
                                                .get("/");
            clientCtx = captor.get();
        }
        await().untilAsserted(() -> assertThat(server.requestContextCaptor().size()).isGreaterThan(0));
        final ServiceRequestContext serverCtx = server.requestContextCaptor().poll();
        assert serverCtx != null;
        serverCtx.cancel();

        final RequestLog log = clientCtx.log().whenComplete().join();
        logger.info("log.responseCause(): ", log.responseCause());
        // retries
        logger.info("log.children(): {}", log.children());
    }
}
