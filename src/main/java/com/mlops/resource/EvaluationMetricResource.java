package com.mlops.resource;

import com.mlops.exception.ModelDeprecatedException;
import com.mlops.exception.ResourceNotFoundException;
import com.mlops.model.DataStore;
import com.mlops.model.EvaluationMetric;
import com.mlops.model.MachineLearningModel;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * Part 4.2 — Evaluation Metric Sub-Resource
 *
 * Handles /api/v1/models/{modelId}/metrics
 * Instantiated by ModelResource's sub-resource locator — NOT registered at a fixed @Path.
 *
 * @Produces at class level means all methods return JSON by default.
 * Individual methods can override this with their own @Produces if needed.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EvaluationMetricResource {

    private final DataStore store = DataStore.getInstance();
    private final String modelId;

    // The parent modelId is injected by the sub-resource locator
    public EvaluationMetricResource(String modelId) {
        this.modelId = modelId;
    }

    // -------------------------------------------------------------------------
    // GET / — fetch all evaluation metrics for the given model
    // -------------------------------------------------------------------------
    @GET
    public Response getMetrics() {
        // Verify model exists
        MachineLearningModel model = store.getModel(modelId);
        if (model == null) {
            throw new ResourceNotFoundException("Model", modelId);
        }

        List<EvaluationMetric> metrics = store.getMetricsForModel(modelId);
        return Response.ok(metrics).build();
    }

    // -------------------------------------------------------------------------
    // POST / — append a new evaluation metric for the given model
    // Side-effect: updates the parent model's latestAccuracy field.
    // Throws ModelDeprecatedException (→ 403) if model is DEPRECATED.
    // -------------------------------------------------------------------------
    @POST
    public Response addMetric(EvaluationMetric input) {
        // Verify model exists
        MachineLearningModel model = store.getModel(modelId);
        if (model == null) {
            throw new ResourceNotFoundException("Model", modelId);
        }

        // 5.3 State Constraint: DEPRECATED models cannot receive new metrics
        if ("DEPRECATED".equalsIgnoreCase(model.getStatus())) {
            throw new ModelDeprecatedException(modelId);
        }

        // Server generates ID and timestamp
        String id = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        if (input == null) {
            input = new EvaluationMetric();
        }
        input.setId(id);
        input.setTimestamp(timestamp);

        // Persist the metric
        store.addMetric(modelId, input);

        // Side-effect: update parent model's latestAccuracy for data consistency
        model.setLatestAccuracy(input.getAccuracyScore());
        store.putModel(model);

        return Response.status(Response.Status.CREATED).entity(input).build();
    }
}
