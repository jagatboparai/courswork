package com.mlops.application;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Application entry point.
 * Starts an embedded Grizzly HTTP server on port 8080.
 *
 * Run: java -jar target/mlops-api-1.0.0.jar
 * The API will be available at: http://localhost:8080/api/v1
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static final String BASE_URI = "http://0.0.0.0:8080/";

    public static HttpServer startServer() {
        ResourceConfig config = new ResourceConfig()
                // Scan our package for resources, providers, and filters
                .packages("com.mlops")
                // Register Jackson for JSON serialisation/deserialisation
                .register(JacksonFeature.class);

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        LOGGER.info("MLOps Pipeline Management API started.");
        LOGGER.info("Access the API at: http://localhost:8080/api/v1");
        LOGGER.info("Press ENTER to stop the server...");
        System.in.read();
        server.shutdownNow();
    }
}
