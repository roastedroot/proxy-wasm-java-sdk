package io.roastedroot.proxywasm.plugin;

import io.roastedroot.proxywasm.ProxyMap;
import java.net.URI;

/**
 * This interface will help us deal with differences in the http server impl.
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
