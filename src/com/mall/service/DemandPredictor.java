package com.mall.service;

import com.mall.model.Product;
import com.mall.model.ReorderSuggestion;
import com.mall.model.ShopZone;
import com.mall.model.UrgencyLevel;

import java.util.List;
import java.util.Map;

/**
 * Core forecasting service implementing inventory algorithms.
 * 
 * Demonstrates:
 * - OOP: Separation of Concerns (Service layer handles mathematical forecasting)
 * - Multi-dimensional Arrays: 2D array analysis for zone-wide sales grids
 * - Mathematical models: Weighted Average, Weekend Multipliers, Reorder Points
 * - Wrapper classes: Double, Integer conversions
 * 
 * =========================================================================
 * PREDICTION MATHEMATICAL FORMULAS (College Viva Explanation):
 * =========================================================================
 * 1. WEIGHTED MOVING AVERAGE (WMA):
 *    Recent consumer sales reflect immediate trends better than older data.
 *    For N recorded days (e.g., 14 days), each day 'i' (from 1 to N) is assigned
 *    linear weight w_i = i.
 *    Formula:
 *        Weighted Average = (Sum_{i=1}^N (w_i * sales_i)) / (Sum_{i=1}^N w_i)
 *    Where Sum_{i=1}^N w_i = N * (N + 1) / 2
 * 
 * 2. FESTIVAL / WEEKEND DEMAND MULTIPLIER:
 *    Adjusted Demand = Weighted Average * Demand Multiplier (e.g. 1.25x for peak footfall)
 * 
 * 3. ESTIMATED DAYS UNTIL STOCKOUT:
 *    Days Left = Current Stock / Adjusted Demand Rate
 * 
 * 4. REORDER POINT FORMULA (ROP):
 *    ROP = (Adjusted Demand * Supplier Lead Time) + Safety Stock Buffer
 *    - If Current Stock <= ROP, Reorder is triggered.
 * 
 * 5. SUGGESTED REORDER QUANTITY:
 *    Target Stock = (Lead Time + Order Review Cycle) * Adjusted Demand + Safety Stock
 *    Suggested Quantity = Target Stock - Current Stock
 * =========================================================================
 */
public class DemandPredictor {

    // Default festival/weekend multiplier (1.0 = normal baseline, 1.25 = 25% surge)
    private double demandMultiplier;

    public DemandPredictor() {
        this.demandMultiplier = 1.0; // Standard weekday baseline
        Product.setGlobalDemandMultiplier(1.0);
    }

    public DemandPredictor(double demandMultiplier) {
        this.demandMultiplier = demandMultiplier > 0.0 ? demandMultiplier : 1.0;
        Product.setGlobalDemandMultiplier(this.demandMultiplier);
    }

    public double getDemandMultiplier() {
        return demandMultiplier;
    }

    public void setDemandMultiplier(double demandMultiplier) {
        if (demandMultiplier > 0.0) {
            this.demandMultiplier = demandMultiplier;
            Product.setGlobalDemandMultiplier(demandMultiplier);
        }
    }

    /**
     * Calculates the Weighted Moving Average (WMA) of a product's sales history.
     * Recent days receive linearly higher weight.
     * 
     * @param product The product being analyzed
     * @return Weighted average daily units sold
     */
    public double calculateWeightedDailySales(Product product) {
        if (product == null) {
            return 0.0;
        }

        List<Integer> history = product.getSalesHistory();
        if (history == null || history.isEmpty()) {
            return 0.0;
        }

        int n = history.size();
        double weightedSum = 0.0;
        int totalWeight = 0;

        // Day index 0 is oldest (weight 1), day index n-1 is yesterday (weight n)
        for (int i = 0; i < n; i++) {
            int weight = i + 1; // 1, 2, 3, ... n
            int unitsSold = history.get(i); // Auto-unboxing Integer to int
            weightedSum += (weight * unitsSold);
            totalWeight += weight;
        }

        if (totalWeight == 0) {
            return 0.0;
        }

        double baseWma = weightedSum / totalWeight;

        // Apply mall event / weekend multiplier
        return baseWma * this.demandMultiplier;
    }

    /**
     * Estimates days until stock runs out based on weighted forecasted demand.
     */
    public int estimateDaysUntilStockout(Product product) {
        if (product == null) {
            return 999;
        }
        double dailyRate = calculateWeightedDailySales(product);
        if (dailyRate <= 0.0001) {
            return 999; // Dead stock or 0 sales
        }
        int days = (int) Math.floor(product.getStock() / dailyRate);
        return Math.max(0, days);
    }

