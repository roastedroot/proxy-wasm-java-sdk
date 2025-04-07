package io.roastedroot.proxywasm.corazawaf.example;

import io.roastedroot.proxywasm.jaxrs.WasmPlugin;
import jakarta.ws.rs.Path;

@Path("/admin")
@WasmPlugin("waf") // use the corsaWAF filter
public class Admin extends Anything {}
