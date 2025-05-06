package io.roastedroot.proxywasm.jaxrs;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A JAX-RS {@link NameBinding} annotation used to mark resource classes or methods
 * that should be intercepted and processed by the Proxy-Wasm plugins.
 *
 * <p>Apply this annotation to JAX-RS resource classes or methods to enable filtering
 * by the Proxy-Wasm plugins identified by the names specified in the {@link #value()} attribute.
 * The {@link ProxyWasmFeature} must be registered for this annotation to have effect.
 *
 * @see ProxyWasmFeature
 * @see ProxyWasmFilter
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ProxyWasm {
    /**
     * Specifies the names of the Proxy-Wasm plugins that should filter the annotated
     * resource class or method.
     *
     * @return An array of plugin names.
     */
    String[] value();
}
