package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.ArrayProxyMap;
import io.roastedroot.proxywasm.ProxyMap;
import io.roastedroot.proxywasm.plugin.HttpCallResponse;
import io.roastedroot.proxywasm.plugin.HttpCallResponseHandler;
import io.roastedroot.proxywasm.plugin.HttpRequestAdaptor;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerAdaptor implements io.roastedroot.proxywasm.plugin.ServerAdaptor {

    ScheduledExecutorService tickExecutorService = Executors.newScheduledThreadPool(1);
    ExecutorService executorService = Executors.newWorkStealingPool(5);
    HttpClient client = HttpClient.newHttpClient();

    @Override
    public Runnable scheduleTick(long delay, Runnable task) {
        var f = tickExecutorService.scheduleAtFixedRate(task, delay, delay, TimeUnit.MILLISECONDS);
        return () -> {
            f.cancel(false);
        };
    }

    @Override
    public HttpRequestAdaptor httpRequestAdaptor(Object context) {
        return new JaxrsHttpRequestAdaptor();
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

        Callable<Object> task =
                () -> {
                    var resp = httpCall(method, host, port, uri, headers, body);
                    handler.call(resp);
                    return null;
                };
        var f = executorService.submit(task);
        Runnable cancel =
                () -> {
                    f.cancel(true);
                };
        // TODO: is there a better way to do this?
        if (timeout > 0) {
            tickExecutorService.schedule(cancel, timeout, TimeUnit.MILLISECONDS);
        }
        return cancel;
    }

    private HttpCallResponse httpCall(
            String method, String host, int port, URI uri, ProxyMap headers, byte[] body) {

        try {
            var connectUri = UriBuilder.fromUri(uri).host(host).port(port).build();

            var builder = HttpRequest.newBuilder().uri(connectUri);
            for (var e : headers.entries()) {
                try {
                    builder.header(e.getKey(), e.getValue());
                } catch (IllegalArgumentException ignore) {
                    // ignore
                }
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

            return new HttpCallResponse(response.statusCode(), h, response.body());
        } catch (Exception e) {
            return new HttpCallResponse(500, new ArrayProxyMap(), new byte[] {});
        }
    }
}
