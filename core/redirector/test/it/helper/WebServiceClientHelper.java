package it.helper;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.junit.Assert;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.net.URI;
import java.net.URL;

public class WebServiceClientHelper {

    public static HttpServer httpBuilder (String connectionUrl, String profileName) {
        try {
            URL url = new URL(connectionUrl);

            System.setProperty("spring.profiles.active", profileName);
            AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(SpringAnnotationConfig.class);

            ResourceConfig resourceConfig = new ResourceConfig();
            resourceConfig.register(RequestContextFilter.class);
            resourceConfig.property("contextConfig", annotationConfigApplicationContext);
            resourceConfig.register(RestSupport.class);

            URI baseUri = URI.create(url.getProtocol() + "://" + url.getAuthority());
            return  GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig, false);
        } catch (Exception e) {
            Assert.fail("Could'n parse configfile." + e.getMessage());
        }
        return null;
    }

    public static HttpServer httpBuilder (String connectionUrl) {
        try {
            URL url = new URL(connectionUrl);

            AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(SpringAnnotationConfig.class);

            ResourceConfig resourceConfig = new ResourceConfig();
            resourceConfig.register(RequestContextFilter.class);
            resourceConfig.property("contextConfig", annotationConfigApplicationContext);
            resourceConfig.register(RestSupport.class);

            URI baseUri = URI.create(url.getProtocol() + "://" + url.getAuthority());
            return  GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig, false);
        } catch (Exception e) {
            Assert.fail("Could'n parse configfile." + e.getMessage());
        }
        return null;
    }
    
}
