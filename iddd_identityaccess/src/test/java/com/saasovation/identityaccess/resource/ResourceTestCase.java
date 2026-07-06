//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.saasovation.identityaccess.resource;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;

import com.saasovation.identityaccess.application.ApplicationServiceTest;

public abstract class ResourceTestCase extends ApplicationServiceTest {

    protected final static int PORT = 8081;

    private Client client;
    private UndertowJaxrsServer server;

    protected ResourceTestCase() {
        super();
    }

    protected void dumpHeaders(MultivaluedMap<String, String> aResponseHeaders) {
        for (String key : aResponseHeaders.keySet()) {
            System.out.print(key + ":");
            String sep = " ";
            for (String value : aResponseHeaders.get(key)) {
                System.out.print(sep);
                System.out.print(value);
                sep = ", ";
            }

            System.out.println();
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        this.setClient(ClientBuilder.newClient());
        this.setUpEmbeddedServer();
    }

    protected void tearDown() throws Exception {
        this.getServer().stop();

        this.setServer(null);
        this.getClient().close();
        this.setClient(null);

        super.tearDown();
    }

    protected Response get(String aUrl, Object... aTemplatePairs) {
        WebTarget target = this.getClient().target(aUrl);

        for (int idx = 0; idx < aTemplatePairs.length; idx += 2) {
            target = target.resolveTemplate(
                    (String) aTemplatePairs[idx],
                    aTemplatePairs[idx + 1]);
        }

        return target.request().get();
    }

    private void setUpEmbeddedServer() {
        UndertowJaxrsServer server = new UndertowJaxrsServer();

        server.setPort(PORT);
        server.setHostname("localhost");
        server.deploy(new ResourceTestCaseApplication(), "/");

        server.start();

        this.setServer(server);
    }

    private Client getClient() {
        return client;
    }

    private void setClient(Client aClient) {
        this.client = aClient;
    }

    private UndertowJaxrsServer getServer() {
        return server;
    }

    private void setServer(UndertowJaxrsServer aServer) {
        this.server = aServer;
    }

    private static class ResourceTestCaseApplication extends Application {

        public ResourceTestCaseApplication() {
            super();
        }

        @Override
        public Set<Class<?>> getClasses() {
            Set<Class<?>> classes = new HashSet<Class<?>>();
            classes.add(GroupResource.class);
            classes.add(NotificationResource.class);
            classes.add(TenantResource.class);
            classes.add(UserResource.class);
            return classes;
        }

        @Override
        public Set<Object> getSingletons() {
            Set<Object> singletons = new HashSet<Object>();
            return singletons;
        }
    }
}
