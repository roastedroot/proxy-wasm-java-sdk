package io.roastedroot.proxywasm;

/**
 * StartException is thrown when there is an error during the start of a plugin.
 */
public class StartException extends Exception {
    public StartException(String message) {
        super(message);
    }

    public StartException(String message, Throwable cause) {
        super(message, cause);
    }
}
