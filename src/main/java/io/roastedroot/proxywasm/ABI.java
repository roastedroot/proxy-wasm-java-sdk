package io.roastedroot.proxywasm;

import static io.roastedroot.proxywasm.Helpers.replaceBytes;
import static io.roastedroot.proxywasm.Helpers.string;

import com.dylibso.chicory.experimental.hostmodule.annotations.HostModule;
import com.dylibso.chicory.experimental.hostmodule.annotations.WasmExport;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Memory;
import com.dylibso.chicory.runtime.WasmRuntimeException;
import com.dylibso.chicory.wasm.InvalidException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@HostModule("env")
public class ABI {

    private Handler handler;
    private Instance instance;

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public Instance.Exports exports() {
        return instance.exports();
    }

    public Memory memory() {
        return instance.memory();
    }

    // //////////////////////////////////////////////////////////////////////
    // Common Helpers
    // //////////////////////////////////////////////////////////////////////

    // Size of a 32-bit integer in bytes
    static final int U32_LEN = 4;

    public boolean instanceExportsFunction(String name) {
        try {
            this.exports().function(name);
            return true;
        } catch (InvalidException e) {
            return false;
        }
    }

    /**
     * Write a 32-bit unsigned integer to memory().
     *
     * @param address The address to write to
     * @param value   The value to write
     * @throws WasmException if the memory access is invalid
     */
    void putUint32(int address, int value) throws WasmException {
        try {
            memory().writeI32(address, value);
        } catch (RuntimeException e) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
    }

    /**
     * Read a 32-bit unsigned integer to memory().
     *
     * @param address The address to read from
     * @throws WasmException if the memory access is invalid
     */
    long getUint32(int address) throws WasmException {
        try {
            return memory().readU32(address);
        } catch (RuntimeException e) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
    }

    /**
     * Write a byte to memory().
     *
     * @param address The address to write to
     * @param value   The value to write
     * @throws WasmException if the memory access is invalid
     */
    void putByte(int address, byte value) throws WasmException {
        try {
            memory().writeByte(address, value);
        } catch (RuntimeException e) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
    }

    /**
     * Write bytes to memory().
     *
     * @param address The address to write to
     * @param data    The data to write
     * @throws WasmException if the memory access is invalid
     */
    void putMemory(int address, byte[] data) throws WasmException {
        try {
            // TODO: do we need a better writeU32 method?
            memory().write(address, data);
        } catch (RuntimeException e) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
    }

    /**
     * Write bytes to memory().
     *
     * @param address The address to write to
     * @param data    The data to write
     * @throws WasmException if the memory access is invalid
     */
    void putMemory(int address, ByteBuffer data) throws WasmException {
        try {
            if (data.hasArray()) {
                var array = data.array();
                memory().write(address, array, data.position(), data.remaining());
            } else {
                // This could likely be optimized by extending the memory interface to accept
                // ByteBuffer
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                memory().write(address, bytes);
            }
        } catch (RuntimeException e) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
    }

