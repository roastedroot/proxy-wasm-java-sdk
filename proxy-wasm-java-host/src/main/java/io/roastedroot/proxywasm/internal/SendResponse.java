package io.roastedroot.proxywasm.internal;

public class SendResponse {

    private final int statusCode;
    private final byte[] statusCodeDetails;
    private final byte[] body;
    private final ProxyMap headers;
    private final int grpcStatus;

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

    public int statusCode() {
        return statusCode;
    }

    public byte[] statusCodeDetails() {
        return statusCodeDetails;
    }

    public byte[] body() {
        return body;
    }

    public ProxyMap headers() {
        return headers;
    }

    public int grpcStatus() {
        return grpcStatus;
    }
}
