package io.roastedroot.proxywasm;

import io.roastedroot.proxywasm.internal.WasmResult;

public interface SharedDataHandler {

    SharedDataHandler DEFAULT = new SharedDataHandler() {};

    default SharedData getSharedData(String key) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default WasmResult setSharedData(String key, byte[] value, int cas) {
        return WasmResult.UNIMPLEMENTED;
    }
}
