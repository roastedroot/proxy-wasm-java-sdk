package io.roastedroot.proxywasm;

public interface ForeignFunction {
    byte[] apply(byte[] data);
}
