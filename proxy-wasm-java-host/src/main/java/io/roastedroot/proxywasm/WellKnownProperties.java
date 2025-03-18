package io.roastedroot.proxywasm;

public final class WellKnownProperties {
    private WellKnownProperties() {}

    // Proxy-Wasm properties
    public static final String PLUGIN_NAME = "plugin_name";
    public static final String PLUGIN_ROOT_ID = "plugin_root_id";
    public static final String PLUGIN_VM_ID = "plugin_vm_id";

    // Downstream connection properties
    public static final String CONNECTION_ID = "connection.id";
    public static final String SOURCE_ADDRESS = "source.address";
    public static final String SOURCE_PORT = "source.port";
    public static final String DESTINATION_ADDRESS = "destination.address";
    public static final String DESTINATION_PORT = "destination.port";
    public static final String CONNECTION_TLS_VERSION = "connection.tls_version";
    public static final String CONNECTION_REQUESTED_SERVER_NAME =
            "connection.requested_server_name";
    public static final String CONNECTION_MTLS = "connection.mtls";
    public static final String CONNECTION_SUBJECT_LOCAL_CERTIFICATE =
            "connection.subject_local_certificate";
    public static final String CONNECTION_SUBJECT_PEER_CERTIFICATE =
            "connection.subject_peer_certificate";
    public static final String CONNECTION_DNS_SAN_LOCAL_CERTIFICATE =
            "connection.dns_san_local_certificate";
    public static final String CONNECTION_DNS_SAN_PEER_CERTIFICATE =
            "connection.dns_san_peer_certificate";
    public static final String CONNECTION_URI_SAN_LOCAL_CERTIFICATE =
            "connection.uri_san_local_certificate";
    public static final String CONNECTION_URI_SAN_PEER_CERTIFICATE =
            "connection.uri_san_peer_certificate";
    public static final String CONNECTION_SHA256_PEER_CERTIFICATE_DIGEST =
            "connection.sha256_peer_certificate_digest";

    // Upstream connection properties
    public static final String UPSTREAM_ADDRESS = "upstream.address";
    public static final String UPSTREAM_PORT = "upstream.port";
    public static final String UPSTREAM_LOCAL_ADDRESS = "upstream.local_address";
    public static final String UPSTREAM_LOCAL_PORT = "upstream.local_port";
    public static final String UPSTREAM_TLS_VERSION = "upstream.tls_version";
    public static final String UPSTREAM_SUBJECT_LOCAL_CERTIFICATE =
            "upstream.subject_local_certificate";
    public static final String UPSTREAM_SUBJECT_PEER_CERTIFICATE =
            "upstream.subject_peer_certificate";
    public static final String UPSTREAM_DNS_SAN_LOCAL_CERTIFICATE =
            "upstream.dns_san_local_certificate";
    public static final String UPSTREAM_DNS_SAN_PEER_CERTIFICATE =
            "upstream.dns_san_peer_certificate";
    public static final String UPSTREAM_URI_SAN_LOCAL_CERTIFICATE =
            "upstream.uri_san_local_certificate";
    public static final String UPSTREAM_URI_SAN_PEER_CERTIFICATE =
            "upstream.uri_san_peer_certificate";
    public static final String UPSTREAM_SHA256_PEER_CERTIFICATE_DIGEST =
            "upstream.sha256_peer_certificate_digest";

    // HTTP request properties
    public static final String REQUEST_PROTOCOL = "request.protocol";
    public static final String REQUEST_TIME = "request.time";
    public static final String REQUEST_DURATION = "request.duration";
    public static final String REQUEST_SIZE = "request.size";
    public static final String REQUEST_TOTAL_SIZE = "request.total_size";

    // HTTP response properties
    public static final String RESPONSE_SIZE = "response.size";
    public static final String RESPONSE_TOTAL_SIZE = "response.total_size";
}