    /**
     * Determines the UrgencyLevel based on forecasted days until stockout.
     */
    public UrgencyLevel determineUrgency(int daysUntilStockout) {
        if (daysUntilStockout <= 2) {
            return UrgencyLevel.CRITICAL;
        } else if (daysUntilStockout <= 5) {
            return UrgencyLevel.WARNING;
        } else {
            return UrgencyLevel.SAFE;
        }
    }

    /**
     * Generates a comprehensive ReorderSuggestion for a given product.
     */
    public ReorderSuggestion evaluateReorder(Product product) {
        if (product == null) {
            return null;
        }

        double forecastedDaily = calculateWeightedDailySales(product);
        int daysLeft = estimateDaysUntilStockout(product);
        UrgencyLevel urgency = determineUrgency(daysLeft);

        int leadTime = product.calculateLeadTimeDays();
        int safetyStock = product.calculateSafetyStock();

        // Target stock for 7-day review cycle plus lead time buffer
        int reviewCycleDays = 7;
        double targetStock = ((leadTime + reviewCycleDays) * forecastedDaily) + safetyStock;

        int suggestedQty = 0;
        String rationale;

        if (product.getStock() <= ((forecastedDaily * leadTime) + safetyStock)) {
            suggestedQty = (int) Math.ceil(targetStock - product.getStock());
            if (suggestedQty < 10) {
                suggestedQty = 10; // Minimum economic order quantity (EOQ)
            }
            rationale = String.format("Stock below ROP buffer (%d units). Reorder %d units to cover %d-day lead time + safety buffer.",
                    (int)((forecastedDaily * leadTime) + safetyStock), suggestedQty, leadTime);
        } else {
            suggestedQty = 0;
            rationale = "Current stock is healthy and above reorder threshold.";
        }

        return new ReorderSuggestion(
                product,
                product.getStock(),
                forecastedDaily,
                daysLeft,
                suggestedQty,
                urgency,
                rationale
        );
    }

    // =========================================================================
    // MULTI-DIMENSIONAL ARRAY REQUIREMENT (2D Matrix Operations)
    // =========================================================================

    /**
     * Constructs a 2D integer matrix [Zones][Days] summarizing daily sales history.
     * Demonstrates multi-dimensional array creation, population, and row/column summing.
     * 
     * @param zonesMap Map of zones in Mall of Tomorrow
     * @param numDays Number of days tracked (e.g., 14)
     * @return 2D integer array where matrix[z][d] = total sales units in Zone z on Day d
     */
    public int[][] buildZoneSalesMatrix(Map<String, ShopZone> zonesMap, int numDays) {
        int zoneCount = zonesMap.size();
        // Multi-dimensional array declaration and instantiation:
        int[][] matrix = new int[zoneCount][numDays];

        int zoneIndex = 0;
        for (ShopZone zone : zonesMap.values()) {
            for (Product p : zone.getAllProducts()) {
                List<Integer> history = p.getSalesHistory();
                for (int d = 0; d < numDays && d < history.size(); d++) {
                    matrix[zoneIndex][d] += history.get(d);
                }
            }
            zoneIndex++;
        }

        return matrix;
    }

    /**
     * Calculates row-wise totals for a 2D sales matrix (Total sales per zone).
     */
    public int[] calculateZoneTotals(int[][] matrix) {
        int rows = matrix.length;
        int[] totals = new int[rows];
        for (int r = 0; r < rows; r++) {
            int sum = 0;
            for (int c = 0; c < matrix[r].length; c++) {
                sum += matrix[r][c];
            }
            totals[r] = sum;
        }
        return totals;
    }

    /**
     * Calculates column-wise daily mall footfall/sales totals across all zones.
     */
    public int[] calculateMallDailyTotals(int[][] matrix) {
        if (matrix.length == 0) return new int[0];
        int cols = matrix[0].length;
        int[] dailyTotals = new int[cols];

        for (int c = 0; c < cols; c++) {
            int daySum = 0;
            for (int r = 0; r < matrix.length; r++) {
                daySum += matrix[r][c];
            }
            dailyTotals[c] = daySum;
        }
        return dailyTotals;
    }
}
