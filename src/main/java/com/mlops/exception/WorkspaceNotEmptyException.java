package com.mlops.exception;

/**
 * Thrown when attempting to DELETE a workspace that still has models assigned to it.
 * Maps to HTTP 409 Conflict.
 */
public class WorkspaceNotEmptyException extends RuntimeException {

    private final String workspaceId;
    private final int modelCount;

    public WorkspaceNotEmptyException(String workspaceId, int modelCount) {
        super("Workspace '" + workspaceId + "' still has " + modelCount + " model(s) assigned.");
        this.workspaceId = workspaceId;
        this.modelCount = modelCount;
    }

    public String getWorkspaceId() { return workspaceId; }
    public int getModelCount() { return modelCount; }
}
