# Proxy WASM Java Host

The Java implementation for proxy-wasm, enabling developer to run proxy-wasm extensions in Java.

## Building

To build the project, you need to have Maven installed. You can build the project using the following command:

```bash
mvn clean install
```

## Quarkus Example

The `quarkus-proxy-wasm-example` directory contains a simple example of how to use the proxy-wasm Java host with Quarkus.

It creates a simple JAX-RS application that uses ta proxy-wasm plugin to filter the http request before it reaches the application resource:

```java
package io.roastedroot.proxywasm.jaxrs.example;

import io.roastedroot.proxywasm.jaxrs.WasmPlugin;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/")
public class Resources {

    @Path("/test")
    @GET
    @WasmPlugin("example") // filter with example wasm plugin
    public String ffiTests() {
        return "Hello World";
    }
}
```

The `WasmPlugin` annotation is used to specify the name of the plugin to be used for filtering.

```java
package io.roastedroot.proxywasm.jaxrs.example;

import com.dylibso.chicory.experimental.aot.AotMachine;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.Plugin;
import io.roastedroot.proxywasm.PluginFactory;
import io.roastedroot.proxywasm.StartException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.nio.file.Path;

@ApplicationScoped
public class App {

    private static WasmModule module =
            Parser.parse(
                    Path.of("../proxy-wasm-java-host/src/test/go-examples/unit_tester/main.wasm"));

    // configure the the example wasm plugin
    @Produces
    public PluginFactory example() throws StartException {
        return () ->
                Plugin.builder(module)
                        .withName("example")
                        .withPluginConfig("{ \"type\": \"headerTests\" }")
                        .withMachineFactory(AotMachine::new)
                        .build();
    }
}
```

The method `example()` is give CDI a `PluginFactory` that can create instances of the `example` plugin.  An instance of that plugin will be used to filter the request on resources that have the `@WasmPlugin("example")` annotation.

### Running the Example

Once the project is [built](#building), you can run the example using the following command:

```bash
cd quarkus-proxy-wasm-example
java -jar target/quarkus-app/quarkus-run.jar
```

Then test it using curl command:

```bash
curl -i http://localhost:8080/test
```

The example uses a simple wasm plugin which just adds an `x-response-counter` header to the response. The value of the header is incremented for each request.  So you should see output like this:

```
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8
x-response-counter: 1
content-length: 11

Hello World
```
