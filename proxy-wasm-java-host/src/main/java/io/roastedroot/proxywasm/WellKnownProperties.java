package io.roastedroot.proxywasm;

import java.util.List;

/**
 * Holds constants for the well-known properties defined by the Proxy-Wasm ABI.
 *
 * see: <a href="https://github.com/proxy-wasm/spec/tree/main/abi-versions/v0.1.0#well-known-properties">spec</a>
 * seeL <a href="https://www.envoyproxy.io/docs/envoy/v1.33.0/intro/arch_overview/advanced/attributes">envoy docs</a>
 */
public final class WellKnownProperties {
    private WellKnownProperties() {}

    // Proxy-Wasm properties
    public static final List<String> PLUGIN_NAME = List.of("plugin_name");
    public static final List<String> PLUGIN_ROOT_ID = List.of("plugin_root_id");
    public static final List<String> PLUGIN_VM_ID = List.of("plugin_vm_id");

    // Downstream connection properties
    public static final List<String> CONNECTION_ID = List.of("connection", "id");
    public static final List<String> SOURCE_ADDRESS = List.of("source", "address");
    public static final List<String> SOURCE_PORT = List.of("source", "port");
    public static final List<String> DESTINATION_ADDRESS = List.of("destination", "address");
    public static final List<String> DESTINATION_PORT = List.of("destination", "port");
    public static final List<String> CONNECTION_TLS_VERSION = List.of("connection", "tls_version");
    public static final List<String> CONNECTION_REQUESTED_SERVER_NAME =
            List.of("connection", "requested_server_name");
    public static final List<String> CONNECTION_MTLS = List.of("connection", "mtls");
    public static final List<String> CONNECTION_SUBJECT_LOCAL_CERTIFICATE =
            List.of("connection", "subject_local_certificate");
    public static final List<String> CONNECTION_SUBJECT_PEER_CERTIFICATE =
            List.of("connection", "subject_peer_certificate");
    public static final List<String> CONNECTION_DNS_SAN_LOCAL_CERTIFICATE =
            List.of("connection", "dns_san_local_certificate");
    public static final List<String> CONNECTION_DNS_SAN_PEER_CERTIFICATE =
            List.of("connection", "dns_san_peer_certificate");
    public static final List<String> CONNECTION_URI_SAN_LOCAL_CERTIFICATE =
            List.of("connection", "uri_san_local_certificate");
    public static final List<String> CONNECTION_URI_SAN_PEER_CERTIFICATE =
            List.of("connection", "uri_san_peer_certificate");
    public static final List<String> CONNECTION_SHA256_PEER_CERTIFICATE_DIGEST =
            List.of("connection", "sha256_peer_certificate_digest");

    // Upstream connection properties
    public static final List<String> UPSTREAM_ADDRESS = List.of("upstream", "address");
    public static final List<String> UPSTREAM_PORT = List.of("upstream", "port");
    public static final List<String> UPSTREAM_LOCAL_ADDRESS = List.of("upstream", "local_address");
    public static final List<String> UPSTREAM_LOCAL_PORT = List.of("upstream", "local_port");
    public static final List<String> UPSTREAM_TLS_VERSION = List.of("upstream", "tls_version");
    public static final List<String> UPSTREAM_SUBJECT_LOCAL_CERTIFICATE =
            List.of("upstream", "subject_local_certificate");
    public static final List<String> UPSTREAM_SUBJECT_PEER_CERTIFICATE =
            List.of("upstream", "subject_peer_certificate");
    public static final List<String> UPSTREAM_DNS_SAN_LOCAL_CERTIFICATE =
            List.of("upstream", "dns_san_local_certificate");
    public static final List<String> UPSTREAM_DNS_SAN_PEER_CERTIFICATE =
            List.of("upstream", "dns_san_peer_certificate");
    public static final List<String> UPSTREAM_URI_SAN_LOCAL_CERTIFICATE =
            List.of("upstream", "uri_san_local_certificate");
    public static final List<String> UPSTREAM_URI_SAN_PEER_CERTIFICATE =
            List.of("upstream", "uri_san_peer_certificate");
    public static final List<String> UPSTREAM_SHA256_PEER_CERTIFICATE_DIGEST =
            List.of("upstream", "sha256_peer_certificate_digest");
    public static final List<String> UPSTREAM_TRANSPORT_FAILURE_REASON =
            List.of("upstream", "transport_failure_reason");
    public static final List<String> UPSTREAM_REQUEST_ATTEMPT_COUNT =
            List.of("upstream", "request_attempt_count");
    public static final List<String> UPSTREAM_CX_POOL_READY_DURATION =
            List.of("upstream", "cx_pool_ready_duration");

    // Metadata and filter state properties
    public static final List<String> METADATA = List.of("metadata");
    public static final List<String> FILTER_STATE = List.of("filter_state");
    public static final List<String> UPSTREAM_FILTER_STATE = List.of("upstream_filter_state");

    // HTTP request properties
    public static final List<String> REQUEST_PROTOCOL = List.of("request", "protocol");
    public static final List<String> REQUEST_TIME = List.of("request", "time");

    public static final List<String> REQUEST_PATH = List.of("request", "path");
    public static final List<String> REQUEST_URL_PATH = List.of("request", "url_path");
    public static final List<String> REQUEST_HOST = List.of("request", "host");
    public static final List<String> REQUEST_SCHEME = List.of("request", "scheme");
    public static final List<String> REQUEST_METHOD = List.of("request", "method");
    public static final List<String> REQUEST_HEADERS = List.of("request", "headers");
    public static final List<String> REQUEST_REFERER = List.of("request", "referer");
    public static final List<String> REQUEST_USERAGENT = List.of("request", "useragent");
    public static final List<String> REQUEST_ID = List.of("request", "id");
    public static final List<String> REQUEST_QUERY = List.of("request", "query");

    // These properties are available once the request completes:
    public static final List<String> REQUEST_DURATION = List.of("request", "duration");
    public static final List<String> REQUEST_SIZE = List.of("request", "size");
    public static final List<String> REQUEST_TOTAL_SIZE = List.of("request", "total_size");

    // HTTP response properties
    public static final List<String> RESPONSE_SIZE = List.of("response", "size");
    public static final List<String> RESPONSE_TOTAL_SIZE = List.of("response", "total_size");

    public static final List<String> RESPONSE_CODE = List.of("response", "code");
    public static final List<String> RESPONSE_CODE_DETAILS = List.of("response", "code_details");
    public static final List<String> RESPONSE_FLAGS = List.of("response", "flags");
    public static final List<String> RESPONSE_GRPC_STATUS = List.of("response", "grpc_status");
    public static final List<String> RESPONSE_HEADERS = List.of("response", "headers");
    public static final List<String> RESPONSE_TRAILERS = List.of("response", "trailers");
    public static final List<String> RESPONSE_BACKEND_LATENCY =
            List.of("response", "backend_latency");
}
