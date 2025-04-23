package org.example;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;

public class LimitadorTestContainer implements QuarkusTestResourceLifecycleManager {

    GenericContainer container;

    @Override
    public Map<String, String> start() {

        String config;
        try (InputStream is = LimitadorTestContainer.class.getResourceAsStream("limits.yaml")) {
            config = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        container =
                new GenericContainer("quay.io/kuadrant/limitador:v2.0.0")
                        .withCommand("limitador-server", "-vvv", "/opt/kuadrant/limits/limits.yaml")
                        .withCopyToContainer(
                                Transferable.of(config, 0777), "/opt/kuadrant/limits/limits.yaml")
                        .withExposedPorts(8080, 8081);
        container.start();
        Map<String, String> result =
                Map.of(
                        "limitador.http.url",
                                "http://"
                                        + container.getHost()
                                        + ":"
                                        + container.getMappedPort(8080),
                        "limitador.rls.url",
                                "http://"
                                        + container.getHost()
                                        + ":"
                                        + container.getMappedPort(8081));
        return result;
    }

    @Override
    public void stop() {
        container.stop();
    }
}
