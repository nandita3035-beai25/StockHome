package com.mall.model;

import java.util.List;

/**
 * Subclass representing items prone to spoilage (Groceries, Food Court meals).
 * 
 * Demonstrates:
 * - OOP: Inheritance (extends Product), Polymorphism (method overriding), super() call
 * - Domain-specific reorder logic: High turn-around, shorter lead time, restricted safety stock
 *   to minimize write-offs due to shelf-life expiration.
 */
public class PerishableProduct extends Product {
    private int shelfLifeDays; // Maximum days before item expires

    public PerishableProduct(String id, String name, double price, int stock, String category, int shelfLifeDays) {
        super(id, name, price, stock, category); // Invoke superclass constructor
        this.shelfLifeDays = shelfLifeDays;
    }

    public PerishableProduct(String id, String name, double price, int stock, String category, 
                            int shelfLifeDays, List<Integer> initialHistory) {
        super(id, name, price, stock, category, initialHistory);
        this.shelfLifeDays = shelfLifeDays;
    }

    /**
     * Perishable items have rapid supplier replenishment cycles (e.g., 2 days)
     * to keep stock fresh.
     */
    @Override
    public int calculateLeadTimeDays() {
        return 2;
    }

    /**
     * Safety stock buffer for perishables is lean (1.5x daily sales)
     * to prevent unsold inventory from spoiling past shelf life.
     */
    @Override
    public int calculateSafetyStock() {
        double avg = averageDailySales();
        return (int) Math.ceil(avg * 1.5);
    }

    /**
     * Polymorphic reorder evaluation:
     * Triggers reorder when stock falls below:
     * (avgDailySales * leadTimeDays) + safetyStock, OR when stock is below 5 units.
     */
    @Override
    public boolean isReorderNeeded() {
        double avg = averageDailySales();
        int leadTime = calculateLeadTimeDays();
        int safetyStock = calculateSafetyStock();
        double reorderPoint = (avg * leadTime) + safetyStock;
        
        // Also triggers warning if remaining stock exceeds shelf-life consumption capacity
        return this.getStock() <= reorderPoint;
    }

    public int getShelfLifeDays() {
        return shelfLifeDays;
    }

    public void setShelfLifeDays(int shelfLifeDays) {
        this.shelfLifeDays = shelfLifeDays;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | [Perishable - ShelfLife: %dd]", shelfLifeDays);
    }
}
