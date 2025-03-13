package io.roastedroot.proxywasm.v1;

import java.io.Closeable;

public abstract class Context implements Closeable {

    ProxyWasm proxyWasm;
    final int id;
    boolean closeDone;
    boolean closeStarted;

    Context(ProxyWasm proxyWasm) {
        this.proxyWasm = proxyWasm;
        this.id = proxyWasm.nextContextID();
    }

    abstract Handler handler();

    void activate() throws WasmException {
        if (this != proxyWasm.getActiveContext()) {
            proxyWasm.setActiveContext(this);
            proxyWasm.exports().proxySetEffectiveContext(id);
        }
    }

    public int id() {
        return id;
    }

    public void close() {
        if (closeStarted) {
            return;
        }
        closeStarted = true;
        if (!closeDone) {
            // the plugin may want to finish closing later...
            if (proxyWasm.exports().proxyOnDone(id)) {
                // close now...
                finishClose();
            }
        }
    }

    // plugin is indicating it wants to finish closing
    // TODO: need a unit test for this.  We likely can't implement the test until we provide tick
    // callbacks to the plugin.
    WasmResult done() {
        if (!closeStarted) {
            // spec says: return NOT_FOUND when active context was not pending finalization.
            return WasmResult.NOT_FOUND;
        }
        if (!closeDone) {
            finishClose();
        }
        return WasmResult.OK;
    }

    protected void finishClose() {
        closeDone = true;

        // TODO: I think we need to call proxyOnLog for all ABI version, but we have a 0.1.0 wasm
        // test
        // that fails, need to see if the .wasm file is wrong or if we really need to do this
        if (proxyWasm.abiVersion() != ABIVersion.V0_1_0) {
            proxyWasm.exports().proxyOnLog(id);
        }

        proxyWasm.contexts().remove(id);
        // todo: we likely need to callback to user code to allow cleaning up resources like http
        // connections.

        // I think we should allways be the current context...
        assert proxyWasm.getActiveContext() == this : "we are the active context";

        // unset active context so that callbacks don't try to use us.
        proxyWasm.setActiveContext(null);
        proxyWasm.exports().proxyOnDelete(id);
    }
}
