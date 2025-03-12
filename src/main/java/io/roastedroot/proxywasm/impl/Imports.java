package io.roastedroot.proxywasm.impl;

import com.dylibso.chicory.experimental.hostmodule.annotations.HostModule;
import com.dylibso.chicory.experimental.hostmodule.annotations.WasmExport;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.WasmRuntimeException;
import io.roastedroot.proxywasm.v1.BufferType;
import io.roastedroot.proxywasm.v1.Handler;
import io.roastedroot.proxywasm.v1.LogLevel;
import io.roastedroot.proxywasm.v1.MapType;
import io.roastedroot.proxywasm.v1.WasmException;
import io.roastedroot.proxywasm.v1.WasmResult;
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
     * Set a header map based on the map type.
     *
     * @param instance The WebAssembly instance
     * @param mapType  The type of map to set
     * @param map      The header map to set
     * @return WasmResult indicating success or failure
     */
    private WasmResult setMap(Instance instance, int mapType, Map<String, String> map) {
        var knownType = MapType.fromInt(mapType);
        if (knownType == null) {
            return handler.setCustomHeader(mapType, map);
        }

        switch (knownType) {
            case HTTP_REQUEST_HEADERS:
                return handler.setHttpRequestHeader(map);
            case HTTP_REQUEST_TRAILERS:
                return handler.setHttpRequestTrailer(map);
            case HTTP_RESPONSE_HEADERS:
                return handler.setHttpResponseHeader(map);
            case HTTP_RESPONSE_TRAILERS:
                return handler.setHttpResponseTrailer(map);
            case HTTP_CALL_RESPONSE_HEADERS:
                return handler.setHttpCallResponseHeaders(map);
            case HTTP_CALL_RESPONSE_TRAILERS:
                return handler.setHttpCallResponseTrailer(map);
            case GRPC_RECEIVE_INITIAL_METADATA:
                return handler.setGrpcReceiveInitialMetaData(map);
            case GRPC_RECEIVE_TRAILING_METADATA:
                return handler.setGrpcReceiveTrailerMetaData(map);
        }
        return WasmResult.NOT_FOUND;
    }

    /**
     * Get a buffer based on the buffer type.
     *
     * @param instance   The WebAssembly instance
     * @param bufferType The type of buffer to get
     * @return The buffer, or null if not found
     */
    private byte[] getBuffer(Instance instance, int bufferType) {
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
     * @param instance   The WebAssembly instance
     * @param bufferType The type of buffer to set
     * @param buffer     The buffer to set
     * @return WasmResult indicating success or failure
     */
    private WasmResult setBuffer(Instance instance, int bufferType, byte[] buffer) {
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
     * Set header map pairs from WebAssembly memory.
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
            Map<String, String> headerMap = getMap(instance, mapType);
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
     * Retrieves serialized size of all key-value pairs from the map mapType
     *
     * @param mapType    The type of map to set
     * @param returnSize Pointer to ruturn the size of the map data
     * @return WasmResult status code
     */
    @WasmExport
    public int proxyGetHeaderMapSize(int mapType, int returnSize) {
        try {

            // Get the header map based on the map type
            Map<String, String> header = getMap(instance, mapType);
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
     * Get a value from a header map by key.
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
            Map<String, String> headerMap = getMap(instance, mapType);
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
     * Replace a value in a header map.
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
            Map<String, String> headerMap = getMap(instance, mapType);
            if (headerMap == null) {
                return WasmResult.BAD_ARGUMENT.getValue();
            }

            // Get key from memory
            String key = readString(keyDataPtr, keySize);

            // Get value from memory
            String value = readString(valueDataPtr, valueSize);

            // Replace value in map
            var copy = new HashMap<>(headerMap);
            headerMap.put(key, value);
            setMap(instance, mapType, copy);

            return WasmResult.OK.getValue();

        } catch (WasmRuntimeException e) {
            return WasmResult.INVALID_MEMORY_ACCESS.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
    }

    /**
     * Add a value to a header map.
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
     * Remove a value from a header map.
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
            Map<String, String> headerMap = getMap(instance, mapType);
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
            setMap(instance, mapType, copy);

            return WasmResult.OK.getValue();

        } catch (WasmRuntimeException e) {
            return WasmResult.INVALID_MEMORY_ACCESS.getValue();
        } catch (WasmException e) {
            return e.result().getValue();
        }
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
    public int proxyGetBufferBytes(
            int bufferType, int start, int length, int returnBufferData, int returnBufferSize) {

        try {
            // Get the buffer based on the buffer type
            byte[] b = getBuffer(instance, bufferType);
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
     * Set bytes in a buffer.
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
            var buf = getBuffer(instance, bufferType);
            if (buf == null) {
                return WasmResult.NOT_FOUND.getValue();
            }

            // Get content from WebAssembly memory
            byte[] content = instance.memory().readBytes(dataPtr, dataSize);

            content = replaceBytes(buf, content, start, length);

            // Set the buffer using the appropriate handler method
            WasmResult result = setBuffer(instance, bufferType, content);
            return result.getValue();

        } catch (WasmRuntimeException e) {
            return WasmResult.INVALID_MEMORY_ACCESS.getValue();
        }
    }

    public static byte[] replaceBytes(
            byte[] existing, byte[] change, int replaceStart, int replaceLength) {

        if (replaceStart > existing.length) {
            replaceStart = existing.length;
        }
        if (replaceLength > existing.length) {
            replaceLength = existing.length;
        }

        // when we are replacing the whole buffer
        if (replaceStart == 0 && replaceLength == existing.length) {
            return change;
        }

        int newLength = change.length + (existing.length - replaceLength);
        byte[] result = new byte[newLength];

        // Copy the unchanged part before the start position
        System.arraycopy(existing, 0, result, 0, Math.min(replaceStart, existing.length));

        // Copy the new change bytes
        System.arraycopy(change, 0, result, replaceStart, change.length);

        // Copy the remaining unchanged part after replacement
        if (replaceStart + replaceLength < existing.length) {
            System.arraycopy(
                    existing,
                    replaceStart + replaceLength,
                    result,
                    replaceStart + change.length,
                    existing.length - (replaceStart + replaceLength));
        }

        return result;
    }

    /**
     * Send an HTTP response.
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
                        instance.memory()
                                .readBytes(responseCodeDetailsData, responseCodeDetailsSize);
            }

            // Get response body from memory
            byte[] responseBody = new byte[0];
            if (responseBodySize > 0) {
                responseBody = instance.memory().readBytes(responseBodyData, responseBodySize);
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

    @WasmExport
    int proxySetEffectiveContext(int contextId) {
        return handler.setEffectiveContextID(contextId).getValue();
    }

    @WasmExport
    int proxyDone() {
        return handler.done().getValue();
    }

    @WasmExport
    int proxySetTickPeriodMilliseconds(int tick_period) {
        return handler.setTickPeriodMilliseconds(tick_period).getValue();
    }

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
}
