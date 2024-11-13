package com.github.jrhee17;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.SessionProtocol;
import com.linecorp.armeria.common.util.ThreadFactories;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.GrpcService;

import io.grpc.stub.StreamObserver;
import io.netty.util.AttributeKey;
import testing.grpc.Hello.HelloReply;
import testing.grpc.Hello.HelloRequest;
import testing.grpc.TestServiceGrpc.TestServiceImplBase;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final AtomicInteger whenCompleteCount = new AtomicInteger();

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
            ThreadFactories.newThreadFactory("gc-trigger", true));

    private static final AttributeKey<Tracker> TRACKER_KEY = AttributeKey.valueOf(Main.class, "tracker");

    private static class Tracker {

        private final ServiceRequestContext ctx;

        Tracker(ServiceRequestContext ctx) {
            this.ctx = ctx;
        }

        @Override
        protected void finalize() throws Throwable {
            whenCompleteCount.incrementAndGet();
            if (whenCompleteCount.get() % 10_000 == 0) {
                logger.info("whenCompleteCount: {}", whenCompleteCount.get());
            }
            if (!ctx.log().isComplete()) {
                logger.info("not completed ctx: {}", ctx);
            }
            if (!ctx.log().whenComplete().isDone()) {
                logger.info("not completed ctx: {}", ctx);
            }

            super.finalize();
        }
    }

    public static void main(String[] args) throws Exception {
        executor.scheduleAtFixedRate(() -> {
            logger.info("Triggering gc...");
            System.gc();
        }, 1, 1, TimeUnit.SECONDS);

        final Server server = Server.builder()
                                    .service("/", (ctx, req) -> {
                                        ctx.setAttr(TRACKER_KEY, new Tracker(ctx));
                                        return HttpResponse.of(200);
                                    })
                                    .service(GrpcService.builder()
                                                        .addService(new TestServiceImplBase() {
                                                            @Override
                                                            public void hello(HelloRequest request,
                                                                              StreamObserver<HelloReply> responseObserver) {
                                                                ServiceRequestContext ctx =
                                                                        ServiceRequestContext.current();
                                                                ctx.setAttr(TRACKER_KEY, new Tracker(ctx));
                                                                responseObserver.onNext(HelloReply.newBuilder()
                                                                                                  .setMessage("Hello").build());
                                                                responseObserver.onCompleted();
                                                            }
                                                        })
                                                        .build())
                                    .port(8080, SessionProtocol.HTTP)
                                    .build();
        server.closeOnJvmShutdown();
        server.start().join();
    }
}