package com.mall.model;

import java.util.List;

/**
 * Subclass representing items that do not expire (Electronics, Clothing, Footwear).
 * 
 * Demonstrates:
 * - OOP: Inheritance (extends Product), Polymorphism (method overriding), super() call
 * - Domain-specific reorder logic: Longer lead times, larger bulk buffers, no spoilage risk.
 */
public class NonPerishableProduct extends Product {
    private int warrantyMonths; // Warranty duration provided to consumer

    public NonPerishableProduct(String id, String name, double price, int stock, String category, int warrantyMonths) {
        super(id, name, price, stock, category);
        this.warrantyMonths = warrantyMonths;
    }

    public NonPerishableProduct(String id, String name, double price, int stock, String category,
                                int warrantyMonths, List<Integer> initialHistory) {
        super(id, name, price, stock, category, initialHistory);
        this.warrantyMonths = warrantyMonths;
    }

    /**
     * Non-perishable goods typically require longer factory/distributor
     * lead times (e.g., 5 days).
     */
    @Override
    public int calculateLeadTimeDays() {
        return 5;
    }

    /**
     * Non-perishable goods can tolerate a higher safety buffer (3.0x daily sales)
     * because items do not spoil and stockouts risk high-value revenue loss.
     */
    @Override
    public int calculateSafetyStock() {
        double avg = averageDailySales();
        return (int) Math.ceil(avg * 3.0);
    }

    /**
     * Polymorphic reorder evaluation:
     * Standard industrial formula:
     * Reorder Point = (Average Daily Demand * Lead Time) + Safety Stock Buffer
     */
    @Override
    public boolean isReorderNeeded() {
        double avg = averageDailySales();
        int leadTime = calculateLeadTimeDays();
        int safetyStock = calculateSafetyStock();
        double reorderPoint = (avg * leadTime) + safetyStock;
        return this.getStock() <= reorderPoint;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | [Non-Perishable - Warranty: %dm]", warrantyMonths);
    }
}
