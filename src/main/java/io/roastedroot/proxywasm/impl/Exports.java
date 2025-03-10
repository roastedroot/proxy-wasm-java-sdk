package io.roastedroot.proxywasm.impl;

import com.dylibso.chicory.runtime.Instance;
import io.roastedroot.proxywasm.v1.WasmException;
import io.roastedroot.proxywasm.v1.WasmResult;

//@WasmModuleInterface("main.wasm")
public class Exports {

    private final Instance.Exports exports;

    public String getMallocFunctionName() {
        return mallocFunctionName;
    }

    public void setMallocFunctionName(String mallocFunctionName) {
        this.mallocFunctionName = mallocFunctionName;
    }

    String mallocFunctionName = "malloc";

    public Exports(Instance.Exports exports) {
        this.exports = exports;
    }

    int malloc(int size) throws WasmException {
        long ptr = exports.function(mallocFunctionName).apply(size)[0];
        if (ptr == 0) {
            throw new WasmException(WasmResult.INVALID_MEMORY_ACCESS);
        }
        return (int) ptr;
    }

    public boolean proxyOnVmStart(int arg0, int arg1) {
        long result = exports.function("proxy_on_vm_start").apply((long) arg0,
                (long) arg1)[0];
        return result != 0;
    }

    public int proxyValidateConfiguration(int arg0, int arg1) {
        long result = exports.function("proxy_validate_configuration").apply((long) arg0,
                (long) arg1)[0];
        return (int) result;
    }

    public boolean proxyOnConfigure(int arg0, int arg1) {
        long result = exports.function("proxy_on_configure").apply((long) arg0,
                (long) arg1)[0];
        return result != 0;
    }

    public void proxyOnTick(int arg0) {
        exports.function("proxy_on_tick").apply((long) arg0);

    }

    public void proxyOnContextCreate(int contextID, int parentContextID) {
        exports.function("proxy_on_context_create").apply((long) contextID,
                (long) parentContextID);

    }

    public int proxyOnNewConnection(int arg0) {
        long result = exports.function("proxy_on_new_connection").apply((long) arg0)[0];
        return (int) result;
    }

    public int proxyOnDownstreamData(int arg0, int arg1, int arg2) {
        long result = exports.function("proxy_on_downstream_data").apply((long) arg0,
                (long) arg1,
                (long) arg2)[0];
        return (int) result;
    }

    public int proxyOnUpstreamData(int arg0, int arg1, int arg2) {
        long result = exports.function("proxy_on_upstream_data").apply((long) arg0,
                (long) arg1,
                (long) arg2)[0];
        return (int) result;
    }

    public void proxyOnDownstreamConnectionClose(int arg0, int arg1) {
        exports.function("proxy_on_downstream_connection_close").apply((long) arg0,
                (long) arg1);

    }

    public void proxyOnUpstreamConnectionClose(int arg0, int arg1) {
        exports.function("proxy_on_upstream_connection_close").apply((long) arg0,
                (long) arg1);

    }

    public int proxyOnRequestHeaders(int contextID, int headers, int endOfStream) {
        long result = exports.function("proxy_on_request_headers").apply((long) contextID,
                (long) headers,
                (long) endOfStream)[0];
        return (int) result;
    }

    public int proxyOnRequestMetadata(int arg0, int arg1) {
        long result = exports.function("proxy_on_request_metadata").apply((long) arg0,
                (long) arg1)[0];
        return (int) result;
    }

    public int proxyOnRequestBody(int arg0, int arg1, int arg2) {
        long result = exports.function("proxy_on_request_body").apply((long) arg0,
                (long) arg1,
                (long) arg2)[0];
        return (int) result;
    }

    public int proxyOnRequestTrailers(int arg0, int arg1) {
        long result = exports.function("proxy_on_request_trailers").apply((long) arg0,
                (long) arg1)[0];
        return (int) result;
    }

    public int proxyOnResponseHeaders(int arg0, int arg1, int arg2) {
        long result = exports.function("proxy_on_response_headers").apply((long) arg0,
                (long) arg1,
                (long) arg2)[0];
        return (int) result;
    }

    public int proxyOnResponseMetadata(int arg0, int arg1) {
        long result = exports.function("proxy_on_response_metadata").apply((long) arg0,
                (long) arg1)[0];
        return (int) result;
    }

    public int proxyOnResponseBody(int arg0, int arg1, int arg2) {
        long result = exports.function("proxy_on_response_body").apply((long) arg0,
                (long) arg1,
                (long) arg2)[0];
        return (int) result;
    }

    public int proxyOnResponseTrailers(int arg0, int arg1) {
        long result = exports.function("proxy_on_response_trailers").apply((long) arg0,
                (long) arg1)[0];
        return (int) result;
    }

    public int proxyOnDone(int arg0) {
        long result = exports.function("proxy_on_done").apply((long) arg0)[0];
        return (int) result;
    }

    public void proxyOnLog(int arg0) {
        exports.function("proxy_on_log").apply((long) arg0);

    }

    public void proxyOnDelete(int arg0) {
        exports.function("proxy_on_delete").apply((long) arg0);

    }

    public void proxyOnHttpCallResponse(int arg0, int arg1, int arg2, int arg3, int arg4) {
        exports.function("proxy_on_http_call_response").apply((long) arg0,
                (long) arg1,
                (long) arg2,
                (long) arg3,
                (long) arg4);

    }

    public void proxyOnGrpcReceiveInitialMetadata(int arg0, int arg1, int arg2) {
        exports.function("proxy_on_grpc_receive_initial_metadata").apply((long) arg0,
                (long) arg1,
                (long) arg2);

    }

    public void proxyOnGrpcReceiveTrailingMetadata(int arg0, int arg1, int arg2) {
        exports.function("proxy_on_grpc_receive_trailing_metadata").apply((long) arg0,
                (long) arg1,
                (long) arg2);

    }

    public void proxyOnGrpcReceive(int arg0, int arg1, int arg2) {
        exports.function("proxy_on_grpc_receive").apply((long) arg0,
                (long) arg1,
                (long) arg2);

    }

    public void proxyOnGrpcClose(int arg0, int arg1, int arg2) {
        exports.function("proxy_on_grpc_close").apply((long) arg0,
                (long) arg1,
                (long) arg2);

    }

    public void proxyOnQueueReady(int arg0, int arg1) {
        exports.function("proxy_on_queue_ready").apply((long) arg0,
                (long) arg1);

    }

    public void proxyOnForeignFunction(int arg0, int arg1, int arg2) {
        exports.function("proxy_on_foreign_function").apply((long) arg0,
                (long) arg1,
                (long) arg2);
    }


    public void initialize() {
        exports.function("_initialize").apply();
    }

    public int main(int arg0, int arg1) {
        long result = exports.function("main").apply((long) arg0,
                (long) arg1)[0];
        return (int) result;
    }

    public void start() {
        exports.function("_start").apply();
    }
}
