package io.roastedroot.proxywasm;

/**
 * A functional interface that represents a foreign function call in the Proxy-Wasm.
 */
public interface ForeignFunction extends java.util.function.Function<byte[], byte[]> {}
