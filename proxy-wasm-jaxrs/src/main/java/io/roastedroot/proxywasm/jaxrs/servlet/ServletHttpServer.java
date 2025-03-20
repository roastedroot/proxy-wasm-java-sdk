package io.roastedroot.proxywasm.jaxrs.servlet;

import io.roastedroot.proxywasm.jaxrs.spi.HttpServer;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Alternative
@Priority(100)
@ApplicationScoped
public class ServletHttpServer implements HttpServer {

    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Override
    public Runnable scheduleTick(long delay, Runnable task) {
        var f = executorService.scheduleAtFixedRate(task, delay, delay, TimeUnit.MILLISECONDS);
        return () -> {
            ;
            f.cancel(false);
        };
    }
}
