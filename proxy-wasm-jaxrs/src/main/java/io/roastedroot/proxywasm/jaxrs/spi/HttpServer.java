package io.roastedroot.proxywasm.jaxrs.spi;

import io.roastedroot.proxywasm.ProxyMap;
import java.net.URI;

/**
 * This interface will help us deal with differences in the http server impl.
 */
public interface HttpServer {

    Runnable scheduleTick(long delay, Runnable task);

    Runnable scheduleHttpCall(
            String method,
            String host,
            int port,
            URI uri,
            ProxyMap headers,
            byte[] body,
            ProxyMap trailers,
            int timeout,
            HandlerHttpResponseHandler handler)
            throws InterruptedException;

    class HandlerHttpResponse {

        public final int statusCode;
        public final ProxyMap headers;
        public final byte[] body;

        public HandlerHttpResponse(int statusCode, ProxyMap headers, byte[] body) {
            this.statusCode = statusCode;
            this.headers = headers;
            this.body = body;
        }
    }

    interface HandlerHttpResponseHandler {
        void call(HandlerHttpResponse response);
    }
}
