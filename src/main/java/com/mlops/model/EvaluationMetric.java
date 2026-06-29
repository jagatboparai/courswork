package com.mlops.model;

/**
 * Represents a single evaluation metric event captured for a ML model.
 * Used to track concept drift and performance over time.
 */
public class EvaluationMetric {

    private String id;             // Unique metric event ID (UUID recommended)
    private long timestamp;        // Epoch time (ms) when the metric was captured
    private double accuracyScore;  // The accuracy/F1 score recorded during evaluation

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public EvaluationMetric() {}

    public EvaluationMetric(String id, long timestamp, double accuracyScore) {
        this.id = id;
        this.timestamp = timestamp;
        this.accuracyScore = accuracyScore;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getAccuracyScore() { return accuracyScore; }
    public void setAccuracyScore(double accuracyScore) { this.accuracyScore = accuracyScore; }
}
