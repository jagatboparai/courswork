package com.mlops.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Part 1.2 — Discovery Endpoint
 *
 * GET /api/v1
 * Returns API metadata: versioning info, admin contact, and a map of primary resource collections.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("api", "MLOps Pipeline Management API");
        metadata.put("version", "1.0.0");
        metadata.put("description",
                "RESTful API for managing ML Workspaces, Models, and Evaluation Metrics.");

        // Admin contact
        Map<String, String> contact = new HashMap<>();
        contact.put("name", "MLOps Admin");
        contact.put("email", "admin@mlops-lab.ai");
        contact.put("documentation", "https://github.com/your-username/mlops-api");
        metadata.put("contact", contact);

        // Resource collection links
        Map<String, String> resources = new HashMap<>();
        resources.put("workspaces", "/api/v1/workspaces");
        resources.put("models", "/api/v1/models");
        metadata.put("resources", resources);

        // Supported HTTP methods overview
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /api/v1/workspaces", "List all workspaces");
        endpoints.put("POST /api/v1/workspaces", "Create a new workspace");
        endpoints.put("GET /api/v1/workspaces/{id}", "Get workspace by ID");
        endpoints.put("DELETE /api/v1/workspaces/{id}", "Delete workspace (only if empty)");
        endpoints.put("HEAD /api/v1/workspaces/{id}", "Check workspace existence");
        endpoints.put("GET /api/v1/models", "List all models (optional ?status= filter)");
        endpoints.put("POST /api/v1/models", "Register a new model");
        endpoints.put("GET /api/v1/models/{id}/metrics", "Get evaluation metrics for a model");
        endpoints.put("POST /api/v1/models/{id}/metrics", "Append an evaluation metric");
        metadata.put("endpoints", endpoints);

        metadata.put("timestamp", System.currentTimeMillis());

        return Response.ok(metadata).build();
    }
}
