package com.mall.service;

import com.mall.exceptions.InvalidQuantityException;
import com.mall.exceptions.OutOfStockException;
import com.mall.exceptions.ProductNotFoundException;
import com.mall.model.Product;
import com.mall.model.SaleTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Background worker service simulating live customer traffic in Mall of Tomorrow.
 * 
 * Demonstrates:
 * - Multithreading: implements java.lang.Runnable
 * - Thread lifecycle: start, sleep, interrupt, join
 * - Volatile keyword: volatile boolean flag for thread visibility & graceful stop
 * - Synchronization: coordinates concurrent access to InventoryManager
 */
public class SimulationService implements Runnable {

    private final InventoryManager inventoryManager;
    private final Random random;
    private volatile boolean running;
    private int simulationTicks;
    private int successfulPurchases;
    private int outOfStockIncidents;
    private String lastEvent;

    public SimulationService(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        this.random = new Random();
        this.running = true; // Set active upon creation so monitor loop detects active state immediately
        this.simulationTicks = 0;
        this.successfulPurchases = 0;
        this.outOfStockIncidents = 0;
        this.lastEvent = "Simulation initialized. Waiting for customer traffic.";
    }

    @Override
    public void run() {
        this.running = true;
        this.lastEvent = "Mall footfall simulation active! Shoppers entering zones...";

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Sleep for 1000 milliseconds (1 second simulation interval)
                Thread.sleep(1000);
                simulationTicks++;

                // Pick a random product from inventory
                List<Product> products = new ArrayList<Product>();
                synchronized (inventoryManager) {
                    for (Product p : inventoryManager.getAlphabeticalCatalog().values()) {
                        products.add(p);
                    }
                }

                if (!products.isEmpty()) {
                    Product targetProduct = products.get(random.nextInt(products.size()));
                    int purchaseQty = 1 + random.nextInt(3); // Buy 1 to 3 units

                    try {
                        // Synchronized method call on InventoryManager
                        SaleTransaction tx = inventoryManager.recordSale(targetProduct.getId(), purchaseQty);
                        successfulPurchases++;
                        lastEvent = String.format("Customer bought %d x '%s' for $%.2f (Stock left: %d)",
                                purchaseQty, targetProduct.getName(), tx.getTotalAmount(), targetProduct.getStock());
                    } catch (OutOfStockException oos) {
                        outOfStockIncidents++;
                        lastEvent = String.format("Customer wanted %d x '%s' but STORE IS OUT OF STOCK!",
                                purchaseQty, targetProduct.getName());
                    } catch (ProductNotFoundException e) {
                        lastEvent = "Error: Target product vanished during simulation.";
                    } catch (InvalidQuantityException e) {
                        lastEvent = "Error: Invalid simulation quantity.";
                    }
                }
            } catch (InterruptedException ie) {
                // Restore interrupted flag and exit cleanly
                Thread.currentThread().interrupt();
                this.running = false;
                break;
            }
        }

        this.lastEvent = "Simulation stopped cleanly.";
    }

    /**
     * Signals the simulation thread to terminate.
     */
    public void stopSimulation() {
        this.running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public int getSimulationTicks() {
        return simulationTicks;
    }

    public int getSuccessfulPurchases() {
        return successfulPurchases;
    }

    public int getOutOfStockIncidents() {
        return outOfStockIncidents;
    }

    public String getLastEvent() {
        return lastEvent;
    }
}
