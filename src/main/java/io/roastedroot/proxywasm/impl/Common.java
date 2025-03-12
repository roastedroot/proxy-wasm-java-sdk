package io.roastedroot.proxywasm.impl;

import com.dylibso.chicory.runtime.Instance;
import io.roastedroot.proxywasm.v1.WasmException;
import io.roastedroot.proxywasm.v1.WasmResult;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Common methods used to implement the Wasm modules.
 *
 * Some of it may seem not-java idomatic, that's because it's being used to translate
 * from a go version
 *
 * Once the translation is done, we can refactor it to be more idiomatic.
 */
public abstract class Common {

    // Size of a 32-bit integer in bytes
    static final int U32_LEN = 4;

    Instance instance;

    public Instance getInstance() {
        return this.instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    /**
     * Write a 32-bit unsigned integer to memory.
     *
     * @param address The address to write to
     * @param value The value to write
     * @throws WasmException if the memory access is invalid
     */
    void putUint32(int address, int value) throws WasmException {
        try {
            instance.memory().writeI32(address, value);
        } catch (RuntimeException e) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
    }

    /**
     * Read a 32-bit unsigned integer to memory.
     *
     * @param address The address to read from
     * @throws WasmException if the memory access is invalid
     */
    long getUint32(int address) throws WasmException {
        try {
            return instance.memory().readU32(address);
        } catch (RuntimeException e) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
    }

    /**
     * Write a byte to memory.
     *
     * @param address The address to write to
     * @param value The value to write
     * @throws WasmException if the memory access is invalid
     */
    void putByte(int address, byte value) throws WasmException {
        try {
            instance.memory().writeByte(address, value);
        } catch (RuntimeException e) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
    }

    /**
     * Write bytes to memory.
     *
     * @param address The address to write to
     * @param data The data to write
     * @throws WasmException if the memory access is invalid
     */
    void putMemory(int address, byte[] data) throws WasmException {
        try {
            // TODO: do we need a better writeU32 method?
            instance.memory().write(address, data);
        } catch (RuntimeException e) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
    }

    /**
     * Write bytes to memory.
     *
     * @param address The address to write to
     * @param data The data to write
     * @throws WasmException if the memory access is invalid
     */
    void putMemory(int address, ByteBuffer data) throws WasmException {
        try {
            if (data.hasArray()) {
                var array = data.array();
                instance.memory().write(address, array, data.position(), data.remaining());
            } else {
                // This could likely be optimized by extending the memory interface to accept
                // ByteBuffer
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                instance.memory().write(address, bytes);
            }
        } catch (RuntimeException e) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
    }

    /**
     * Read bytes from memory.
     *
     * @param address The address to read from
     * @param len The number of bytes to read
     * @return The value read
     * @throws WasmException if the memory access is invalid
     */
    byte[] readMemory(int address, int len) throws WasmException {
        try {
            return instance.memory().readBytes(address, len);
        } catch (RuntimeException e) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
    }

    String readString(int address, int len) throws WasmException {
        var data = readMemory(address, len);
        return new String(data, StandardCharsets.UTF_8);
    }

    void copyIntoInstance(String value, int retPtr, int retSize) throws WasmException {
        copyIntoInstance(value.getBytes(), retPtr, retSize);
    }

    void copyIntoInstance(byte[] value, int retPtr, int retSize) throws WasmException {
        try {
            int addr = malloc(value.length);
            putMemory(addr, value);
            putUint32(retPtr, addr);
            putUint32(retSize, value.length);
        } catch (WasmException e) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
    }

    abstract int malloc(int length) throws WasmException;
}
