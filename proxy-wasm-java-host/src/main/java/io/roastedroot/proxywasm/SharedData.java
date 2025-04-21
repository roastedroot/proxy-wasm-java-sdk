package io.roastedroot.proxywasm;

/**
 * Represents a unit of shared data retrieved from the host environment via
 * {@link SharedDataHandler#getSharedData(String)}.
 *
 * <p>This class encapsulates the data itself (as a byte array) and its associated
 * Compare-And-Swap (CAS) value. The CAS value acts as a version identifier, enabling
 * optimistic concurrency control when updating shared data using
 * {@link SharedDataHandler#setSharedData(String, byte[], int)}.
 */
public class SharedData {
    private final byte[] data;
    private final int cas;

    /**
     * Constructs a new SharedData instance.
     *
     * @param data The raw byte data retrieved from the shared data store.
     *             This might be {@code null} if the key was found but had no associated value,
     *             depending on the {@link SharedDataHandler} implementation.
     * @param cas  The Compare-And-Swap (version) value associated with this data.
     *             A value of 0 typically indicates the key did not exist or CAS is not supported/applicable.
     */
    public SharedData(byte[] data, int cas) {
        this.data = data;
        this.cas = cas;
    }

    /**
     * Gets the raw data bytes.
     *
     * @return The byte array representing the shared data's value.
     *         May be {@code null}.
     */
    public byte[] data() {
        return data;
    }

    /**
     * Gets the Compare-And-Swap (CAS) value associated with this data.
     * This value should be passed back to {@link SharedDataHandler#setSharedData(String, byte[], int)}
     * when performing a conditional update.
     *
     * @return The integer CAS value.
     */
    public int cas() {
        return cas;
    }
}
