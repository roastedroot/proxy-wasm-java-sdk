package io.roastedroot.proxywasm.jaxrs.quarkus.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.jaxrs.spi.deployment.AdditionalJaxRsResourceMethodAnnotationsBuildItem;
import io.roastedroot.proxywasm.jaxrs.NamedWasmPlugin;
import io.roastedroot.proxywasm.jaxrs.WasmPluginFeature;
import java.util.List;
import org.jboss.jandex.DotName;

class ProxyWasmJaxrsQuarkusProcessor {

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem("proxy-wasm-jaxrs-quarkus");
    }

    @BuildStep
    AdditionalBeanBuildItem resources() {
        return new AdditionalBeanBuildItem(WasmPluginFeature.class);
    }

    @BuildStep
    public AdditionalJaxRsResourceMethodAnnotationsBuildItem annotations() {
        return new AdditionalJaxRsResourceMethodAnnotationsBuildItem(
                List.of(DotName.createSimple(NamedWasmPlugin.class.getName())));
    }
}
