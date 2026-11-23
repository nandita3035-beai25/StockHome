package com.mall.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a distinct shopping sector or anchor store inside Mall of Tomorrow.
 * 
 * Demonstrates:
 * - OOP: Composition (Zone owns and manages Products)
 * - Collections: LinkedHashMap to preserve insertion sequence while providing O(1) key lookups
 * - Health metric aggregation for ASCII Mall Map color rendering
 */
public class ShopZone {
    private final String zoneCode;   // e.g. "FOOD", "ELEC", "FASH", "GROC"
    private final String name;       // e.g. "Food Court Gourmet"
    private final String category;   // Matching Product category
    private final Map<String, Product> productMap; // Key: Product ID -> Value: Product object

    public ShopZone(String zoneCode, String name, String category) {
        this.zoneCode = zoneCode;
        this.name = name;
        this.category = category;
        this.productMap = new LinkedHashMap<String, Product>(); // Collections: LinkedHashMap
    }

    public synchronized void addProduct(Product product) {
        if (product != null) {
            productMap.put(product.getId(), product);
        }
    }

    public synchronized Product removeProduct(String productId) {
        return productMap.remove(productId);
    }

    public synchronized Product getProduct(String productId) {
        return productMap.get(productId);
    }

    public synchronized boolean hasProduct(String productId) {
        return productMap.containsKey(productId);
    }

    public synchronized Collection<Product> getAllProducts() {
        return Collections.unmodifiableCollection(productMap.values());
    }

    public synchronized int getProductCount() {
        return productMap.size();
    }

    public synchronized int getTotalStockCount() {
        int total = 0;
        for (Product p : productMap.values()) {
            total += p.getStock();
        }
        return total;
    }

    /**
     * Determines aggregated stock health status for the zone:
     * - CRITICAL: if at least one product has <= 2 days of stock remaining
     * - WARNING: if no critical, but at least one product has <= 5 days remaining
     * - SAFE: all items have > 5 days of stock
     */
    public synchronized UrgencyLevel getZoneStockHealth() {
        if (productMap.isEmpty()) {
            return UrgencyLevel.SAFE;
        }

        boolean hasWarning = false;
        for (Product p : productMap.values()) {
            int days = p.daysUntilStockout();
            if (days <= 2) {
                return UrgencyLevel.CRITICAL; // Worst-case health propagation
            } else if (days <= 5) {
                hasWarning = true;
            }
        }

        return hasWarning ? UrgencyLevel.WARNING : UrgencyLevel.SAFE;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s (Category: %-12s, Items: %d, Total Units: %d, Health: %s)",
                zoneCode, name, category, productMap.size(), getTotalStockCount(), getZoneStockHealth().getLabel());
    }
}
