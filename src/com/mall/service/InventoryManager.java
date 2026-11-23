package com.mall.service;

import com.mall.exceptions.InvalidQuantityException;
import com.mall.exceptions.OutOfStockException;
import com.mall.exceptions.ProductNotFoundException;
import com.mall.model.Product;
import com.mall.model.ReorderSuggestion;
import com.mall.model.SaleTransaction;
import com.mall.model.ShopZone;
import com.mall.model.UrgencyLevel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

/**
 * Core business management facade orchestrating all mall inventory operations.
 * 
 * Demonstrates:
 * - OOP: Facade design pattern, Encapsulation
 * - Collections extensively utilized:
 *   1. HashMap: O(1) fast product lookups by ID
 *   2. LinkedHashMap: Preserves mall zone layout sequence
 *   3. LinkedList: Maintains FIFO recent event audit log
 *   4. Deque (ArrayDeque): LIFO Stack for "Undo Last Sale"
 *   5. HashSet: Tracks unique categories and active critical product IDs
 *   6. PriorityQueue: Urgency ranking min-heap (most critical item polled first)
 *   7. TreeMap: Sorted alphabetical product index
 *   8. ArrayList: Filtered search results and reports
 *   9. Iterator: Explicit traversal demonstrating hasNext() and next()
 *  10. Comparable: Natural order ranking in PriorityQueue
 *  11. Comparator: Custom comparators for Top Sellers, Slow Movers, Dead Stock
 * - Multithreading: synchronized methods ensuring thread-safe concurrency
 * - Custom Exceptions: OutOfStockException, InvalidQuantityException, ProductNotFoundException
 */
public class InventoryManager {

    // 1. LinkedHashMap: preserves order of zones (Food Court -> Electronics -> Fashion -> Grocery)
    private final Map<String, ShopZone> zones;

    // 2. HashMap: fast O(1) global product index
    private final Map<String, Product> productLookup;

    // 3. LinkedList: audit trail of recent 15 inventory activities
    private final LinkedList<String> activityLog;

    // 4. Deque (ArrayDeque): LIFO Stack for undoing sales transactions
    private final Deque<SaleTransaction> undoStack;

    // 5. HashSet: tracks unique product categories
    private final Set<String> uniqueCategories;

    // Service dependency
    private final DemandPredictor demandPredictor;

    public InventoryManager() {
        this.zones = new LinkedHashMap<String, ShopZone>();
        this.productLookup = new HashMap<String, Product>();
        this.activityLog = new LinkedList<String>();
        this.undoStack = new ArrayDeque<SaleTransaction>();
        this.uniqueCategories = new HashSet<String>();
        this.demandPredictor = new DemandPredictor();
    }

    // =========================================================================
    // ZONE MANAGEMENT
    // =========================================================================

    public synchronized void registerZone(ShopZone zone) {
        if (zone != null) {
            zones.put(zone.getZoneCode().toUpperCase(), zone);
            logActivity("Zone registered: [" + zone.getZoneCode() + "] " + zone.getName());
        }
    }

    public synchronized ShopZone getZone(String zoneCode) {
        if (zoneCode == null) return null;
        return zones.get(zoneCode.toUpperCase());
    }

    public synchronized Map<String, ShopZone> getZones() {
        return Collections.unmodifiableMap(zones);
    }

    // =========================================================================
    // PRODUCT CRUD OPERATIONS (Synchronized & Thread-Safe)
    // =========================================================================

    /**
     * Adds a new product to a designated mall zone.
     */
    public synchronized void addProduct(String zoneCode, Product product) throws ProductNotFoundException {
        if (zoneCode == null || product == null) {
            throw new IllegalArgumentException("Zone code and product cannot be null.");
        }

        ShopZone zone = getZone(zoneCode);
        if (zone == null) {
            throw new ProductNotFoundException("Zone with code '" + zoneCode + "' does not exist.");
        }

        zone.addProduct(product);
        productLookup.put(product.getId().toUpperCase(), product);
        uniqueCategories.add(product.getCategory()); // HashSet records unique category

        logActivity(String.format("Added product '%s' (%s) to zone [%s]", 
                product.getName(), product.getId(), zone.getName()));
    }

