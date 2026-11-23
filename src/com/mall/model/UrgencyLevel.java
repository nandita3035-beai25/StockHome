package com.mall.model;

/**
 * Enumeration representing inventory stock health and reorder urgency tiers.
 * 
 * Criteria:
 * - CRITICAL: days left <= 2 days (immediate stockout risk)
 * - WARNING:  days left <= 5 days (reorder recommended)
 * - SAFE:     days left > 5 days (healthy inventory buffer)
 * 
 * College Concept: Java Enum with fields, constructors, and getters.
 */
public enum UrgencyLevel {
    CRITICAL(1, "CRITICAL", "Immediate reorder required! Stockout imminent!"),
    WARNING(2, "WARNING", "Low stock buffer. Order placed soon."),
    SAFE(3, "SAFE", "Healthy inventory levels. No action needed.");

    private final int priorityRank; // 1 = highest urgency
    private final String label;
    private final String description;

    UrgencyLevel(int priorityRank, String label, String description) {
        this.priorityRank = priorityRank;
        this.label = label;
        this.description = description;
    }

    public int getPriorityRank() {
        return priorityRank;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
