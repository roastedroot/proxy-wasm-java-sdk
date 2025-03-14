package io.roastedroot.proxywasm.v1;

public interface ForeignFunction {
    byte[] apply(byte[] data);
}
