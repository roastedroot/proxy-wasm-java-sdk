package io.roastedroot.proxywasm.v1;

public enum ABIVersion {
    // The values follow the same order as in the Go code using iota (0-based incrementing)
    V0_1_0("proxy_abi_version_0_1_0"),
    V0_2_0("proxy_abi_version_0_2_0"),
    V0_2_1("proxy_abi_version_0_2_1");

    private final String abiMarkerFunction;

    /**
     * Constructor for ABIVersion enum.
     *
     * @param value The version string of the ABI
     */
    ABIVersion(String value) {
        this.abiMarkerFunction = value;
    }

    public String getAbiMarkerFunction() {
        return abiMarkerFunction;
    }

}
