package io.roastedroot.proxywasm.v1;

import com.dylibso.chicory.runtime.HostFunction;

import java.util.Arrays;

public class Helpers {

    static public HostFunction[] withModuleName(HostFunction[] hostFunctions, String moduleName) {
        return Arrays.stream(hostFunctions).map(hf ->
                new HostFunction(moduleName, hf.name(), hf.paramTypes(), hf.returnTypes(), hf.handle())
        ).toArray(HostFunction[]::new);
    }

}
