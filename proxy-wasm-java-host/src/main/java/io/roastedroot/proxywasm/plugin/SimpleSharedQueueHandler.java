package io.roastedroot.proxywasm.plugin;

import io.roastedroot.proxywasm.QueueName;
import io.roastedroot.proxywasm.SharedQueueHandler;
import io.roastedroot.proxywasm.WasmException;
import io.roastedroot.proxywasm.WasmResult;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleSharedQueueHandler implements SharedQueueHandler {

    public static class SharedQueue {
        public final QueueName queueName;
        public final LinkedList<byte[]> data = new LinkedList<>();
        public final int id;

        public SharedQueue(QueueName queueName, int id) {
            this.queueName = queueName;
            this.id = id;
        }
    }

    private final AtomicInteger lastSharedQueueId = new AtomicInteger(0);
    private final HashMap<Integer, SharedQueue> sharedQueues = new HashMap<>();

    public synchronized SharedQueue getSharedQueue(int queueId) {
        return sharedQueues.get(queueId);
    }

    @Override
    public synchronized WasmResult enqueueSharedQueue(int queueId, byte[] value) {
        SharedQueue queue = sharedQueues.get(queueId);
        if (queue == null) {
            return WasmResult.NOT_FOUND;
        }
        queue.data.add(value);
        return WasmResult.OK;
    }

    @Override
    public synchronized byte[] dequeueSharedQueue(int queueId) throws WasmException {
        SharedQueue queue = sharedQueues.get(queueId);
        if (queue == null) {
            throw new WasmException(WasmResult.NOT_FOUND);
        }
        return queue.data.poll();
    }

    @Override
    public synchronized int resolveSharedQueue(QueueName queueName) throws WasmException {
        var existing =
                sharedQueues.values().stream()
                        .filter(x -> x.queueName.equals(queueName))
                        .findFirst();
        if (existing.isPresent()) {
            return existing.get().id;
        } else {
            throw new WasmException(WasmResult.NOT_FOUND);
        }
    }

    @Override
    public synchronized int registerSharedQueue(QueueName queueName) throws WasmException {
        var existing =
                sharedQueues.values().stream()
                        .filter(x -> x.queueName.equals(queueName))
                        .findFirst();
        if (existing.isPresent()) {
            return existing.get().id;
        } else {
            int id = lastSharedQueueId.incrementAndGet();
            sharedQueues.put(id, new SharedQueue(queueName, id));
            return id;
        }
    }
}
