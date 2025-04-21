package io.roastedroot.proxywasm;

import io.roastedroot.proxywasm.internal.WasmResult;

/**
 * Defines the contract for handling shared message queues accessible by Proxy-WASM modules.
 * Implementations of this interface manage the registration, resolution, and data manipulation
 * (enqueue/dequeue) of queues that can potentially be accessed by multiple WASM modules
 * or different instances of the same module, depending on the host environment's implementation.
 *
 * <p>Shared queues provide a mechanism for inter-plugin communication or for passing data
 * between different processing stages involving WASM modules.
 */
public interface SharedQueueHandler {

    /**
     * A default, non-functional instance of {@code SharedQueueHandler}.
     * This instance throws {@link WasmException} with {@link WasmResult#UNIMPLEMENTED}
     * for methods that register, resolve, or dequeue from queues, and returns
     * {@link WasmResult#UNIMPLEMENTED} for enqueue operations.
     * Useful as a placeholder or base when shared queue functionality is not supported or needed.
     */
    SharedQueueHandler DEFAULT = new SharedQueueHandler() {};

    /**
     * Registers a shared queue with the given name, creating it if it doesn't exist,
     * and returns its unique identifier (queue ID).
     *
     * @param name The {@link QueueName} uniquely identifying the queue (VM ID + queue name string).
     * @return The unique integer ID assigned to the queue.
     * @throws WasmException If the queue cannot be registered (e.g., resource limits, invalid name)
     *                       or if the operation is unimplemented by the host.
     */
    default int registerSharedQueue(QueueName name) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    /**
     * Resolves the unique identifier (queue ID) for a shared queue given its name.
     * This does not create the queue if it doesn't exist.
     *
     * @param name The {@link QueueName} uniquely identifying the queue (VM ID + queue name string).
     * @return The unique integer ID of the existing queue.
     * @throws WasmException If the queue cannot be found ({@link WasmResult#NOT_FOUND}),
     *                       or if the operation is unimplemented by the host.
     */
    default int resolveSharedQueue(QueueName name) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    /**
     * Removes and returns the next available message (byte array) from the front of the specified queue.
     *
     * @param queueId The unique integer ID of the target queue.
     * @return The dequeued message data as a byte array.
     * @throws WasmException If the queue is empty ({@link WasmResult#EMPTY}), the queue ID is invalid
     *                       ({@link WasmResult#NOT_FOUND}), or the operation is unimplemented by the host.
     */
    default byte[] dequeueSharedQueue(int queueId) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    /**
     * Adds a message (byte array) to the end of the specified shared queue.
     *
     * @param queueId The unique integer ID of the target queue.
     * @param value   The message data to enqueue.
     * @return A {@link WasmResult} indicating the outcome (e.g., {@link WasmResult#OK},
     *         {@link WasmResult#NOT_FOUND}, {@link WasmResult#UNIMPLEMENTED}).
     */
    default WasmResult enqueueSharedQueue(int queueId, byte[] value) {
        return WasmResult.UNIMPLEMENTED;
    }
}