    /**
     * Read bytes from memory().
     *
     * @param address The address to read from
     * @param len     The number of bytes to read
     * @return The value read
     * @throws WasmException if the memory access is invalid
     */
    byte[] readMemory(int address, int len) throws WasmException {
        try {
            return memory().readBytes(address, len);
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
            if (value.length != 0) {
                int addr = malloc(value.length);
                putMemory(addr, value);
                putUint32(retPtr, addr);
            } else {
                putUint32(retPtr, 0);
            }
            putUint32(retSize, value.length);
        } catch (WasmException e) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Integration
    // //////////////////////////////////////////////////////////////////////

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#_initialize
     */
    public void initialize() {
        exports().function("_initialize").apply();
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#main
     */
    public int main(int arg0, int arg1) {
        long result = exports().function("main").apply(arg0, arg1)[0];
        return (int) result;
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#_start
     */
    public void start() {
        exports().function("_start").apply();
    }

    // //////////////////////////////////////////////////////////////////////
    // Memory management
    // //////////////////////////////////////////////////////////////////////

    String mallocFunctionName = "malloc";

    public String getMallocFunctionName() {
        return mallocFunctionName;
    }

    public void setMallocFunctionName(String mallocFunctionName) {
        this.mallocFunctionName = mallocFunctionName;
    }

    /**
     * implements:
     *  * https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#malloc
     *  * https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_memory_allocate
     */
    int malloc(int size) throws WasmException {
        // I've noticed guests fail on malloc(0) so lets avoid that
        assert size > 0 : "malloc size must be greater than zero";
        long ptr = exports().function(mallocFunctionName).apply(size)[0];
        if (ptr == 0) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
        return (int) ptr;
    }

    // //////////////////////////////////////////////////////////////////////
    // Context lifecycle
    // //////////////////////////////////////////////////////////////////////

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_context_create
     */
    public void proxyOnContextCreate(int contextID, int parentContextID) {
        exports().function("proxy_on_context_create").apply(contextID, parentContextID);
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_done
     */
    public boolean proxyOnDone(int context_id) {
        long result = exports().function("proxy_on_done").apply(context_id)[0];
        return result != 0;
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_log
     */
    public void proxyOnLog(int context_id) {
        exports().function("proxy_on_log").apply(context_id);
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_delete
     */
    public void proxyOnDelete(int context_id) {
        exports().function("proxy_on_delete").apply(context_id);
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_done
     */
    @WasmExport
    int proxyDone() {
        return handler.done().getValue();
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_set_effective_context
     */
    @WasmExport
    int proxySetEffectiveContext(int contextId) {
        return handler.setEffectiveContextID(contextId).getValue();
    }

    // //////////////////////////////////////////////////////////////////////
    // Configuration
    // //////////////////////////////////////////////////////////////////////

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_vm_start
     */
    public boolean proxyOnVmStart(int arg0, int arg1) {
        long result = exports().function("proxy_on_vm_start").apply(arg0, arg1)[0];
        return result != 0;
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_configure
     */
    public boolean proxyOnConfigure(int arg0, int arg1) {
        long result = exports().function("proxy_on_configure").apply(arg0, arg1)[0];
        return result != 0;
    }

    // //////////////////////////////////////////////////////////////////////
    // Logging
    // //////////////////////////////////////////////////////////////////////

    // wasi_snapshot_preview1.fd_write :
    // https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#wasi_snapshot_preview1fd_write

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_log
     */
    @WasmExport
    public int proxyLog(int logLevel, int messageData, int messageSize) {
        try {
            var msg = memory().readBytes(messageData, messageSize);
            handler.log(LogLevel.fromInt(logLevel), new String(msg));
            return WasmResult.OK.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_get_log_level
     */
    @WasmExport
    public int proxyGetLogLevel(int returnLogLevel) {
        try {
            LogLevel level = handler.getLogLevel();
            putUint32(returnLogLevel, level.value());
            return WasmResult.OK.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Clocks
    // //////////////////////////////////////////////////////////////////////

    // wasi_snapshot_preview1.clock_time_get

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_get_current_time_nanoseconds
     */
    @WasmExport
    int proxyGetCurrentTimeNanoseconds(int returnTime) {
        try {
            int currentTimeNanoseconds = handler.getCurrentTimeNanoseconds();
            putUint32(returnTime, currentTimeNanoseconds);
            return WasmResult.OK.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Timers
    // //////////////////////////////////////////////////////////////////////

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_set_tick_period_milliseconds
     */
    @WasmExport
    int proxySetTickPeriodMilliseconds(int tick_period) {
        return handler.setTickPeriodMilliseconds(tick_period).getValue();
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_tick
     */
    public void proxyOnTick(int arg0) {
        exports().function("proxy_on_tick").apply(arg0);
    }

    // //////////////////////////////////////////////////////////////////////
    // Randomness
    // //////////////////////////////////////////////////////////////////////

    // wasi_snapshot_preview1.random_get

    // //////////////////////////////////////////////////////////////////////
    // Environment variables
    // //////////////////////////////////////////////////////////////////////

    // wasi_snapshot_preview1.environ_sizes_get
    // wasi_snapshot_preview1.environ_get

    // //////////////////////////////////////////////////////////////////////
    // Buffers
    // //////////////////////////////////////////////////////////////////////

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_get_buffer_bytes
     *
     * @param bufferType       The type of buffer to get
     * @param start            The start index in the buffer
     * @param length           The number of bytes to get
     * @param returnBufferData Pointer to where the buffer data address should be stored
     * @param returnBufferSize Pointer to where the buffer size should be stored
     * @return WasmResult status code
     */
    @WasmExport
    public int proxyGetBufferBytes(
            int bufferType, int start, int length, int returnBufferData, int returnBufferSize) {

        try {
            // Get the buffer based on the buffer type
            byte[] b = getBuffer(bufferType);
            if (b == null || b.length == 0) {
                return WasmResult.NOT_FOUND.getValue();
            }

            if (start > start + length) {
                return WasmResult.BAD_ARGUMENT.getValue();
            }

            ByteBuffer buffer = ByteBuffer.wrap(b);
            if (start + length > buffer.capacity()) {
                length = buffer.capacity() - start;
            }

            try {
                buffer.position(start);
                buffer.limit(start + length);
            } catch (IllegalArgumentException e) {
                return WasmResult.BAD_ARGUMENT.getValue();
            }

            // Allocate memory in the WebAssembly instance
            int addr = malloc(length);
            putMemory(addr, buffer);
            // Write the address to the return pointer
            putUint32(returnBufferData, addr);
            // Write the length to the return size pointer
            putUint32(returnBufferSize, length);
            return WasmResult.OK.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_set_buffer_bytes
     *
     * @param bufferType The type of buffer to modify
     * @param start      The start index in the buffer
     * @param length     The number of bytes to set
     * @param dataPtr    Pointer to the data in WebAssembly memory
     * @param dataSize   Size of the data
     * @return WasmResult status code
     */
    @WasmExport
    public int proxySetBufferBytes(
            int bufferType, int start, int length, int dataPtr, int dataSize) {
        try {

            // Get the buffer based on the buffer type
            var buf = getBuffer(bufferType);
            if (buf == null) {
                return WasmResult.NOT_FOUND.getValue();
            }

            // Get content from WebAssembly memory
            byte[] content = memory().readBytes(dataPtr, dataSize);

            content = replaceBytes(buf, content, start, length);

            // Set the buffer using the appropriate handler method
            WasmResult result = setBuffer(bufferType, content);
            return result.getValue();

        } catch (WasmRuntimeException e) {
            return WasmResult.INVALID_MEMORY_ACCESS.getValue();
        }
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_get_buffer_status
     */
    @WasmExport
    public int proxyGetBufferStatus(int bufferType, int returnBufferSize, int returnUnused) {
        try {
            // Get the buffer based on the buffer type
            byte[] b = getBuffer(bufferType);
            if (b == null) {
                return WasmResult.NOT_FOUND.getValue();
            }

            // Write the buffer size to the return size pointer
            putUint32(returnBufferSize, b.length);
            return WasmResult.OK.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * Get a buffer based on the buffer type.
     *
     * @param bufferType The type of buffer to get
     * @return The buffer, or null if not found
     */
    private byte[] getBuffer(int bufferType) {
        // Return the appropriate buffer based on the buffer type
        var knownType = BufferType.fromInt(bufferType);
        if (knownType == null) {
            return handler.getCustomBuffer(bufferType);
        }
        switch (knownType) {
            case HTTP_REQUEST_BODY:
                return handler.getHttpRequestBody();
            case HTTP_RESPONSE_BODY:
                return handler.getHttpResponseBody();
            case DOWNSTREAM_DATA:
                return handler.getDownStreamData();
            case UPSTREAM_DATA:
                return handler.getUpstreamData();
            case HTTP_CALL_RESPONSE_BODY:
                return handler.getHttpCallResponseBody();
            case GRPC_RECEIVE_BUFFER:
                return handler.getGrpcReceiveBuffer();
            case PLUGIN_CONFIGURATION:
                return handler.getPluginConfig();
            case VM_CONFIGURATION:
                return handler.getVmConfig();
            case CALL_DATA:
                return handler.getFuncCallData();
        }
        return null;
    }

    /**
     * Set a buffer based on the buffer type.
     *
     * @param bufferType The type of buffer to set
     * @param buffer     The buffer to set
     * @return WasmResult indicating success or failure
     */
    private WasmResult setBuffer(int bufferType, byte[] buffer) {
        // Set the appropriate buffer based on the buffer type
        var knownType = BufferType.fromInt(bufferType);
        if (knownType == null) {
            return handler.setCustomBuffer(bufferType, buffer);
        }

        // TODO: check if the buffers that can be set may depend on the ABI version.
        switch (knownType) {
            case HTTP_REQUEST_BODY:
                return handler.setHttpRequestBody(buffer);
            case HTTP_RESPONSE_BODY:
                return handler.setHttpResponseBody(buffer);
            case DOWNSTREAM_DATA:
                return handler.setDownStreamData(buffer);
            case UPSTREAM_DATA:
                return handler.setUpstreamData(buffer);
            case HTTP_CALL_RESPONSE_BODY:
                return handler.setHttpCallResponseBody(buffer);
            case GRPC_RECEIVE_BUFFER:
                return handler.setGrpcReceiveBuffer(buffer);
            case CALL_DATA:
                return handler.setFuncCallData(buffer);
        }
        return WasmResult.NOT_FOUND;
    }

    // //////////////////////////////////////////////////////////////////////
    // HTTP fields
    // //////////////////////////////////////////////////////////////////////

    /**
     * Retrieves serialized size of all key-value pairs from the map mapType
     *
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_get_header_map_size
     *
     * @param mapType    The type of map to set
     * @param returnSize Pointer to ruturn the size of the map data
     * @return WasmResult status code
     */
    @WasmExport
    public int proxyGetHeaderMapSize(int mapType, int returnSize) {
        try {

            // Get the header map based on the map type
            Map<String, String> header = getMap(mapType);
            if (header == null) {
                return WasmResult.BAD_ARGUMENT.getValue();
            }

            // to clone the headers so that they don't change on while we process them in the loop
            final Map<String, String> cloneMap = new HashMap<>();
            int totalBytesLen = U32_LEN; // Start with space for the count

            for (Map.Entry<String, String> entry : header.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                cloneMap.put(key, value);
                totalBytesLen += U32_LEN + U32_LEN; // keyLen + valueLen
                totalBytesLen += key.length() + 1 + value.length() + 1; // key + \0 + value + \0
            }

            // Write the total size to the return size pointer
            putUint32(returnSize, totalBytesLen);

            return WasmResult.OK.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * Get header map pairs and format them for WebAssembly memory().
     *
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_get_header_map_pairs
     *
     * @param mapType        The type of map to get
     * @param returnDataPtr  Pointer to where the data address should be stored
     * @param returnDataSize Pointer to where the data size should be stored
     * @return WasmResult status code
     */
    @WasmExport
    public int proxyGetHeaderMapPairs(int mapType, int returnDataPtr, int returnDataSize) {
        try {

            // Get the header map based on the map type
            Map<String, String> header = getMap(mapType);
            if (header == null) {
                return WasmResult.NOT_FOUND.getValue();
            }

            // to clone the headers so that they don't change on while we process them in the loop
            final Map<String, String> cloneMap = new HashMap<>();
            int totalBytesLen = U32_LEN; // Start with space for the count

            for (Map.Entry<String, String> entry : header.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                cloneMap.put(key, value);
                totalBytesLen += U32_LEN + U32_LEN; // keyLen + valueLen
                totalBytesLen += key.length() + 1 + value.length() + 1; // key + \0 + value + \0
            }

            // Allocate memory in the WebAssembly instance
            int addr = malloc(totalBytesLen);

            // Write the number of entries to the allocated memory
            putUint32(addr, cloneMap.size());

            // Calculate pointers for lengths and data
            int lenPtr = addr + U32_LEN;
            int dataPtr = lenPtr + ((U32_LEN + U32_LEN) * cloneMap.size());

            // Write each key-value pair to memory
            for (Map.Entry<String, String> entry : cloneMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // Write key length
                putUint32(lenPtr, key.length());
                lenPtr += U32_LEN;

                // Write value length
                putUint32(lenPtr, value.length());
                lenPtr += U32_LEN;

                // Write key bytes
                putMemory(dataPtr, key.getBytes());
                dataPtr += key.length();

                // Write null terminator for key
                putByte(dataPtr, (byte) 0);
                dataPtr++;

                // Write value bytes
                putMemory(dataPtr, value.getBytes());
                dataPtr += value.length();

                // Write null terminator for value
                putByte(dataPtr, (byte) 0);
                dataPtr++;
            }

            // Write the address to the return pointer
            putUint32(returnDataPtr, addr);

            // Write the total size to the return size pointer
            putUint32(returnDataSize, totalBytesLen);

            return WasmResult.OK.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * Set header map pairs from WebAssembly memory().
     *
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_set_header_map_pairs
     *
     * @param mapType The type of map to set
     * @param ptr     Pointer to the map data in WebAssembly memory
     * @param size    Size of the map data
     * @return WasmResult status code
     */
    @WasmExport
    public int proxySetHeaderMapPairs(int mapType, int ptr, int size) {

        try {
            // Get the header map based on the map type
            Map<String, String> headerMap = getMap(mapType);
            if (headerMap == null) {
                return WasmResult.BAD_ARGUMENT.getValue();
            }

            // Decode the map content and set each key-value pair
            Map<String, String> newMap = decodeMap(ptr, size);
            for (Map.Entry<String, String> entry : newMap.entrySet()) {
                headerMap.put(entry.getKey(), entry.getValue());
            }

            return WasmResult.OK.getValue();
        } catch (WasmRuntimeException e) {
            return WasmResult.INVALID_MEMORY_ACCESS.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * Get a value from a header map by key.
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_get_header_map_value
     *
     * @param mapType      The type of map to get from
     * @param keyDataPtr   Pointer to the key data in WebAssembly memory
     * @param keySize      Size of the key data
     * @param valueDataPtr Pointer to where the value data should be stored
     * @param valueSize    Size of the value data buffer
     * @return WasmResult status code
     */
    @WasmExport
    public int proxyGetHeaderMapValue(
            int mapType, int keyDataPtr, int keySize, int valueDataPtr, int valueSize) {
        try {
            // Get the header map based on the map type
            Map<String, String> headerMap = getMap(mapType);
            if (headerMap == null) {
                return WasmResult.BAD_ARGUMENT.getValue();
            }

            // Get key from memory
            String key = readString(keyDataPtr, keySize);

            // Get value from map
            String value = headerMap.get(key);
            if (value == null) {
                return WasmResult.NOT_FOUND.getValue();
            }

            // Copy value into WebAssembly memory
            copyIntoInstance(value, valueDataPtr, valueSize);
            return WasmResult.OK.getValue();

        } catch (WasmRuntimeException e) {
            return WasmResult.INVALID_MEMORY_ACCESS.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * Add a value to a header map.
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_add_header_map_value
     *
     * @param mapType      The type of map to modify
     * @param keyDataPtr   Pointer to the key data in WebAssembly memory
     * @param keySize      Size of the key data
     * @param valueDataPtr Pointer to the value data in WebAssembly memory
     * @param valueSize    Size of the value data
     * @return WasmResult status code
     */
    @WasmExport
    public int proxyAddHeaderMapValue(
            int mapType, int keyDataPtr, int keySize, int valueDataPtr, int valueSize) {
        return proxyReplaceHeaderMapValue(mapType, keyDataPtr, keySize, valueDataPtr, valueSize);
    }

    /**
     * Replace a value in a header map.
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_replace_header_map_value
     *
     * @param mapType      The type of map to modify
     * @param keyDataPtr   Pointer to the key data in WebAssembly memory
     * @param keySize      Size of the key data
     * @param valueDataPtr Pointer to the value data in WebAssembly memory
     * @param valueSize    Size of the value data
     * @return WasmResult status code
     */
    @WasmExport
    public int proxyReplaceHeaderMapValue(
            int mapType, int keyDataPtr, int keySize, int valueDataPtr, int valueSize) {
        try {
            // Get the header map based on the map type
            Map<String, String> headerMap = getMap(mapType);
            if (headerMap == null) {
                return WasmResult.BAD_ARGUMENT.getValue();
            }

            // Get key from memory
            String key = readString(keyDataPtr, keySize);

            // Get value from memory
            String value = readString(valueDataPtr, valueSize);

            // Replace value in map
            var copy = new HashMap<>(headerMap);
            copy.put(key, value);
            setMap(mapType, copy);

            return WasmResult.OK.getValue();

        } catch (WasmRuntimeException e) {
            return WasmResult.INVALID_MEMORY_ACCESS.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * Remove a value from a header map.
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_remove_header_map_value
     *
     * @param mapType    The type of map to modify
     * @param keyDataPtr Pointer to the key data in WebAssembly memory
     * @param keySize    Size of the key data
     * @return WasmResult status code
     */
    @WasmExport
    public int proxyRemoveHeaderMapValue(int mapType, int keyDataPtr, int keySize) {
        try {
            // Get the header map based on the map type
            Map<String, String> headerMap = getMap(mapType);
            if (headerMap == null) {
                return WasmResult.NOT_FOUND.getValue();
            }

            // Get key from memory
            String key = readString(keyDataPtr, keySize);
            if (key.isEmpty()) {
                return WasmResult.BAD_ARGUMENT.getValue();
            }

            // Remove key from map
            var copy = new HashMap<>(headerMap);
            copy.remove(key);
            setMap(mapType, copy);

            return WasmResult.OK.getValue();

        } catch (WasmRuntimeException e) {
            return WasmResult.INVALID_MEMORY_ACCESS.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * Get a header map based on the map type.
     *
     * @param mapType  The type of map to get
     * @return The header map
     */
    private Map<String, String> getMap(int mapType) {

        var knownType = MapType.fromInt(mapType);
        if (knownType == null) {
            return handler.getCustomHeaders(mapType);
        }

        switch (knownType) {
            case HTTP_REQUEST_HEADERS:
                return handler.getHttpRequestHeaders();
            case HTTP_REQUEST_TRAILERS:
                return handler.getHttpRequestTrailers();
            case HTTP_RESPONSE_HEADERS:
                return handler.getHttpResponseHeaders();
            case HTTP_RESPONSE_TRAILERS:
                return handler.getHttpResponseTrailers();
            case HTTP_CALL_RESPONSE_HEADERS:
                return handler.getHttpCallResponseHeaders();
            case HTTP_CALL_RESPONSE_TRAILERS:
                return handler.getHttpCallResponseTrailers();
            case GRPC_RECEIVE_INITIAL_METADATA:
                return handler.getGrpcReceiveInitialMetaData();
            case GRPC_RECEIVE_TRAILING_METADATA:
                return handler.getGrpcReceiveTrailerMetaData();
        }
        return null;
    }

    /**
     * Set a header map based on the map type.
     *
     * @param mapType  The type of map to set
     * @param map      The header map to set
     * @return WasmResult indicating success or failure
     */
    private WasmResult setMap(int mapType, Map<String, String> map) {
        var knownType = MapType.fromInt(mapType);
        if (knownType == null) {
            return handler.setCustomHeaders(mapType, map);
        }

        switch (knownType) {
            case HTTP_REQUEST_HEADERS:
                return handler.setHttpRequestHeaders(map);
            case HTTP_REQUEST_TRAILERS:
                return handler.setHttpRequestTrailers(map);
            case HTTP_RESPONSE_HEADERS:
                return handler.setHttpResponseHeaders(map);
            case HTTP_RESPONSE_TRAILERS:
                return handler.setHttpResponseTrailers(map);
            case HTTP_CALL_RESPONSE_HEADERS:
                return handler.setHttpCallResponseHeaders(map);
            case HTTP_CALL_RESPONSE_TRAILERS:
                return handler.setHttpCallResponseTrailers(map);
            case GRPC_RECEIVE_INITIAL_METADATA:
                return handler.setGrpcReceiveInitialMetaData(map);
            case GRPC_RECEIVE_TRAILING_METADATA:
                return handler.setGrpcReceiveTrailerMetaData(map);
        }
        return WasmResult.NOT_FOUND;
    }

    /**
     * Decodes a byte array containing map data into a Map of String key-value pairs.
     * <p>
     * The format is:
     * - First 4 bytes: number of entries (headerSize) in little endian
     * - For each entry:
     * - 4 bytes: key size in little endian
     * - 4 bytes: value size in little endian
     * - Then the actual data:
     * - key bytes (null terminated)
     * - value bytes (null terminated)
     *
     * @param addr     The memory address to read from
     * @param mem_size The size of memory to read
     * @return The decoded map containing string keys and values
     * @throws WasmException if there is an error accessing memory
     */
    private HashMap<String, String> decodeMap(int addr, int mem_size) throws WasmException {
        if (mem_size < U32_LEN) {
            return new HashMap<>();
        }

        // Read header size (number of entries)
        var mapSize = getUint32(addr);

        // Calculate start of data section
        // mapSize + (key1_size + value1_size) * mapSize
        long dataOffset = U32_LEN + (U32_LEN + U32_LEN) * mapSize;
        if (dataOffset >= mem_size) {
            return new HashMap<>();
        }

        // Create result map with initial capacity
        var result = new HashMap<String, String>((int) mapSize);

        // Process each entry
        for (int i = 0; i < mapSize; i++) {

            // Calculate index for length values
            int keySizeOffset = U32_LEN + (U32_LEN + U32_LEN) * i;
            int valueSizeOffset = keySizeOffset + U32_LEN;

            // Read key and value sizes
            long keySize = getUint32(addr + keySizeOffset);
            long valueSize = getUint32(addr + valueSizeOffset);

            // Check if we have enough data for the key/value
            if (dataOffset >= mem_size || dataOffset + keySize + valueSize + 2 > mem_size) {
                break;
            }

            // Extract key
            String key = readString((int) (addr + dataOffset), (int) keySize);
            dataOffset += keySize + 1; // Skip null terminator

            // Extract value
            String value = readString((int) (addr + dataOffset), (int) valueSize);
            dataOffset += valueSize + 1;

            // Add to result map
            result.put(key, value);
        }

        return result;
    }

    // //////////////////////////////////////////////////////////////////////
    // Common HTTP and TCP stream operations
    // //////////////////////////////////////////////////////////////////////

    /**
     * Resumes processing of paused stream_type.
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/v0.2.0#proxy_continue_stream
     */
    @WasmExport
    int proxyContinueStream(int arg) {
        var streamType = StreamType.fromInt(arg);
        if (streamType == null) {
            return WasmResult.BAD_ARGUMENT.getValue();
        }
        WasmResult result = handler.setAction(streamType, Action.CONTINUE);
        return result.getValue();
    }

    // TODO: implement
    // https://github.com/proxy-wasm/spec/tree/main/abi-versions/v0.2.1#proxy_close_stream
    // TODO: implement
    // https://github.com/proxy-wasm/spec/tree/main/abi-versions/v0.2.1#proxy_get_status

    // //////////////////////////////////////////////////////////////////////
    // TCP streams
    // //////////////////////////////////////////////////////////////////////

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_new_connection
     */
    public int proxyOnNewConnection(int arg0) {
        long result = exports().function("proxy_on_new_connection").apply(arg0)[0];
        return (int) result;
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_downstream_data
     */
    public int proxyOnDownstreamData(int contextId, int dataSize, int endOfStream) {
        long result =
                exports()
                        .function("proxy_on_downstream_data")
                        .apply(contextId, dataSize, endOfStream)[0];
        return (int) result;
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_downstream_connection_close
     */
    public void proxyOnDownstreamConnectionClose(int arg0, int arg1) {
        exports().function("proxy_on_downstream_connection_close").apply(arg0, arg1);
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_upstream_data
     */
    public int proxyOnUpstreamData(int arg0, int arg1, int arg2) {
        long result = exports().function("proxy_on_upstream_data").apply(arg0, arg1, arg2)[0];
        return (int) result;
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_upstream_connection_close
     */
    public void proxyOnUpstreamConnectionClose(int arg0, int arg1) {
        exports().function("proxy_on_upstream_connection_close").apply(arg0, arg1);
    }

    // //////////////////////////////////////////////////////////////////////
    // HTTP streams
    // //////////////////////////////////////////////////////////////////////

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_request_headers
     */
    public int proxyOnRequestHeaders(int contextID, int headers, int endOfStream) {
        long result =
                exports()
                        .function("proxy_on_request_headers")
                        .apply(contextID, headers, endOfStream)[0];
        return (int) result;
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_request_body
     */
    public int proxyOnRequestBody(int contextId, int bodySize, int arg2) {
        long result =
                exports().function("proxy_on_request_body").apply(contextId, bodySize, arg2)[0];
        return (int) result;
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_request_trailers
     */
    public int proxyOnRequestTrailers(int arg0, int arg1) {
        long result = exports().function("proxy_on_request_trailers").apply(arg0, arg1)[0];
        return (int) result;
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_response_headers
     */
    public int proxyOnResponseHeaders(int arg0, int arg1, int arg2) {
        long result = exports().function("proxy_on_response_headers").apply(arg0, arg1, arg2)[0];
        return (int) result;
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_response_body
     */
    public int proxyOnResponseBody(int arg0, int arg1, int arg2) {
        long result = exports().function("proxy_on_response_body").apply(arg0, arg1, arg2)[0];
        return (int) result;
    }

    /**
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_response_trailers
     */
    public int proxyOnResponseTrailers(int arg0, int arg1) {
        long result = exports().function("proxy_on_response_trailers").apply(arg0, arg1)[0];
        return (int) result;
    }

    /**
     * Send an HTTP response.
     * implements: https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_send_local_response
     *
     * @param responseCode             The HTTP response code
     * @param responseCodeDetailsData  Pointer to response code details in WebAssembly memory
     * @param responseCodeDetailsSize  Size of response code details
     * @param responseBodyData         Pointer to response body in WebAssembly memory
     * @param responseBodySize         Size of response body
     * @param additionalHeadersMapData Pointer to additional headers map in WebAssembly memory
     * @param additionalHeadersSize    Size of additional headers map
     * @param grpcStatus               The gRPC status code (-1 for non-gRPC responses)
     * @return WasmResult status code
     */
    @WasmExport
    public int proxySendLocalResponse(
            int responseCode,
            int responseCodeDetailsData,
            int responseCodeDetailsSize,
            int responseBodyData,
            int responseBodySize,
            int additionalHeadersMapData,
            int additionalHeadersSize,
            int grpcStatus) {
        try {

            // Get response code details from memory
            byte[] responseCodeDetails = null;
            if (responseCodeDetailsSize > 0) {
                responseCodeDetails =
                        memory().readBytes(responseCodeDetailsData, responseCodeDetailsSize);
            }

            // Get response body from memory
            byte[] responseBody = new byte[0];
            if (responseBodySize > 0) {
                responseBody = memory().readBytes(responseBodyData, responseBodySize);
            }

            // Get and decode additional headers from memory
            HashMap<String, String> additionalHeaders =
                    decodeMap(additionalHeadersMapData, additionalHeadersSize);

            // Send the response through the handler
            WasmResult result =
                    handler.sendHttpResponse(
                            responseCode,
                            responseCodeDetails,
                            responseBody,
                            additionalHeaders,
                            grpcStatus);
            return result.getValue();

        } catch (WasmRuntimeException e) {
            return WasmResult.INVALID_MEMORY_ACCESS.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * Resumes processing of paused HTTP request.
     * <p>
     * see: https://github.com/proxy-wasm/spec/blob/main/abi-versions/v0.1.0/README.md#proxy_continue_request
     *
     * @deprecated in 0.2.0
     */
    @WasmExport
    @Deprecated(since = "0.2.0")
    void proxyContinueRequest() {
        handler.setAction(StreamType.REQUEST, Action.CONTINUE);
    }

    /**
     * Resumes processing of paused HTTP response.
     * <p>
     * see: https://github.com/proxy-wasm/spec/blob/main/abi-versions/v0.1.0/README.md#proxy_continue_response
     *
     * @deprecated in 0.2.0
     */
    @WasmExport
    @Deprecated(since = "0.2.0")
    void proxyContinueResponse() {
        handler.setAction(StreamType.RESPONSE, Action.CONTINUE);
    }

    /**
     * Clears cached HTTP route.
     * <p>
     * see: https://github.com/proxy-wasm/spec/blob/main/abi-versions/v0.1.0/README.md#proxy_clear_route_cache
     *
     * @deprecated in 0.2.0
     */
    @WasmExport
    @Deprecated(since = "0.2.0")
    void proxyClearRouteCache() {
        handler.clearRouteCache();
    }

    // //////////////////////////////////////////////////////////////////////
    // HTTP calls
    // //////////////////////////////////////////////////////////////////////

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_http_call
     */
    @WasmExport
    int proxyHttpCall(
            int uriData,
            int uriSize,
            int headersData,
            int headersSize,
            int bodyData,
            int bodySize,
            int trailersData,
            int trailersSize,
            int timeout,
            int returnCalloutID) {

        try {
            var uri = string(readMemory(uriData, uriSize));
            var headers = decodeMap(headersData, headersSize);
            var body = readMemory(bodyData, bodySize);
            var trailers = decodeMap(trailersData, trailersSize);

            int calloutId = handler.httpCall(uri, headers, body, trailers, timeout);

            putUint32(returnCalloutID, calloutId);
            return WasmResult.OK.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * implements https://github.com/api7/wasm-nginx-module/blob/main/proxy_wasm_abi.md#proxy_dispatch_http_call
     */
    @WasmExport
    int proxyDispatchHttpCall(
            int upstreamNameData,
            int upstreamNameSize,
            int headersData,
            int headersSize,
            int bodyData,
            int bodySize,
            int trailersData,
            int trailersSize,
            int timeoutMilliseconds,
            int returnCalloutID) {

        try {
            var upstreamName = string(readMemory(upstreamNameData, upstreamNameSize));
            var headers = decodeMap(headersData, headersSize);
            var body = readMemory(bodyData, bodySize);
            var trailers = decodeMap(trailersData, trailersSize);

            int calloutId =
                    handler.dispatchHttpCall(
                            upstreamName, headers, body, trailers, timeoutMilliseconds);

            putUint32(returnCalloutID, calloutId);
            return WasmResult.OK.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_http_call_response
     */
    public void proxyOnHttpCallResponse(int arg0, int arg1, int arg2, int arg3, int arg4) {
        exports().function("proxy_on_http_call_response").apply(arg0, arg1, arg2, arg3, arg4);
    }

    // //////////////////////////////////////////////////////////////////////
    // gRPC calls
    // //////////////////////////////////////////////////////////////////////

    // TODO: implement
    // https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_grpc_call
    // TODO: implement
    // https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_grpc_stream
    // TODO: implement
    // https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_grpc_send
    // TODO: implement
    // https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_grpc_cancel
    // TODO: implement
    // https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_grpc_close

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_grpc_receive_initial_metadata
     */
    public void proxyOnGrpcReceiveInitialMetadata(int contextId, int callId, int numElements) {
        exports()
                .function("proxy_on_grpc_receive_initial_metadata")
                .apply(contextId, callId, numElements);
    }

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_grpc_receive
     */
    public void proxyOnGrpcReceive(int contextId, int callId, int messageSize) {
        exports().function("proxy_on_grpc_receive").apply(contextId, callId, messageSize);
    }

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_grpc_receive_trailing_metadata
     */
    public void proxyOnGrpcReceiveTrailingMetadata(int arg0, int arg1, int arg2) {
        exports().function("proxy_on_grpc_receive_trailing_metadata").apply(arg0, arg1, arg2);
    }

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_grpc_close
     */
    public void proxyOnGrpcClose(int arg0, int arg1, int arg2) {
        exports().function("proxy_on_grpc_close").apply(arg0, arg1, arg2);
    }

    // //////////////////////////////////////////////////////////////////////
    // Shared Key-Value Store
    // //////////////////////////////////////////////////////////////////////

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_set_shared_data
     */
    @WasmExport
    int proxySetSharedData(int keyDataPtr, int keySize, int valueDataPtr, int valueSize, int cas) {
        try {
            // Get key from memory
            String key = string(readMemory(keyDataPtr, keySize));

            // Get value from memory
            byte[] value = readMemory(valueDataPtr, valueSize);

            // Set shared data value using handler
            WasmResult result = handler.setSharedData(key, value, cas);
            return result.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_get_shared_data
     */
    @WasmExport
    int proxyGetSharedData(
            int keyDataPtr, int keySize, int returnValueData, int returnValueSize, int returnCas) {
        try {
            // Get key from memory
            String key = string(readMemory(keyDataPtr, keySize));

            // Get shared data value using handler
            Handler.SharedData value = handler.getSharedData(key);
            if (value == null) {
                return WasmResult.NOT_FOUND.getValue();
            }

            copyIntoInstance(value.data, returnValueData, returnValueSize);
            putUint32(returnCas, value.cas);
            return WasmResult.OK.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Shared Queues
    // //////////////////////////////////////////////////////////////////////

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_register_shared_queue
     */
    @WasmExport
    int proxyRegisterSharedQueue(int queueNameDataPtr, int queueNameSize, int returnQueueId) {
        try {
            // Get queue name from memory
            String queueName = string(readMemory(queueNameDataPtr, queueNameSize));

            var vmId = handler.getProperty("vm_id");

            // Register shared queue using handler
            int queueId = handler.registerSharedQueue(new QueueName(vmId, queueName));
            putUint32(returnQueueId, queueId);
            return WasmResult.OK.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_resolve_shared_queue
     */
    @WasmExport
    int proxyResolveSharedQueue(
            int vmIdDataPtr,
            int vmIdSize,
            int queueNameDataPtr,
            int queueNameSize,
            int returnQueueId) {
        try {
            // Get vm id from memory
            String vmId = string(readMemory(vmIdDataPtr, vmIdSize));
            // Get queue name from memory
            String queueName = string(readMemory(queueNameDataPtr, queueNameSize));

            // Resolve shared queue using handler
            int queueId = handler.resolveSharedQueue(new QueueName(vmId, queueName));
            putUint32(returnQueueId, queueId);
            return WasmResult.OK.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_enqueue_shared_queue
     */
    @WasmExport
    int proxyEnqueueSharedQueue(int queueId, int valueDataPtr, int valueSize) {
        try {
            // Get value from memory
            byte[] value = readMemory(valueDataPtr, valueSize);

            // Enqueue shared queue using handler
            WasmResult result = handler.enqueueSharedQueue(queueId, value);
            return result.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_dequeue_shared_queue
     */
    @WasmExport
    int proxyDequeueSharedQueue(int queueId, int returnValueData, int returnValueSize) {
        try {
            // Dequeue shared queue using handler
            byte[] value = handler.dequeueSharedQueue(queueId);
            if (value == null) {
                return WasmResult.EMPTY.getValue();
            }

            copyIntoInstance(value, returnValueData, returnValueSize);
            return WasmResult.OK.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_on_queue_ready
     */
    public void proxyOnQueueReady(int arg0, int arg1) {
        exports().function("proxy_on_queue_ready").apply(arg0, arg1);
    }

    // //////////////////////////////////////////////////////////////////////
    // Metrics
    // //////////////////////////////////////////////////////////////////////

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_define_metric
     */
    @WasmExport
    int proxyDefineMetric(int metricType, int nameDataPtr, int nameSize, int returnMetricId) {
        try {
            MetricType type = MetricType.fromInt(metricType);
            if (type == null) {
                return WasmResult.BAD_ARGUMENT.getValue();
            }

            var name = string(readMemory(nameDataPtr, nameSize));
            int metricId = handler.defineMetric(type, name);
            putUint32(returnMetricId, metricId);
            return WasmResult.OK.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_record_metric
     */
    @WasmExport
    int proxyRecordMetric(int metricId, long value) {
        WasmResult result = handler.recordMetric(metricId, value);
        return result.getValue();
    }

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_increment_metric
     */
    @WasmExport
    int proxyIncrementMetric(int metricId, long value) {
        WasmResult result = handler.incrementMetric(metricId, value);
        return result.getValue();
    }

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_get_metric
     */
    @WasmExport
    int proxyGetMetric(int metricId, int returnValuePtr) {
        try {
            var result = handler.getMetric(metricId);
            putUint32(returnValuePtr, (int) result);
            return WasmResult.OK.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * Remove a defined metric.
     * <p>
     * Not defined in the spec, but seems useful to have.
     */
    @WasmExport
    int proxyRemoveMetric(int metricId) {
        WasmResult result = handler.removeMetric(metricId);
        return result.getValue();
    }

    // //////////////////////////////////////////////////////////////////////
    // Properties
    // //////////////////////////////////////////////////////////////////////

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_get_property
     */
    @WasmExport
    public int proxyGetProperty(int keyPtr, int keySize, int returnValueData, int returnValueSize) {
        try {
            // Get key from memory
            byte[] keyBytes = readMemory(keyPtr, keySize);
            if (keyBytes.length == 0) {
                return WasmResult.BAD_ARGUMENT.getValue();
            }

            String key = new String(keyBytes);

            // Get property value using handler
            String value = handler.getProperty(key);
            if (value == null) {
                return WasmResult.NOT_FOUND.getValue();
            }

            copyIntoInstance(value, returnValueData, returnValueSize);
            return WasmResult.OK.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    // TODO: implement
    // https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_set_property

    // //////////////////////////////////////////////////////////////////////
    // Foreign function interface (FFI)
    // //////////////////////////////////////////////////////////////////////

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_call_foreign_function
     */
    @WasmExport
    int proxyCallForeignFunction(
            int nameDataPtr,
            int nameSize,
            int argumentDataPtr,
            int argumentSize,
            int returnResultsPtr,
            int returnResultsSizePtr) {
        try {
            var name = string(readMemory(nameDataPtr, nameSize));
            var argument = readMemory(argumentDataPtr, argumentSize);
            var result = handler.callForeignFunction(name, argument);

            // Allocate memory in the WebAssembly instance
            int addr = malloc(result.length);
            putMemory(addr, result);
            // Write the address to the return pointer
            putUint32(returnResultsPtr, addr);
            // Write the length to the return size pointer
            putUint32(returnResultsSizePtr, result.length);
            return WasmResult.OK.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * implements https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#proxy_call_foreign_function
     */
    public void proxyOnForeignFunction(int contextId, int functionId, int argumentsSize) {
        exports().function("proxy_on_foreign_function").apply(contextId, functionId, argumentsSize);
    }

    // //////////////////////////////////////////////////////////////////////
    // Unimplemented WASI functions
    // //////////////////////////////////////////////////////////////////////

    // wasi_snapshot_preview1.args_sizes_get :
    // https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#wasi_snapshot_preview1args_sizes_get
    // wasi_snapshot_preview1.args_get :
    // https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#wasi_snapshot_preview1args_get
    // wasi_snapshot_preview1.proc_exit :
    // https://github.com/proxy-wasm/spec/tree/main/abi-versions/vNEXT#wasi_snapshot_preview1proc_exit

}
