package io.quarkiverse.proxywasm.runtime;

import io.roastedroot.proxywasm.ProxyMap;
import io.roastedroot.proxywasm.plugin.HttpCallResponse;
import io.roastedroot.proxywasm.plugin.HttpCallResponseHandler;
import io.roastedroot.proxywasm.plugin.HttpRequestAdaptor;
import io.roastedroot.proxywasm.plugin.ServerAdaptor;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.net.URI;

@Alternative
@Priority(200)
@ApplicationScoped
public class VertxServerAdaptor implements ServerAdaptor {

    @Inject Vertx vertx;

    HttpClient client;

    @PostConstruct
    public void setup() {
        this.client = vertx.createHttpClient();
    }

    @PreDestroy
    public void close() {
        client.close();
    }

    @Override
    public Runnable scheduleTick(long delay, Runnable task) {
        var id = vertx.setPeriodic(delay, x -> task.run());
        return () -> {
            vertx.cancelTimer(id);
        };
    }

    @Inject Instance<VertxHttpRequestAdaptor> httpRequestAdaptors;

    @Override
    public HttpRequestAdaptor httpRequestAdaptor(Object context) {
        return httpRequestAdaptors.get();
    }

    @Override
    public Runnable scheduleHttpCall(
            String method,
            String host,
            int port,
            URI uri,
            ProxyMap headers,
            byte[] body,
            ProxyMap trailers,
            int timeout,
            HttpCallResponseHandler handler)
            throws InterruptedException {
        var f =
                client.request(HttpMethod.valueOf(method), port, host, uri.toString())
                        .compose(
                                req -> {
                                    for (var e : headers.entries()) {
                                        req.headers().add(e.getKey(), e.getValue());
                                    }
                                    req.idleTimeout(timeout);
                                    return req.send(Buffer.buffer(body));
                                })
                        .onComplete(
                                resp -> {
                                    if (resp.succeeded()) {
                                        HttpClientResponse result = resp.result();
                                        result.bodyHandler(
                                                bodyHandler -> {
                                                    var h = ProxyMap.of();
                                                    result.headers()
                                                            .forEach(
                                                                    e ->
                                                                            h.add(
                                                                                    e.getKey(),
                                                                                    e.getValue()));
                                                    handler.call(
                                                            new HttpCallResponse(
                                                                    result.statusCode(),
                                                                    h,
                                                                    bodyHandler.getBytes()));
                                                });
                                    } else {
                                        handler.call(
                                                new HttpCallResponse(
                                                        500,
                                                        ProxyMap.of(),
                                                        resp.cause().getMessage().getBytes()));
                                    }
                                });

        return () -> {
            // There doesn't seem to be a way to cancel the request.
        };
    }
}
