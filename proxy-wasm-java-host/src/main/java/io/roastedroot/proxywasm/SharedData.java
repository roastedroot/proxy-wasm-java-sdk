package io.roastedroot.proxywasm;

public class SharedData {
    public byte[] data;
    public int cas;

    public SharedData(byte[] data, int cas) {
        this.data = data;
        this.cas = cas;
    }
}
