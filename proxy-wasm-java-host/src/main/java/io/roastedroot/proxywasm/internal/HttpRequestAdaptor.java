package io.roastedroot.proxywasm.internal;

import io.roastedroot.proxywasm.WasmException;
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

    byte[] getProperty(PluginHttpContext pluginRequest, List<String> path) throws WasmException;

    WasmResult setProperty(PluginHttpContext pluginRequest, List<String> path, byte[] value);
}
