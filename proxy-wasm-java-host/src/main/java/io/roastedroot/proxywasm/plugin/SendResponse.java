package io.roastedroot.proxywasm.plugin;

import io.roastedroot.proxywasm.ProxyMap;

public class SendResponse {

    public final int statusCode;
    public final byte[] statusCodeDetails;
    public final byte[] body;
    public final ProxyMap headers;
    public final int grpcStatus;

    public SendResponse(
            int responseCode,
            byte[] responseCodeDetails,
            byte[] responseBody,
            ProxyMap additionalHeaders,
            int grpcStatus) {
        this.statusCode = responseCode;
        this.statusCodeDetails = responseCodeDetails;
        this.body = responseBody;
        this.headers = additionalHeaders;
        this.grpcStatus = grpcStatus;
    }
}
