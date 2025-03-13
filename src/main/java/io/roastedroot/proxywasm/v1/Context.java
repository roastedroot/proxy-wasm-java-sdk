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

        proxyWasm.exports().proxyOnLog(id);

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
