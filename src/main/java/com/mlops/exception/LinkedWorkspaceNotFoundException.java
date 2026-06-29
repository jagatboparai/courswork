package com.mlops.exception;

/**
 * Thrown when a client attempts to POST a new Model with a workspaceId that does not exist.
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class LinkedWorkspaceNotFoundException extends RuntimeException {

    private final String requestedWorkspaceId;

    public LinkedWorkspaceNotFoundException(String requestedWorkspaceId) {
        super("No workspace found with id '" + requestedWorkspaceId +
              "'. The model cannot be registered without a valid parent workspace.");
        this.requestedWorkspaceId = requestedWorkspaceId;
    }

    public String getRequestedWorkspaceId() { return requestedWorkspaceId; }
}
