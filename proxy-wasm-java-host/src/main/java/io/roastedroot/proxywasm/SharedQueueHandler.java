package io.roastedroot.proxywasm;

import io.roastedroot.proxywasm.internal.WasmResult;

public interface SharedQueueHandler {

    SharedQueueHandler DEFAULT = new SharedQueueHandler() {};

    default int registerSharedQueue(QueueName name) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default int resolveSharedQueue(QueueName name) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default byte[] dequeueSharedQueue(int queueId) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default WasmResult enqueueSharedQueue(int queueId, byte[] value) {
        return WasmResult.UNIMPLEMENTED;
    }
}
