package io.roastedroot.proxywasm.v1;

import io.roastedroot.proxywasm.impl.Exports;

import java.io.Closeable;

public class Context implements Closeable {

    final Exports exports;
    final int contextID;

    public Context(Exports exports, int contextID) {
        this.exports = exports;
        this.contextID = contextID;
    }

    public void close() {
        exports.proxyOnDelete(contextID);
    }

    public int onRequestHeaders(int headerCount, boolean endOfStream) {
        return exports.proxyOnRequestHeaders(contextID, headerCount, endOfStream ? 1 : 0);
    }
}
