package io.roastedroot.proxywasm.internal;

import java.util.function.Supplier;

/**
 * Holds the HTTP response body, loading it from a supplier when first accessed.
 * Unlike request bodies which come from streams, response bodies can be provided
 * directly as byte arrays or from suppliers.
 */
public class HttpResponseBody {

    private byte[] body;
    private boolean loaded = false;
    private final Supplier<byte[]> bodySupplier;

    public HttpResponseBody(Supplier<byte[]> bodySupplier) {
        this.bodySupplier = bodySupplier;
    }

    /**
     * Creates an HttpResponseBody with a fixed byte array (no lazy loading needed).
     */
    public HttpResponseBody(byte[] body) {
        this.body = body;
        this.loaded = true;
        this.bodySupplier = null;
    }

    public byte[] get() {
        if (!loaded) {
            if (bodySupplier != null) {
                body = bodySupplier.get();
            } else {
                body = new byte[0];
            }
            loaded = true;
        }
        return body;
    }

    /**
     * Returns true if the response body has been loaded.
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Sets the response body directly, marking it as loaded.
     * This is used when the body is modified by WASM plugins.
     */
    public void setBody(byte[] body) {
        this.body = body;
        this.loaded = true;
    }

    /**
     * Returns the response body if it has been loaded, null otherwise.
     * This allows checking if the body was accessed without triggering a load.
     */
    public byte[] getBodyIfLoaded() {
        return loaded ? body : null;
    }
}
