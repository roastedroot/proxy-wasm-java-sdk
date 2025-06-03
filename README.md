# Proxy-Wasm Java Host

[![Version](https://img.shields.io/maven-central/v/io.roastedroot/proxy-wasm-jaxrs?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.roastedroot/proxy-wasm-java-host-parent)[![Javadocs](http://javadoc.io/badge/io.roastedroot/proxy-wasm-jaxrs.svg)](http://javadoc.io/doc/io.roastedroot/proxy-wasm-jaxrs)

The Java host implementation for proxy-wasm, enabling developer to run Proxy-Wasm plugins in Java.

## Docs

If your using Quarkus, see the [Quarkus Proxy-Wasm Extension](https://docs.quarkiverse.io/quarkus-proxy-wasm/dev/index.html) docs.

## Building

To build the project, you need to have Maven installed. You can build the project using the following command:

```bash
mvn clean install
```

## Overview

Proxy-Wasm is a plugin system for network proxies. It lets you write plugins that can act as request filters in a
portable, sandboxed, and language-agnostic way, thanks to WebAssembly.

This Quarkus extension allows you to use Proxy-Wasm plugins to filter requests to Jakarta REST (formerly known as JAX-RS)
endpoints.

Adding a Proxy-Wasm plugin to a JAX-RS for a "waf" proxy-wasm module is as simple as adding a `@ProxyWasm` annotation
to a method or class:

```java
package org.example;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import io.roastedroot.proxywasm.jaxrs.ProxyWasm;

@Path("/example")
public class Example {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @ProxyWasm("waf")
    public String hello() {
        return "hello";
    }
}
```

### Configuring the Plugin with CDI

Configuring the Proxy-Wasm plugin is easy in a CDI container like Quarkus or TomEE. You produce it with
a `@ApplicationScoped` `@Produces` method, which returns a `PluginFactory` instance.

Below is an example that uses the [`PluginFactory.builder()`](https://javadoc.io/doc/io.roastedroot/proxy-wasm-java-host/latest/io/roastedroot/proxywasm/PluginFactory.Builder.html) API to create a `PluginFactory`:

```java
package org.example;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.PluginFactory;
import io.roastedroot.proxywasm.SimpleMetricsHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class App {

    private static WasmModule module =
        Parser.parse(App.class.getResourceAsStream("coraza-proxy-wasm.wasm"));

    @Produces
    public PluginFactory waf() {
        return PluginFactory.builder(module)
                        .withName("waf")
                        .withPluginConfig(" ... the config passed to the plugin ... ")
                        .withMetricsHandler(new SimpleMetricsHandler())
                        .build();
    }
}
```

### Non-CDI Configuration

Setting up non-CDI is also possible, but more complicated.  If anyone is interested helping document this, please
send in a PR.  Here is link to where we [configure the ProxyWasmFeature](https://github.com/roastedroot/proxy-wasm-java-host/blob/main/proxy-wasm-jaxrs/src/test/java/io/roastedroot/proxywasm/jaxrs/example/tests/BaseTest.java#L34)
for a Jersey based test case.

### Compiling WASM to Bytecode

By default, WASM modules are executed using the [Chicory](https://chicory.dev/) interpreter.  But if you want the WASM code to
run a near native speed, you should compile the WASM to Java bytecode using the chicory WASM to bytecode compiler.
Chicory supports compiling the WASM module at either build time or runtime.

#### Runtime Compilation

To enable runtime compilation, you need just need to add the following dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>com.dylibso.chicory</groupId>
  <artifactId>compiler</artifactId>
</dependency>
```

You then enable it on the PluginFactory builder by using it as the machine factory:

```java
@Produces
public PluginFactory waf() {
    return PluginFactory.builder(module)
                    .withMachineFactory(MachineFactoryCompiler::compile)
                    .withName("waf")
                    .withPluginConfig(CONFIG)
                    .withMetricsHandler(new SimpleMetricsHandler())
                    .build();
}
```

Please refer to the [Chicory Runtime Compilation documentation](https://chicory.dev/docs/usage/runtime-compiler)
for more details.

#### Build time Compilation

If you want to compile your Quarkus app to native,  then you MUST compile the WASM module at build time to avoid the use
of runtime reflection.

To compile your WASM module at build time, add the Chicory compiler Maven plugin to your `pom.xml`:

```xml
<plugin>
    <groupId>com.dylibso.chicory</groupId>
    <artifactId>chicory-compiler-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>wasm-shim</id>
            <goals>
                <goal>compile</goal>
            </goals>
            <configuration>
                <name>org.example.internal.WasmShim</name>
                <wasm>src/main/resources/plugin.wasm</wasm>
            </configuration>
        </execution>
    </executions>
</plugin>
```

This will generate a `WasmShim` class that provides both a `load()` method to get the `WasmModule` and a `create()`
method for the machine factory. Update your plugin factory to use the compiled module:

```java
@Produces
public PluginFactory waf() {
    return PluginFactory.builder(WasmShim.load())
                    .withMachineFactory(WasmShim::create)
                    .withName("waf")
                    .withPluginConfig(CONFIG)
                    .withMetricsHandler(new SimpleMetricsHandler())
                    .build();
}
```

Please refer to the [Chicory Build time Compilation documentation](https://chicory.dev/docs/usage/build-time-compiler)
for more details.

## JavaDocs

* [Proxy-Wasm JavaDocs](http://javadoc.io/doc/io.roastedroot/proxy-wasm-jaxrs)

## Examples

* [TomEE Integration Test](integration-tests/tomee) - A simple webapp run against TomEE with a Proxy-Wasm plugin.

### Docs and SDKs for plugin authors:

* [ABI specification](https://github.com/istio-ecosystem/wasm-extensions[Proxy-Wasm)
* [C++ SDK](https://github.com/proxy-wasm/proxy-wasm-cpp-sdk[Proxy-Wasm)
* [Rust SDK](https://github.com/proxy-wasm/proxy-wasm-rust-sdk[Proxy-Wasm)
* [Go SDK](https://github.com/proxy-wasm/proxy-wasm-go-sdk[Proxy-Wasm)
* [AssemblyScript SDK](https://github.com/solo-io/proxy-runtime[Proxy-Wasm)

### Popular Proxy-Wasm plugins:

* [Coraza WAF](https://github.com/corazawaf/coraza-proxy-wasm)
* [Kuadrant](https://github.com/Kuadrant/wasm-shim/)
* [Higress](https://higress.cn/en/plugin/)


## Building

To build the project, you need to have Maven installed. You can build the project using the following command:

```bash
mvn clean install
```
