package com.mlops.model;

/**
 * Represents a Machine Learning Model deployed within an ML Workspace.
 * Status values: "TRAINING", "DEPLOYED", "DEPRECATED"
 */
public class MachineLearningModel {

    private String id;              // Unique identifier, e.g., "MOD-8832"
    private String framework;       // e.g., "TensorFlow", "PyTorch", "Scikit-Learn"
    private String status;          // "TRAINING", "DEPLOYED", or "DEPRECATED"
    private double latestAccuracy;  // Most recent accuracy score recorded
    private String workspaceId;     // Foreign key linking to the hosting Workspace

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public MachineLearningModel() {}

    public MachineLearningModel(String id, String framework, String status,
                                double latestAccuracy, String workspaceId) {
        this.id = id;
        this.framework = framework;
        this.status = status;
        this.latestAccuracy = latestAccuracy;
        this.workspaceId = workspaceId;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFramework() { return framework; }
    public void setFramework(String framework) { this.framework = framework; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getLatestAccuracy() { return latestAccuracy; }
    public void setLatestAccuracy(double latestAccuracy) { this.latestAccuracy = latestAccuracy; }

    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
}
