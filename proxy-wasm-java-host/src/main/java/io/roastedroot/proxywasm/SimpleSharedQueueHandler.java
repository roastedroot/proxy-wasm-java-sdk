package io.roastedroot.proxywasm;

import io.roastedroot.proxywasm.internal.WasmResult;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A basic, in-memory implementation of the {@link SharedQueueHandler} interface.
 *
 * <p>This handler manages shared queues entirely within the host's memory using standard Java collections.
 * It is suitable for single-process environments or testing scenarios where queue persistence or
 * cross-process sharing is not required.
 *
 * <p>All operations on this handler are synchronized to ensure thread safety within a single JVM.
 */
public class SimpleSharedQueueHandler implements SharedQueueHandler {

    /**
     * Default constructor.
     */
    public SimpleSharedQueueHandler() {
        // Default constructor for SimpleSharedQueueHandler
    }

    /**
     * Represents an individual shared queue managed by {@link SimpleSharedQueueHandler}.
     * Each queue has a unique name (within its VM), a host-assigned ID, and holds its data
     * in an in-memory list.
     */
    public static class SharedQueue {
        /** The name identifying this queue, composed of a VM ID and queue name string. */
        public final QueueName queueName;

        /** The underlying list holding the byte array messages in the queue (FIFO order). */
        public final LinkedList<byte[]> data = new LinkedList<>();

        /** The unique integer ID assigned to this queue by the handler. */
        public final int id;

        /**
         * Constructs a new SharedQueue instance.
         *
         * @param queueName The {@link QueueName} identifying this queue.
         * @param id        The unique integer ID assigned by the handler.
         */
        public SharedQueue(QueueName queueName, int id) {
            this.queueName = queueName;
            this.id = id;
        }
    }

    private final AtomicInteger lastSharedQueueId = new AtomicInteger(0);
    private final HashMap<Integer, SharedQueue> sharedQueues = new HashMap<>();

    /**
     * Retrieves the internal {@link SharedQueue} object associated with the given ID.
     * This is primarily for internal use or testing/inspection.
     *
     * @param queueId The ID of the queue to retrieve.
     * @return The {@link SharedQueue} instance, or {@code null} if no queue exists with that ID.
     */
    public synchronized SharedQueue getSharedQueue(int queueId) {
        return sharedQueues.get(queueId);
    }

    /**
     * Adds a message (byte array) to the end of the specified queue.
     *
     * @param queueId The ID of the target queue.
     * @param value   The message data to enqueue.
     * @return {@link WasmResult#OK} if successful, or {@link WasmResult#NOT_FOUND} if the queue ID is invalid.
     */
    @Override
    public synchronized WasmResult enqueueSharedQueue(int queueId, byte[] value) {
        SharedQueue queue = sharedQueues.get(queueId);
        if (queue == null) {
            return WasmResult.NOT_FOUND;
        }
        queue.data.add(value);
        return WasmResult.OK;
    }

    /**
     * Removes and returns the message at the front of the specified queue.
     *
     * @param queueId The ID of the target queue.
     * @return The dequeued message data as a byte array, or {@code null} if the queue is empty.
     * @throws WasmException with {@link WasmResult#NOT_FOUND} if the queue ID is invalid.
     */
    @Override
    public synchronized byte[] dequeueSharedQueue(int queueId) throws WasmException {
        SharedQueue queue = sharedQueues.get(queueId);
        if (queue == null) {
            throw new WasmException(WasmResult.NOT_FOUND);
        }
        return queue.data.poll();
    }

    /**
     * Finds the ID of an existing queue based on its name.
     *
     * @param queueName The {@link QueueName} (VM ID and name string) to look up.
     * @return The integer ID of the existing queue.
     * @throws WasmException with {@link WasmResult#NOT_FOUND} if no queue with the specified name exists.
     */
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

    /**
     * Registers a new shared queue with the given name or returns the ID if it already exists.
     * If the queue does not exist, a new unique ID is generated and associated with the name.
     *
     * @param queueName The {@link QueueName} (VM ID and name string) to register or resolve.
     * @return The integer ID of the (potentially newly created) queue.
     * @throws WasmException (Although not declared, could potentially occur in subclass implementations
     *                      or future versions, e.g., due to resource limits. Current implementation does not throw.)
     */
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
