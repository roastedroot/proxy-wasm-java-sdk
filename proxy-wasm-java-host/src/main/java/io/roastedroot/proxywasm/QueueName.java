package io.roastedroot.proxywasm;

import java.util.Objects;

/**
 * Represents the identifier for a shared queue within the Proxy-WASM environment.
 * A queue name is typically composed of a Virtual Machine (VM) identifier and a
 * queue-specific name string.
 *
 * <p>The VM ID provides a namespace, allowing different VMs (potentially running
 * different WASM modules or configurations) to define queues with the same name string
 * without collision.
 *
 * <p>Instances of this class are immutable.
 */
public class QueueName {

    private final String vmId;
    private final String name;

    /**
     * Constructs a new QueueName.
     *
     * @param vmId The identifier of the VM context associated with this queue.
     *             Cannot be null.
     * @param name The specific name of the queue within the VM context.
     *             Cannot be null.
     * @throws NullPointerException if either {@code vmId} or {@code name} is null.
     */
    public QueueName(String vmId, String name) {
        this.vmId = Objects.requireNonNull(vmId, "vmId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueueName queue = (QueueName) o;
        return Objects.equals(vmId, queue.vmId) && Objects.equals(name, queue.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vmId, name);
    }

    /**
     * Gets the VM identifier part of the queue name.
     *
     * @return The non-null VM ID string.
     */
    public String vmId() {
        return vmId;
    }

    /**
     * Gets the specific name string part of the queue name.
     *
     * @return The non-null queue name string.
     */
    public String name() {
        return name;
    }
}
