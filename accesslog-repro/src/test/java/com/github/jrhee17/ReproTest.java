package com.github.jrhee17;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.common.AggregatedHttpResponse;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;

class ReproTest {

    private static final Logger logger = LoggerFactory.getLogger(ReproTest.class);

    @RegisterExtension
    static ServerExtension server = new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
            sb.accessLogWriter(new CustomAccessLogWriter(), false);
            sb.service("/", (ctx, req) -> {
                return HttpResponse.of(Integer.valueOf(req.headers().get("status-code")));
            });
        }
    };

    @ParameterizedTest
    @ValueSource(ints = { 200, 400, 500})
    void testAsdf(Integer statusCode) throws Exception {
        AggregatedHttpResponse res = server.blockingWebClient().prepare()
                .header("status-code", statusCode.toString())
                .get("/").execute();
        logger.info("res: {}", res);
    }
}
