package io.roastedroot.proxywasm.v1;

import static io.roastedroot.proxywasm.v1.Helpers.len;

public class NetworkContext extends Context {

    private final Handler handler;

    NetworkContext(ProxyWasm proxyWasm, Handler handler) {
        super(proxyWasm);
        this.handler = handler;
    }

    Handler handler() {
        return handler;
    }

    public Action callOnNewConnection() {
        int result = proxyWasm.abi().proxyOnNewConnection(id);
        Action action = Action.fromInt(result);
        handler.setAction(StreamType.DOWNSTREAM, action);
        return action;
    }

    public Action callOnDownstreamData(boolean endOfStream) {
        var data = handler.getDownStreamData();
        var result = proxyWasm.abi().proxyOnDownstreamData(id, len(data), endOfStream ? 1 : 0);
        Action action = Action.fromInt(result);
        handler.setAction(StreamType.DOWNSTREAM, action);
        return action;
    }

    public Action callOnUpstreamData(boolean endOfStream) {
        var data = handler.getUpstreamData();
        var result = proxyWasm.abi().proxyOnUpstreamData(id, len(data), endOfStream ? 1 : 0);
        Action action = Action.fromInt(result);
        handler.setAction(StreamType.UPSTREAM, action);
        return action;
    }

    public void callOnDownstreamConnectionClose(PeerType type) {
        proxyWasm.abi().proxyOnDownstreamConnectionClose(id, type.getValue());
    }

    public void callOnDownstreamConnectionClose() {
        // peerType will be removed in the next ABI
        callOnDownstreamConnectionClose(PeerType.LOCAL);
    }
}
