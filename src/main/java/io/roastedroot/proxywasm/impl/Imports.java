package io.roastedroot.proxywasm.impl;

import com.dylibso.chicory.experimental.hostmodule.annotations.HostModule;
import com.dylibso.chicory.experimental.hostmodule.annotations.WasmExport;
import com.dylibso.chicory.runtime.Instance;
import io.roastedroot.proxywasm.v1.*;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@HostModule("env")
public class Imports extends Common {

    private Handler handler;
    Exports exports;

    public Exports getExports() {
        return exports;
    }

    public void setExports(Exports exports) {
        this.exports = exports;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    int malloc(int length) throws WasmException {
        return exports.malloc(length);
    }

    @WasmExport
    public int proxyLog(int logLevel, int messageData, int messageSize) {
        try {
            var msg = instance.memory().readBytes(messageData, messageSize);
            handler.log(LogLevel.fromInt(logLevel), new String(msg));
            return WasmResult.OK.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * Get a header map based on the map type.
     *
     * @param instance The WebAssembly instance
     * @param mapType  The type of map to get
     * @return The header map
     */
    public Map<String, String> getMap(Instance instance, int mapType) {

        var knownType = MapType.fromInt(mapType);
        if (knownType == null) {
            return handler.getCustomHeader(mapType);
        }

        switch (knownType) {
            case HTTP_REQUEST_HEADERS:
                return handler.getHttpRequestHeader();
            case HTTP_REQUEST_TRAILERS:
                return handler.getHttpRequestTrailer();
            case HTTP_RESPONSE_HEADERS:
                return handler.getHttpResponseHeader();
            case HTTP_RESPONSE_TRAILERS:
                return handler.getHttpResponseTrailer();
            case HTTP_CALL_RESPONSE_HEADERS:
                return handler.getHttpCallResponseHeaders();
            case HTTP_CALL_RESPONSE_TRAILERS:
                return handler.getHttpCallResponseTrailer();
            case GRPC_RECEIVE_INITIAL_METADATA:
                return handler.getGrpcReceiveInitialMetaData();
            case GRPC_RECEIVE_TRAILING_METADATA:
                return handler.getGrpcReceiveTrailerMetaData();
        }
        return null;
    }

    /**
     * Get a buffer based on the buffer type.
     *
     * @param instance   The WebAssembly instance
     * @param bufferType The type of buffer to get
     * @return The buffer, or null if not found
     */
    private ByteBuffer getBuffer(Instance instance, int bufferType) {
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
     * Get header map pairs and format them for WebAssembly memory.
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
            Map<String, String> header = getMap(instance, mapType);
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
                totalBytesLen += U32_LEN + U32_LEN;                     // keyLen + valueLen
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


    @WasmExport
    public int proxyGetProperty(int keyPtr, int keySize, int returnValueData, int returnValueSize) {
        try {
            // Get key from memory
            byte[] keyBytes = getMemory(keyPtr, keySize);
            if (keyBytes.length == 0) {
                return WasmResult.BAD_ARGUMENT.getValue();
            }

            String key = new String(keyBytes);

            // Get property value using handler
            String value = handler.getProperty(key);
            if (value == null) {
                value = "";
            }

            copyIntoInstance(value, returnValueSize, returnValueSize);
            return WasmResult.OK.getValue();

        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * Get bytes from a buffer and make them available to WebAssembly.
     *
     * @param bufferType       The type of buffer to get
     * @param start            The start index in the buffer
     * @param length           The number of bytes to get
     * @param returnBufferData Pointer to where the buffer data address should be stored
     * @param returnBufferSize Pointer to where the buffer size should be stored
     * @return WasmResult status code
     */
    @WasmExport
    public int proxyGetBufferBytes(int bufferType, int start, int length, int returnBufferData, int returnBufferSize) {

        try {
            // Get the buffer based on the buffer type
            ByteBuffer buffer = getBuffer(instance, bufferType);
            if (buffer == null) {
                return WasmResult.NOT_FOUND.getValue();
            }

            if (start > start+length) {
                return WasmResult.BAD_ARGUMENT.getValue();
            }

            if (start+length > buffer.capacity()) {
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

    @WasmExport
    int proxySetEffectiveContext(int arg0) {
        return handler.setEffectiveContextID(arg0).getValue();
    }

    @WasmExport
    int proxyDone() {
        return handler.done().getValue();
    }

}
