package com.mlops.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an ML Workspace — a logical container for machine learning models
 * within a cloud-native MLOps environment.
 */
public class MLWorkspace {

    private String id;             // Unique identifier, e.g., "WSVISION-01"
    private String teamName;       // Human-readable team name, e.g., "Computer Vision Lab"
    private int storageQuotaGb;    // Maximum storage allocated for datasets in GB
    private List<String> modelIds = new ArrayList<>(); // IDs of models deployed here

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public MLWorkspace() {}

    public MLWorkspace(String id, String teamName, int storageQuotaGb) {
        this.id = id;
        this.teamName = teamName;
        this.storageQuotaGb = storageQuotaGb;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public int getStorageQuotaGb() { return storageQuotaGb; }
    public void setStorageQuotaGb(int storageQuotaGb) { this.storageQuotaGb = storageQuotaGb; }

    public List<String> getModelIds() { return modelIds; }
    public void setModelIds(List<String> modelIds) { this.modelIds = modelIds; }

    // Convenience helper to add a model ID
    public void addModelId(String modelId) {
        if (!this.modelIds.contains(modelId)) {
            this.modelIds.add(modelId);
        }
    }

    // Convenience helper to remove a model ID
    public void removeModelId(String modelId) {
        this.modelIds.remove(modelId);
    }
}
