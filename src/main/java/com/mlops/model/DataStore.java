package com.mlops.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thread-safe in-memory data store for all MLOps resources.
 * Uses HashMaps as the database-equivalent storage as required by the spec.
 * Singleton pattern ensures consistent state across the application.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    // Primary data stores
    private final Map<String, MLWorkspace> workspaces = new HashMap<>();
    private final Map<String, MachineLearningModel> models = new HashMap<>();
    // Nested: modelId -> list of metrics
    private final Map<String, List<EvaluationMetric>> metrics = new HashMap<>();

    private DataStore() {
        seedData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Workspace operations
    // -------------------------------------------------------------------------

    public Map<String, MLWorkspace> getWorkspaces() { return workspaces; }

    public MLWorkspace getWorkspace(String id) { return workspaces.get(id); }

    public void putWorkspace(MLWorkspace ws) { workspaces.put(ws.getId(), ws); }

    public boolean deleteWorkspace(String id) {
        if (!workspaces.containsKey(id)) return false;
        workspaces.remove(id);
        return true;
    }

    // -------------------------------------------------------------------------
    // Model operations
    // -------------------------------------------------------------------------

    public Map<String, MachineLearningModel> getModels() { return models; }

    public MachineLearningModel getModel(String id) { return models.get(id); }

    public void putModel(MachineLearningModel model) { models.put(model.getId(), model); }

    public boolean deleteModel(String id) {
        if (!models.containsKey(id)) return false;
        models.remove(id);
        metrics.remove(id);
        return true;
    }

    // -------------------------------------------------------------------------
    // Metric operations
    // -------------------------------------------------------------------------

    public List<EvaluationMetric> getMetricsForModel(String modelId) {
        return metrics.getOrDefault(modelId, new ArrayList<>());
    }

    public void addMetric(String modelId, EvaluationMetric metric) {
        metrics.computeIfAbsent(modelId, k -> new ArrayList<>()).add(metric);
    }

    // -------------------------------------------------------------------------
    // Seed data (for demonstration purposes)
    // -------------------------------------------------------------------------

    private void seedData() {
        // Workspaces
        MLWorkspace ws1 = new MLWorkspace("WSVISION-01", "Computer Vision Lab", 500);
        MLWorkspace ws2 = new MLWorkspace("WSNLP-02", "NLP Research Team", 250);
        workspaces.put(ws1.getId(), ws1);
        workspaces.put(ws2.getId(), ws2);

        // Models
        MachineLearningModel m1 = new MachineLearningModel(
                "MOD-8832", "TensorFlow", "DEPLOYED", 0.94, "WSVISION-01");
        MachineLearningModel m2 = new MachineLearningModel(
                "MOD-4411", "PyTorch", "TRAINING", 0.81, "WSVISION-01");
        MachineLearningModel m3 = new MachineLearningModel(
                "MOD-9901", "Scikit-Learn", "DEPRECATED", 0.72, "WSNLP-02");

        models.put(m1.getId(), m1);
        models.put(m2.getId(), m2);
        models.put(m3.getId(), m3);

        ws1.addModelId(m1.getId());
        ws1.addModelId(m2.getId());
        ws2.addModelId(m3.getId());

        // Seed some metrics
        metrics.put("MOD-8832", new ArrayList<>());
        metrics.get("MOD-8832").add(new EvaluationMetric(
                "metric-001", System.currentTimeMillis() - 3600000L, 0.91));
        metrics.get("MOD-8832").add(new EvaluationMetric(
                "metric-002", System.currentTimeMillis() - 1800000L, 0.94));
    }
}
