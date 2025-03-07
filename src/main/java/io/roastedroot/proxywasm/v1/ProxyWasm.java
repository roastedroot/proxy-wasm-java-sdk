package io.roastedroot.proxywasm.v1;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import io.roastedroot.proxywasm.impl.Exports;
import io.roastedroot.proxywasm.impl.ImportsV1;
import io.roastedroot.proxywasm.impl.ImportsV1_ModuleFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class ProxyWasm  {


    private final Exports exports;
    private final ImportsV1 importsV1;
    private final AtomicInteger nextContextID = new AtomicInteger(1);
    private int rootContextID = 0;

    protected ProxyWasm(ProxyWasm other) {
        this.exports = other.exports;
        this.importsV1 = other.importsV1;
        this.nextContextID.set(other.nextContextID.get());
        this.rootContextID = other.rootContextID;
    }

    private ProxyWasm(Instance instance, ImportsV1 importsV1) {
        this.exports = new Exports(instance);
        this.importsV1 = importsV1;
    }

    public static ProxyWasm.Builder builder() {
        return new ProxyWasm.Builder();
    }



    public static final class Builder {
        private final ImportsV1 importsV1 = new ImportsV1();;

        public HostFunction[] toHostFunctions() {
            return ImportsV1_ModuleFactory.toHostFunctions(importsV1);
        }

        Builder() {
        }

        public ProxyWasm build(Instance instance) {
            importsV1.setInstance(instance);
            return new ProxyWasm(instance, importsV1);
        }

    }

    public void setHandler(Handler handler) {
        importsV1.setHandler(handler);
    }


    public Context createContext() {

        if (importsV1.getHandler() == null) {
            throw new IllegalStateException("Handler not set");
        }

        // there should be only one root context
        // we lazily create it, to allow the handler to be set first
        if (rootContextID == 0) {
            this.rootContextID = nextContextID.getAndIncrement();
            exports.proxyOnContextCreate(rootContextID, 0);
        }

        var contextID = nextContextID.getAndIncrement();
        exports.proxyOnContextCreate(contextID, this.rootContextID);
        return new Context(exports, contextID);
    }

}