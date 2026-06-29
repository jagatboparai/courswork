package com.mlops.exception;

/**
 * Thrown when a client attempts to POST evaluation metrics to a DEPRECATED model.
 * Maps to HTTP 403 Forbidden.
 */
public class ModelDeprecatedException extends RuntimeException {

    private final String modelId;

    public ModelDeprecatedException(String modelId) {
        super("Model '" + modelId + "' is DEPRECATED and no longer accepts evaluation metrics.");
        this.modelId = modelId;
    }

    public String getModelId() { return modelId; }
}
