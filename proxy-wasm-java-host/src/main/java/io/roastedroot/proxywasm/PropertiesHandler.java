package io.roastedroot.proxywasm;

import java.util.List;

public interface PropertiesHandler {

    default byte[] getProperty(List<String> key) throws WasmException {
        return null;
    }

    default WasmResult setProperty(List<String> path, byte[] value) {
        return WasmResult.UNIMPLEMENTED;
    }
}
