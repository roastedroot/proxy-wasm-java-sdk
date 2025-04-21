package io.roastedroot.proxywasm.internal;

/**
 * Holds constants for the well-known header keys.
 */
public final class WellKnownHeaders {
    private WellKnownHeaders() {}

    public static final String SCHEME = ":scheme";
    public static final String AUTHORITY = ":authority";
    public static final String PATH = ":path";
    public static final String METHOD = ":method";
    public static final String STATUS = ":status";
}