    /**
     * Finds a product by its ID in O(1) time via HashMap.
     */
    public synchronized Product getProductById(String productId) throws ProductNotFoundException {
        if (productId == null) {
            throw new ProductNotFoundException("Product ID cannot be null.");
        }
        Product p = productLookup.get(productId.trim().toUpperCase());
        if (p == null) {
            throw new ProductNotFoundException(productId);
        }
        return p;
    }

    /**
     * Removes a product from the mall inventory.
     */
    public synchronized Product removeProduct(String productId) throws ProductNotFoundException {
        Product p = getProductById(productId); // Throws if not found

        // Remove from lookup
        productLookup.remove(p.getId().toUpperCase());

        // Remove from its host zone
        for (ShopZone zone : zones.values()) {
            if (zone.hasProduct(p.getId())) {
                zone.removeProduct(p.getId());
                break;
            }
        }

        logActivity("Removed product: " + p.getName() + " [" + p.getId() + "]");
        return p;
    }

    /**
     * Case-insensitive search using String methods: toLowerCase(), contains(), trim().
     */
    public synchronized List<Product> searchProducts(String query) {
        List<Product> results = new ArrayList<Product>();
        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String cleanQuery = query.trim().toLowerCase(); // String manipulation

        // Explicit Iterator usage:
        Iterator<Product> it = productLookup.values().iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p.getName().toLowerCase().contains(cleanQuery) ||
                p.getId().toLowerCase().contains(cleanQuery) ||
                p.getCategory().toLowerCase().contains(cleanQuery)) {
                results.add(p);
            }
        }

        return results;
    }

    // =========================================================================
    // SALES & RESTOCK TRANSACTIONS (Synchronized & Thread-Safe)
    // =========================================================================

    /**
     * Records a customer purchase, decrements stock, logs to undo stack.
     */
    public synchronized SaleTransaction recordSale(String productId, int quantity)
            throws ProductNotFoundException, OutOfStockException, InvalidQuantityException {

        if (quantity <= 0) {
            throw new InvalidQuantityException(quantity);
        }

        Product product = getProductById(productId); // Throws ProductNotFoundException

        if (product.getStock() < quantity) {
            throw new OutOfStockException(product.getId(), quantity, product.getStock());
        }

        // Deduct stock and record in daily sales
        product.deductStock(quantity);
        product.recordSaleEntry(quantity);

        // Create transaction and push onto LIFO Deque Stack
        SaleTransaction tx = new SaleTransaction(product.getId(), product.getName(), quantity, product.getPrice());
        undoStack.push(tx); // Deque push() for undo functionality

        logActivity(String.format("Sale: %dx '%s' ($%.2f). Stock remaining: %d",
                quantity, product.getName(), tx.getTotalAmount(), product.getStock()));

        return tx;
    }

    /**
     * Undoes the most recent sale transaction using Deque (LIFO Stack).
     * Restores stock and removes transaction from stack.
     */
    public synchronized SaleTransaction undoLastSale() throws IllegalStateException, ProductNotFoundException {
        if (undoStack.isEmpty()) {
            throw new IllegalStateException("Undo stack is empty! No sales transactions available to revert.");
        }

        SaleTransaction lastTx = undoStack.pop(); // Deque pop() retrieves most recent transaction
        Product product = getProductById(lastTx.getProductId());

        // Restore deducted units back into inventory
        product.addStock(lastTx.getQuantity());

        logActivity(String.format("REVERTED Sale #%d: Restored %d units of '%s'. Stock now: %d",
                lastTx.getTransactionId(), lastTx.getQuantity(), product.getName(), product.getStock()));

        return lastTx;
    }

    /**
     * Restocks units for a product.
     */
    public synchronized void restockProduct(String productId, int quantity)
            throws ProductNotFoundException, InvalidQuantityException {

        if (quantity <= 0) {
            throw new InvalidQuantityException("Restock quantity must be positive", quantity);
        }

        Product product = getProductById(productId);
        product.addStock(quantity);

        logActivity(String.format("Restocked: +%d units to '%s' [%s]. Current stock: %d",
                quantity, product.getName(), product.getId(), product.getStock()));
    }

    // =========================================================================
    // PREDICTIONS & PRIORITY QUEUE RANKINGS
    // =========================================================================

    /**
     * Evaluates all products and enqueues reorder suggestions into a PriorityQueue.
     * Demonstrates PriorityQueue (Min-Heap) ordered by Urgency and Days left.
     */
    public synchronized PriorityQueue<ReorderSuggestion> generateUrgencyQueue() {
        // PriorityQueue naturally orders by ReorderSuggestion's compareTo()
        PriorityQueue<ReorderSuggestion> pq = new PriorityQueue<ReorderSuggestion>();

        // Explicit Iterator usage:
        Iterator<Product> it = productLookup.values().iterator();
        while (it.hasNext()) {
            Product p = it.next();
            ReorderSuggestion suggestion = demandPredictor.evaluateReorder(p);
            if (suggestion != null) {
                pq.offer(suggestion); // Insert into heap
            }
        }

        return pq;
    }

    /**
     * Retrieves all active Critical alerts (HashSet ensures no duplicate alerts).
     */
    public synchronized Set<String> getActiveCriticalAlerts() {
        Set<String> criticalAlerts = new HashSet<String>();
        for (Product p : productLookup.values()) {
            int days = demandPredictor.estimateDaysUntilStockout(p);
            if (days <= 2) {
                criticalAlerts.add(String.format("CRITICAL: '%s' [%s] in %s has only %d units left (%d days)!",
                        p.getName(), p.getId(), p.getCategory(), p.getStock(), days));
            }
        }
        return criticalAlerts;
    }

    // =========================================================================
    // REPORT GENERATION (Comparators, TreeMap, Iterator)
    // =========================================================================

    /**
     * Returns products sorted by total sales descending (Top Sellers).
     * Demonstrates custom Comparator and Collections.sort().
     */
    public synchronized List<Product> getTopSellersReport() {
        List<Product> list = new ArrayList<Product>(productLookup.values());

        // Custom Comparator: Top Sellers (highest total sales first)
        Collections.sort(list, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                return Integer.compare(p2.getTotalUnitsSold(), p1.getTotalUnitsSold());
            }
        });

        return list;
    }

    /**
     * Returns products with the lowest daily sales velocity (Slow Movers).
     */
    public synchronized List<Product> getSlowMoversReport() {
        List<Product> list = new ArrayList<Product>(productLookup.values());

        // Custom Comparator: Slow Movers (lowest average daily sales first)
        Collections.sort(list, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                return Double.compare(p1.averageDailySales(), p2.averageDailySales());
            }
        });

        return list;
    }

    /**
     * Returns dead stock: items with zero or negligible sales in recent history
     * sorted by remaining excess stock descending.
     */
    public synchronized List<Product> getDeadStockReport() {
        List<Product> deadStock = new ArrayList<Product>();
        for (Product p : productLookup.values()) {
            if (p.getTotalUnitsSold() == 0 || p.averageDailySales() <= 0.2) {
                deadStock.add(p);
            }
        }

        // Custom Comparator: Dead stock with highest stuck inventory first
        Collections.sort(deadStock, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                return Integer.compare(p2.getStock(), p1.getStock());
            }
        });

        return deadStock;
    }

    /**
     * Returns all products sorted alphabetically using TreeMap.
     * Demonstrates TreeMap automatic key ordering.
     */
    public synchronized Map<String, Product> getAlphabeticalCatalog() {
        Map<String, Product> treeMap = new TreeMap<String, Product>();
        for (Product p : productLookup.values()) {
            treeMap.put(p.getName(), p); // TreeMap automatically maintains ascending order of product names
        }
        return treeMap;
    }

    // =========================================================================
    // AUDIT & LOGGING (LinkedList)
    // =========================================================================

    private synchronized void logActivity(String entry) {
        activityLog.addFirst(entry); // LinkedList addFirst
        while (activityLog.size() > 15) {
            activityLog.removeLast(); // Keep recent 15
        }
    }

    public synchronized List<String> getRecentActivityLog() {
        return Collections.unmodifiableList(activityLog);
    }

    public synchronized int getTotalProductCount() {
        return productLookup.size();
    }

    public synchronized Set<String> getUniqueCategories() {
        return Collections.unmodifiableSet(uniqueCategories);
    }

    public DemandPredictor getDemandPredictor() {
        return demandPredictor;
    }

    public Deque<SaleTransaction> getUndoStack() {
        return undoStack;
    }
}
