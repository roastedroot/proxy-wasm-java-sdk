package io.roastedroot.proxywasm;

public class SharedData {
    private final byte[] data;
    private final int cas;

    public SharedData(byte[] data, int cas) {
        this.data = data;
        this.cas = cas;
    }

    public byte[] data() {
        return data;
    }

    public int cas() {
        return cas;
    }
}
