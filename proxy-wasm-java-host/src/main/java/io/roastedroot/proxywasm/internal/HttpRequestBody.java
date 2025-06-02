package io.roastedroot.proxywasm.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Holds the HTTP request body, loading it from a stream when first accessed.
 */
public class HttpRequestBody {

    private byte[] body;
    private boolean loaded = false;
    private final Supplier<InputStream> streamSupplier;

    public HttpRequestBody(Supplier<InputStream> streamSupplier) {
        this.streamSupplier = streamSupplier;
    }

    public byte[] get() {
        if (!loaded) {
            try {
                body = streamSupplier.get().readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException("Failed to read request body", e);
            }
            loaded = true;
        }
        return body;
    }

    /**
     * Returns true if the request body has been loaded from the stream.
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Sets the request body directly, marking it as loaded.
     * This is used when the body is modified by WASM plugins.
     */
    public void setBody(byte[] body) {
        this.body = body;
        this.loaded = true;
    }

    /**
     * Returns the request body if it has been loaded, null otherwise.
     * This allows checking if the body was accessed without triggering a load.
     */
    public byte[] getBodyIfLoaded() {
        return loaded ? body : null;
    }
}
