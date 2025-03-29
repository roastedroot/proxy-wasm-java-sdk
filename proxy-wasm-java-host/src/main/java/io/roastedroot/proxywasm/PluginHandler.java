package io.roastedroot.proxywasm;

public interface PluginHandler {

    /**
     * Get the plugin configuration.
     *
     * @return The plugin configuration as a byte[], or null if not available
     */
    default byte[] getPluginConfig() {
        return null;
    }

    /**
     * Get the VM configuration.
     *
     * @return The VM configuration as a byte[], or null if not available
     */
    default byte[] getVmConfig() {
        return null;
    }

    /**
     * Sets a low-resolution timer period (tick_period).
     *
     * When set, the host will call proxy_on_tick every tickPeriodMilliseconds milliseconds. Setting tickPeriodMilliseconds to 0 disables the timer.
     *
     * @return The current time in nanoseconds
     */
    default WasmResult setTickPeriodMilliseconds(int tickPeriodMilliseconds) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Retrieves current time  or the approximation of it.
     *
     * Note Hosts might return approximate time (e.g. frozen at the context creation) to improve performance and/or prevent various attacks.
     *
     * @return The current time in nanoseconds
     */
    default int getCurrentTimeNanoseconds() throws WasmException {
        return (int) System.currentTimeMillis() * 1000000;
    }
}
