package com.mlops.resource;

import com.mlops.exception.LinkedWorkspaceNotFoundException;
import com.mlops.exception.ResourceNotFoundException;
import com.mlops.model.DataStore;
import com.mlops.model.MLWorkspace;
import com.mlops.model.MachineLearningModel;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Part 3 & 4 — Model Resource + Sub-Resource Locator
 *
 * Manages /api/v1/models and delegates nested metric paths via a locator.
 */
@Path("/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelResource {

    private final DataStore store = DataStore.getInstance();

    // -------------------------------------------------------------------------
    // GET /api/v1/models — list all models, with optional ?status= filter
    // -------------------------------------------------------------------------
    @GET
    public Response getModels(@QueryParam("status") String status) {
        Collection<MachineLearningModel> allModels = store.getModels().values();

        if (status != null && !status.isBlank()) {
            // Filter by status (case-insensitive)
            List<MachineLearningModel> filtered = allModels.stream()
                    .filter(m -> status.equalsIgnoreCase(m.getStatus()))
                    .collect(Collectors.toList());
            return Response.ok(filtered).build();
        }

        return Response.ok(new ArrayList<>(allModels)).build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/models — register a new model
    // Integrity check: workspaceId must reference an existing workspace.
    // Server generates the model ID (UUID).
    // -------------------------------------------------------------------------
    @POST
    public Response createModel(MachineLearningModel input) {
        if (input == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Bad Request", "message", "Request body is required."))
                    .build();
        }

        // 3.1 Workspace integrity check
        if (input.getWorkspaceId() == null || input.getWorkspaceId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Bad Request", "message", "'workspaceId' is required."))
                    .build();
        }

        MLWorkspace workspace = store.getWorkspace(input.getWorkspaceId());
        if (workspace == null) {
            throw new LinkedWorkspaceNotFoundException(input.getWorkspaceId());
        }

        // Server-generated ID — prevents ID injection attacks
        String id = "MOD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        input.setId(id);

        // Default status to TRAINING if not provided
        if (input.getStatus() == null || input.getStatus().isBlank()) {
            input.setStatus("TRAINING");
        }

        store.putModel(input);

        // Link model to its workspace
        workspace.addModelId(id);
        store.putWorkspace(workspace);

        return Response.status(Response.Status.CREATED).entity(input).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/models/{modelId}
    // -------------------------------------------------------------------------
    @GET
    @Path("/{modelId}")
    public Response getModel(@PathParam("modelId") String modelId) {
        MachineLearningModel model = store.getModel(modelId);
        if (model == null) {
            throw new ResourceNotFoundException("Model", modelId);
        }
        return Response.ok(model).build();
    }

    // -------------------------------------------------------------------------
    // Part 4.1 — Sub-Resource Locator
    //
    // Handles /api/v1/models/{modelId}/metrics by returning an instance of
    // EvaluationMetricResource. JAX-RS delegates all HTTP methods under that
    // path to the returned sub-resource object at runtime.
    //
    // NOTE: No HTTP method annotation here — this is a locator, not a handler.
    // -------------------------------------------------------------------------
    @Path("/{modelId}/metrics")
    public EvaluationMetricResource getMetricSubResource(@PathParam("modelId") String modelId) {
        return new EvaluationMetricResource(modelId);
    }
}
