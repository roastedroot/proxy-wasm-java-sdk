package io.roastedroot.proxywasm.plugin;

import io.roastedroot.proxywasm.ProxyMap;
import io.roastedroot.proxywasm.WasmException;
import io.roastedroot.proxywasm.WasmResult;
import java.util.List;

/**
 * This interface will help us deal with differences in the http server impl.
 */
public interface HttpRequestAdaptor {

    String remoteAddress();

    int remotePort();

    String localAddress();

    int localPort();

    String protocol();

    ProxyMap getHttpRequestHeaders();

    ProxyMap getHttpRequestTrailers();

    ProxyMap getHttpResponseHeaders();

    ProxyMap getHttpResponseTrailers();

    ProxyMap getGrpcReceiveInitialMetaData();

    ProxyMap getGrpcReceiveTrailerMetaData();

    byte[] getProperty(HttpContext pluginRequest, List<String> path) throws WasmException;

    WasmResult setProperty(HttpContext pluginRequest, List<String> path, byte[] value);
}
