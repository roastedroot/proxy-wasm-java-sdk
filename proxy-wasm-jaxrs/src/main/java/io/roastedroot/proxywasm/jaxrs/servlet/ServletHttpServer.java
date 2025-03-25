package io.roastedroot.proxywasm.jaxrs.servlet;

import io.roastedroot.proxywasm.ArrayProxyMap;
import io.roastedroot.proxywasm.ProxyMap;
import io.roastedroot.proxywasm.jaxrs.spi.HttpServer;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Alternative
@Priority(100)
@ApplicationScoped
public class ServletHttpServer implements HttpServer {

    ScheduledExecutorService tickExecutorService = Executors.newScheduledThreadPool(1);
    ExecutorService executorService = Executors.newWorkStealingPool(5);
    HttpClient client = HttpClient.newHttpClient();

    @Override
    public Runnable scheduleTick(long delay, Runnable task) {
        var f = tickExecutorService.scheduleAtFixedRate(task, delay, delay, TimeUnit.MILLISECONDS);
        return () -> {
            ;
            f.cancel(false);
        };
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
            HandlerHttpResponseHandler handler)
            throws InterruptedException {

        var f =
                executorService
                        .invokeAll(
                                List.of(
                                        () -> {
                                            var resp =
                                                    httpCall(
                                                            method, host, port, uri, headers, body);
                                            handler.call(resp);
                                            return null;
                                        }),
                                timeout,
                                TimeUnit.MILLISECONDS)
                        .get(0);
        return () -> {
            f.cancel(true);
        };
    }

    private HandlerHttpResponse httpCall(
            String method, String host, int port, URI uri, ProxyMap headers, byte[] body)
            throws IOException, InterruptedException {

        var connectUri = UriBuilder.fromUri(uri).host(host).port(port).build();

        var builder = HttpRequest.newBuilder().uri(connectUri);
        for (var e : headers.entries()) {
            builder.header(e.getKey(), e.getValue());
        }
        builder.method(method, HttpRequest.BodyPublishers.ofByteArray(body));
        var request = builder.build();

        HttpResponse<byte[]> response =
                client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        response.headers()
                .map()
                .forEach(
                        (k, v) -> {
                            for (var s : v) {
                                headers.add(k, s);
                            }
                        });

        var h = new ArrayProxyMap();
        response.headers()
                .map()
                .forEach(
                        (k, v) -> {
                            for (var s : v) {
                                h.add(k, s);
                            }
                        });

        return new HandlerHttpResponse(response.statusCode(), h, response.body());
    }
}
