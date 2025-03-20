package io.roastedroot.proxywasm.jaxrs.spi;

/**
 * This interface will help us deal with differences in the http server impl.
 */
public interface HttpServer {

    Runnable scheduleTick(long delay, Runnable task);
}
