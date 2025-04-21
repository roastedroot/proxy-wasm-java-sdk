package io.quarkiverse.proxywasm.runtime;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.roastedroot.proxywasm.internal.ArrayBytesProxyMap;
import io.roastedroot.proxywasm.internal.GrpcCallResponseHandler;
import io.roastedroot.proxywasm.internal.HttpCallResponseHandler;
import io.roastedroot.proxywasm.internal.HttpRequestAdaptor;
import io.roastedroot.proxywasm.internal.ProxyMap;
import io.roastedroot.proxywasm.internal.ServerAdaptor;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
                                                            result.statusCode(),
                                                            h,
                                                            bodyHandler.getBytes());
                                                });
                                    } else {
                                        handler.call(
                                                500,
                                                ProxyMap.of(),
                                                resp.cause().getMessage().getBytes());
                                    }
                                });

        return () -> {
            // There doesn't seem to be a way to cancel the request.
        };
    }

    @Override
    public Runnable scheduleGrpcCall(
            String host,
            int port,
            boolean plainText,
            String serviceName,
            String methodName,
            ProxyMap headers,
            byte[] message,
            int timeoutMillis,
            GrpcCallResponseHandler handler)
            throws InterruptedException {

        ManagedChannelBuilder<?> managedChannelBuilder =
                ManagedChannelBuilder.forAddress(host, port);
        if (plainText) {
            managedChannelBuilder.usePlaintext();
        } else {
            managedChannelBuilder.useTransportSecurity();
        }
        ManagedChannel channel = managedChannelBuilder.build();

        // Construct method descriptor (assuming unary request/response and protobuf)
        MethodDescriptor<byte[], byte[]> methodDescriptor =
                MethodDescriptor.<byte[], byte[]>newBuilder()
                        .setType(MethodDescriptor.MethodType.UNARY)
                        .setFullMethodName(
                                MethodDescriptor.generateFullMethodName(serviceName, methodName))
                        .setRequestMarshaller(new BytesMessageMarshaller())
                        .setResponseMarshaller(new BytesMessageMarshaller())
                        .build();

        ClientCall<byte[], byte[]> call =
                channel.newCall(
                        methodDescriptor,
                        CallOptions.DEFAULT.withDeadlineAfter(
                                timeoutMillis * 1000, TimeUnit.MILLISECONDS));

        Metadata metadata = new Metadata();
        for (Map.Entry<String, String> entry : headers.entries()) {
            Metadata.Key<String> key =
                    Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER);
            metadata.put(key, entry.getValue());
        }

        call.start(
                new ClientCall.Listener<>() {

                    @Override
                    public void onReady() {
                        super.onReady();
                    }

                    @Override
                    public void onHeaders(Metadata metadata) {
                        if (metadata.keys().isEmpty()) {
                            return;
                        }
                        ArrayBytesProxyMap trailerMap = new ArrayBytesProxyMap();
                        for (var key : metadata.keys()) {
                            if (key.endsWith("-bin")) {
                                var value =
                                        metadata.get(
                                                Metadata.Key.of(
                                                        key, Metadata.BINARY_BYTE_MARSHALLER));
                                trailerMap.add(key, value);
                            } else {
                                var value =
                                        metadata.get(
                                                Metadata.Key.of(
                                                        key, Metadata.ASCII_STRING_MARSHALLER));
                                trailerMap.add(key, value);
                            }
                        }
                        handler.onHeaders(trailerMap);
                    }

                    @Override
                    public void onMessage(byte[] data) {
                        handler.onMessage(data);
                    }

                    @Override
                    public void onClose(Status status, Metadata metadata) {
                        if (!metadata.keys().isEmpty()) {
                            ArrayBytesProxyMap trailerMap = new ArrayBytesProxyMap();
                            for (var key : metadata.keys()) {
                                var value =
                                        metadata.get(
                                                Metadata.Key.of(
                                                        key, Metadata.BINARY_BYTE_MARSHALLER));
                                trailerMap.add(key, value);
                            }
                            handler.onTrailers(trailerMap);
                        }
                        handler.onClose(status.getCode().value());
                        channel.shutdownNow();
                    }
                },
                metadata);
        call.sendMessage(message);
        call.halfClose();
        call.request(1); // Request a single response
        return () -> {
            call.cancel("shutdown", null);
            channel.shutdownNow();
        };
    }

    static class BytesMessageMarshaller implements io.grpc.MethodDescriptor.Marshaller<byte[]> {
        @Override
        public InputStream stream(byte[] value) {
            return new ByteArrayInputStream(value);
        }

        @Override
        public byte[] parse(InputStream stream) {
            try {
                return stream.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
