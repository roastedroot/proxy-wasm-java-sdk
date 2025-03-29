package io.roastedroot.proxywasm.plugin;

import io.roastedroot.proxywasm.SharedData;
import io.roastedroot.proxywasm.SharedDataHandler;
import io.roastedroot.proxywasm.WasmException;
import io.roastedroot.proxywasm.WasmResult;
import java.util.HashMap;

public class SimpleSharedDataHandler implements SharedDataHandler {

    private final HashMap<String, SharedData> sharedData = new HashMap<>();

    @Override
    public synchronized SharedData getSharedData(String key) throws WasmException {
        return sharedData.get(key);
    }

    @Override
    public synchronized WasmResult setSharedData(String key, byte[] value, int cas) {
        SharedData prev = sharedData.get(key);
        if (prev == null) {
            if (cas == 0) {
                sharedData.put(key, new SharedData(value, 0));
                return WasmResult.OK;
            } else {
                return WasmResult.CAS_MISMATCH;
            }
        } else {
            if (cas == 0 || prev.cas() == cas) {
                sharedData.put(key, new SharedData(value, prev.cas() + 1));
                return WasmResult.OK;
            } else {
                return WasmResult.CAS_MISMATCH;
            }
        }
    }
}
