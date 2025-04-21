package io.roastedroot.proxywasm.internal;

import java.net.URI;

/**
 * The ServerAdaptor interface provides adapting plugins to the server environment.
 */
public interface ServerAdaptor {

    Runnable scheduleTick(long delay, Runnable task);

    HttpRequestAdaptor httpRequestAdaptor(Object context);

    default Runnable scheduleHttpCall(
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
        throw new UnsupportedOperationException("scheduleHttpCall not implemented");
    }

    default Runnable scheduleGrpcCall(
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
        throw new UnsupportedOperationException("scheduleGrpcCall not implemented");
    }
}
