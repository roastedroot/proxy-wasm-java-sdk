package io.roastedroot.proxywasm;

public interface SharedDataHandler {

    default SharedData getSharedData(String key) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default WasmResult setSharedData(String key, byte[] value, int cas) {
        return WasmResult.UNIMPLEMENTED;
    }
}
