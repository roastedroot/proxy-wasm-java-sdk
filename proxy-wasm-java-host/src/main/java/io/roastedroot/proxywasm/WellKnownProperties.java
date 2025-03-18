package io.roastedroot.proxywasm;

import java.util.List;

/**
 * Holds constants for the well-known properties defined by the Proxy-Wasm ABI.
 *
 * see: <a href="https://github.com/proxy-wasm/spec/tree/main/abi-versions/v0.1.0#well-known-properties">spec</a>
 */
public final class WellKnownProperties {
    private WellKnownProperties() {}

    // Proxy-Wasm properties
    public static final List<String> PLUGIN_NAME = List.of("plugin_name");
    public static final List<String> PLUGIN_ROOT_ID = List.of("plugin_root_id");
    public static final List<String> PLUGIN_VM_ID = List.of("plugin_vm_id");

    // Downstream connection properties
    public static final List<String> CONNECTION_ID = List.of("connection.id");
    public static final List<String> SOURCE_ADDRESS = List.of("source.address");
    public static final List<String> SOURCE_PORT = List.of("source.port");
    public static final List<String> DESTINATION_ADDRESS = List.of("destination.address");
    public static final List<String> DESTINATION_PORT = List.of("destination.port");
    public static final List<String> CONNECTION_TLS_VERSION = List.of("connection.tls_version");
    public static final List<String> CONNECTION_REQUESTED_SERVER_NAME =
            List.of("connection.requested_server_name");
    public static final List<String> CONNECTION_MTLS = List.of("connection.mtls");
    public static final List<String> CONNECTION_SUBJECT_LOCAL_CERTIFICATE =
            List.of("connection.subject_local_certificate");
    public static final List<String> CONNECTION_SUBJECT_PEER_CERTIFICATE =
            List.of("connection.subject_peer_certificate");
    public static final List<String> CONNECTION_DNS_SAN_LOCAL_CERTIFICATE =
            List.of("connection.dns_san_local_certificate");
    public static final List<String> CONNECTION_DNS_SAN_PEER_CERTIFICATE =
            List.of("connection.dns_san_peer_certificate");
    public static final List<String> CONNECTION_URI_SAN_LOCAL_CERTIFICATE =
            List.of("connection.uri_san_local_certificate");
    public static final List<String> CONNECTION_URI_SAN_PEER_CERTIFICATE =
            List.of("connection.uri_san_peer_certificate");
    public static final List<String> CONNECTION_SHA256_PEER_CERTIFICATE_DIGEST =
            List.of("connection.sha256_peer_certificate_digest");

    // Upstream connection properties
    public static final List<String> UPSTREAM_ADDRESS = List.of("upstream.address");
    public static final List<String> UPSTREAM_PORT = List.of("upstream.port");
    public static final List<String> UPSTREAM_LOCAL_ADDRESS = List.of("upstream.local_address");
    public static final List<String> UPSTREAM_LOCAL_PORT = List.of("upstream.local_port");
    public static final List<String> UPSTREAM_TLS_VERSION = List.of("upstream.tls_version");
    public static final List<String> UPSTREAM_SUBJECT_LOCAL_CERTIFICATE =
            List.of("upstream.subject_local_certificate");
    public static final List<String> UPSTREAM_SUBJECT_PEER_CERTIFICATE =
            List.of("upstream.subject_peer_certificate");
    public static final List<String> UPSTREAM_DNS_SAN_LOCAL_CERTIFICATE =
            List.of("upstream.dns_san_local_certificate");
    public static final List<String> UPSTREAM_DNS_SAN_PEER_CERTIFICATE =
            List.of("upstream.dns_san_peer_certificate");
    public static final List<String> UPSTREAM_URI_SAN_LOCAL_CERTIFICATE =
            List.of("upstream.uri_san_local_certificate");
    public static final List<String> UPSTREAM_URI_SAN_PEER_CERTIFICATE =
            List.of("upstream.uri_san_peer_certificate");
    public static final List<String> UPSTREAM_SHA256_PEER_CERTIFICATE_DIGEST =
            List.of("upstream.sha256_peer_certificate_digest");

    // HTTP request properties
    public static final List<String> REQUEST_PROTOCOL = List.of("request.protocol");
    public static final List<String> REQUEST_TIME = List.of("request.time");
    public static final List<String> REQUEST_DURATION = List.of("request.duration");
    public static final List<String> REQUEST_SIZE = List.of("request.size");
    public static final List<String> REQUEST_TOTAL_SIZE = List.of("request.total_size");

    // HTTP response properties
    public static final List<String> RESPONSE_SIZE = List.of("response.size");
    public static final List<String> RESPONSE_TOTAL_SIZE = List.of("response.total_size");
}
