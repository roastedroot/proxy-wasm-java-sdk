package io.roastedroot.proxywasm.jaxrs.vertx;

import io.roastedroot.proxywasm.jaxrs.spi.HttpServer;
import io.vertx.core.Vertx;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

@Alternative
@Priority(200)
@ApplicationScoped
public class VertxHttpServer implements HttpServer {

    @Inject Vertx vertx;

    @Override
    public Runnable scheduleTick(long delay, Runnable task) {
        var id = vertx.setPeriodic(delay, x -> task.run());
        return () -> {
            vertx.cancelTimer(id);
        };
    }
}
