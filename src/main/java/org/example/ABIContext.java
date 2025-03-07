package org.example;

import com.dylibso.chicory.runtime.ByteBufferMemory;
import com.dylibso.chicory.runtime.ImportMemory;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.WasmModule;
import com.dylibso.chicory.wasm.types.MemoryLimits;
import io.roastedroot.proxywasm.impl.ImportsV1;
import io.roastedroot.proxywasm.impl.ImportsV1_ModuleFactory;

import java.util.ArrayList;
import java.util.List;

public class ABIContext {

    private final Instance instance;
    private final List<Object> objects = new ArrayList<>();
    
    public ABIContext(WasmModule module) {

        var importsV1 = new ImportsV1();
        var imports = ImportValues.builder()
                .addMemory(new ImportMemory("env", "memory",
                        new ByteBufferMemory(new MemoryLimits(2, MemoryLimits.MAX_PAGES))))
                .addFunction(ImportsV1_ModuleFactory.toHostFunctions(importsV1))
                .build();

        instance = Instance.builder(module)
                .withImportValues(imports)
                .build();
        importsV1.setInstance(instance);

    }


}
