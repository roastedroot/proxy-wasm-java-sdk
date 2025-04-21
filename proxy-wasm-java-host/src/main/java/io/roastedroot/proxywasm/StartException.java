package io.roastedroot.proxywasm;

public class StartException extends Exception {
    public StartException(String message) {
        super(message);
    }

    public StartException(String message, Throwable cause) {
        super(message, cause);
    }
}
