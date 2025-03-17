package io.roastedroot.proxywasm;

import java.util.Objects;

public class QueueName {
    private final String vmId;
    private final String name;

    public QueueName(String vmId, String name) {
        this.vmId = vmId;
        this.name = name;
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

    public String vmId() {
        return vmId;
    }

    public String name() {
        return name;
    }
}
