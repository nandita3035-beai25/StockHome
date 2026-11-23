package com.mall.model;

/**
 * Interface representing inventory items whose future demand
 * and stock exhaustion timeline can be mathematically predicted.
 * 
 * College Concept: Interface definition, contract for polymorphic classes.
 */
public interface Predictable {
    /**
     * Calculates the baseline average daily sales based on historical sales data.
     * @return average units sold per day as a double
     */
    double averageDailySales();

    /**
     * Estimates how many full days current stock will last given daily consumption rate.
     * Formula: currentStock / averageDailySales
     * @return number of days until stock hits 0, or Integer.MAX_VALUE if sales are 0
     */
    int daysUntilStockout();
}
