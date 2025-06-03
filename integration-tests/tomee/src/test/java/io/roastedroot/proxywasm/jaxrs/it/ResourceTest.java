/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.roastedroot.proxywasm.jaxrs.it;

import jakarta.ws.rs.client.ClientBuilder;
import java.io.File;
import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ResourceTest extends Assert {

    @ArquillianResource private URL webappUrl;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {

        // Get GAV coordinates from system properties set by Maven
        String version = System.getProperty("proxy.wasm.version", "999-SNAPSHOT");
        String gav = "io.roastedroot:proxy-wasm-jaxrs:" + version;

        return ShrinkWrap.create(WebArchive.class)
                .addClasses(Resources.class, App.class)
                .addAsLibraries(Maven.resolver().resolve(gav).withTransitivity().asFile())
                .addAsWebInfResource(
                        new FileAsset(new File("src/main/webapp/WEB-INF/beans.xml")), "beans.xml");
    }

    @Test
    public void test() throws Exception {
        final var webTarget = ClientBuilder.newClient().target(webappUrl.toURI());

        var response = webTarget.path("/headerTests").request().get();
        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString("x-response-counter"));
        assertEquals("counter: 1", response.readEntity(String.class));

        response = webTarget.path("/headerTests").request().get();
        assertEquals(200, response.getStatus());
        assertEquals("2", response.getHeaderString("x-response-counter"));
        assertEquals("counter: 2", response.readEntity(String.class));
    }
}
