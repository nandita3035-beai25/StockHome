package com.mall.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class representing a generic retail product in the Mall of Tomorrow.
 * 
 * Demonstrates:
 * - OOP: Abstraction (abstract class), Encapsulation (private fields), Inheritance base
 * - Static keyword: static counter for total products created
 * - 'this' keyword: disambiguating instance variables and constructor chaining
 * - Interfaces: implements Predictable and Comparable<Product>
 * - Collections: ArrayList for salesHistory
 * - Wrapper Classes: Integer for sales history records
 */
public abstract class Product implements Predictable, Comparable<Product> {
    // Static variable shared across all instances
    private static int totalProductsCreated = 0;
    private static double globalDemandMultiplier = 1.0;

    // Encapsulated instance variables
    private String id;
    private String name;
    private double price;
    private int stock;
    private String category;
    private int todaysSales = 0; // Tracks units sold today before closeDay()
    private List<Integer> salesHistory; // ArrayList storing past daily sales units

    /**
     * Parameterized Constructor
     */
    public Product(String id, String name, double price, int stock, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.salesHistory = new ArrayList<Integer>(); // Collections: ArrayList initialization

        // Increment static product counter
        Product.totalProductsCreated++;
    }

    /**
     * Overloaded Constructor with initial sales history
     */
    public Product(String id, String name, double price, int stock, String category, List<Integer> initialHistory) {
        this(id, name, price, stock, category); // Constructor chaining using this()
        if (initialHistory != null) {
            for (Integer units : initialHistory) {
                this.salesHistory.add(units); // Wrapper class auto-unboxing/boxing
            }
        }
    }

    // ==========================================
    // ABSTRACT METHODS (Polymorphism & Abstraction)
    // ==========================================

    /**
     * Calculates the minimum safety buffer required to prevent stockout during lead time.
     * Overridden differently by PerishableProduct vs NonPerishableProduct.
     */
    public abstract int calculateSafetyStock();

    /**
     * Returns supplier shipment lead time in days for replenishment.
     */
    public abstract int calculateLeadTimeDays();

    /**
     * Evaluates whether current inventory has breached reorder threshold:
     * Threshold formula: (Average Daily Sales * Lead Time Days) + Safety Stock
     */
    public abstract boolean isReorderNeeded();

    // ==========================================
    // PREDICTABLE INTERFACE IMPLEMENTATION
    // ==========================================

    public static void setGlobalDemandMultiplier(double multiplier) {
        if (multiplier > 0.0) {
            globalDemandMultiplier = multiplier;
        }
    }

    public static double getGlobalDemandMultiplier() {
        return globalDemandMultiplier;
    }

    /**
     * Calculates the linear weighted moving average (WMA) of a product's sales history:
     * Older days receive weight 1, while recent days receive linearly higher weight (up to N).
     * Scaled by the active global demand multiplier.
     */
    public double calculateWeightedDailySales() {
        if (salesHistory == null || salesHistory.isEmpty()) {
            return 0.0;
        }
        int n = salesHistory.size();
        double weightedSum = 0.0;
        int totalWeight = 0;
        for (int i = 0; i < n; i++) {
            int weight = i + 1;
            weightedSum += weight * salesHistory.get(i);
            totalWeight += weight;
        }
        if (totalWeight == 0) {
            return 0.0;
        }
        return (weightedSum / totalWeight) * globalDemandMultiplier;
    }

    /**
     * Baseline arithmetic mean of historical daily sales (unweighted).
     */
    public double unweightedDailySales() {
        if (salesHistory == null || salesHistory.isEmpty()) {
            return 0.0;
        }
        int sum = 0;
        for (int i = 0; i < salesHistory.size(); i++) {
            sum += salesHistory.get(i);
        }
        return (double) sum / salesHistory.size();
    }

    /**
     * Implements Predictable: returns the active weighted daily sales velocity.
     */
    @Override
    public double averageDailySales() {
        return calculateWeightedDailySales();
    }

    /**
     * Calculates days until stockout: Current Stock / Weighted Daily Sales Rate.
     * Returns 999 (effective infinity) if product has zero sales or negligible demand.
     */
    @Override
    public int daysUntilStockout() {
        double avg = calculateWeightedDailySales();
        if (avg <= 0.0001) {
            return 999; // Dead stock or no sales recorded yet
        }
        int days = (int) Math.floor(this.stock / avg);
        return Math.max(0, days);
    }

    // ==========================================
    // BUSINESS LOGIC & STATE MUTATION
    // ==========================================

    /**
     * Adds units to current stock.
     */
    public synchronized void addStock(int units) {
        if (units > 0) {
            this.stock += units;
        }
    }

    /**
     * Reduces current stock by sold units.
     */
    public synchronized void deductStock(int units) {
        if (units > 0 && units <= this.stock) {
            this.stock -= units;
        }
    }

    /**
     * Records today's sales units without appending to 14-day history yet.
     */
    public synchronized void recordSaleEntry(int units) {
        if (units > 0) {
            this.todaysSales += units;
        }
    }

    /**
     * Closes today's sales: appends today's sales figure to 14-day history,
     * resets today's sales to 0, and trims history so it stays at max 14 days.
     */
    public synchronized void closeDay() {
        this.salesHistory.add(Integer.valueOf(this.todaysSales));
        this.todaysSales = 0;
        while (this.salesHistory.size() > 14) {
            this.salesHistory.remove(0); // Discard oldest entry
        }
    }

    public synchronized int getTodaysSales() {
        return todaysSales;
    }

    public synchronized void setTodaysSales(int todaysSales) {
        this.todaysSales = Math.max(0, todaysSales);
    }

    public synchronized void deductTodaysSales(int units) {
        this.todaysSales = Math.max(0, this.todaysSales - units);
    }

    /**
     * Sum of all past units sold across recorded history plus today's sales.
     */
    public int getTotalUnitsSold() {
        int total = todaysSales;
        for (Integer sale : salesHistory) {
            total += sale.intValue();
        }
        return total;
    }

    // ==========================================
    // COMPARABLE INTERFACE IMPLEMENTATION
    // ==========================================

    /**
     * Natural ordering for Product: by product ID (lexicographical order).
     */
    @Override
    public int compareTo(Product other) {
        if (other == null) {
            return 1;
        }
        return this.id.compareToIgnoreCase(other.id);
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public static int getTotalProductsCreated() {
        return totalProductsCreated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public synchronized int getStock() {
        return stock;
    }

    public synchronized void setStock(int stock) {
        this.stock = Math.max(0, stock);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Integer> getSalesHistory() {
        return Collections.unmodifiableList(salesHistory);
    }

    public void addSalesHistoryEntry(int units) {
        this.salesHistory.add(units);
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s | Cat: %-12s | Price: $%-6.2f | Stock: %-4d | Avg/Day: %.1f",
                id, name, category, price, stock, averageDailySales());
    }
}
