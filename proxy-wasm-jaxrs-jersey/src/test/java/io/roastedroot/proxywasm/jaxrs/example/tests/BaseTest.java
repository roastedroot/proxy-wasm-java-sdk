package io.roastedroot.proxywasm.jaxrs.example.tests;

import io.restassured.specification.RequestSpecification;
import io.roastedroot.proxywasm.jaxrs.WasmPluginFeature;
import io.roastedroot.proxywasm.jaxrs.example.App;
import io.roastedroot.proxywasm.jaxrs.example.Resources;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseTest {
    protected Server server;
    protected static final int PORT = 8081;

    public RequestSpecification given() {
        return io.restassured.RestAssured.given().port(PORT);
    }

    @BeforeEach
    public void setUp() throws Exception {
        server = new Server(PORT);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(Resources.class);

        // Create mock Instance<PluginFactory> for WasmPluginFeature
        resourceConfig.register(
                new WasmPluginFeature(
                        new io.roastedroot.proxywasm.jaxrs.ServerAdaptor(),
                        App.headerTests(),
                        App.headerTestsNotShared(),
                        App.tickTests(),
                        App.ffiTests(),
                        App.httpCallTests()));

        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(resourceConfig));
        jerseyServlet.setInitOrder(0);
        context.addServlet(jerseyServlet, "/*");

        server.setHandler(context);
        server.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }
}
