package com.mall.ui;

import com.mall.exceptions.InvalidQuantityException;
import com.mall.exceptions.OutOfStockException;
import com.mall.exceptions.ProductNotFoundException;
import com.mall.model.NonPerishableProduct;
import com.mall.model.PerishableProduct;
import com.mall.model.Product;
import com.mall.model.ReorderSuggestion;
import com.mall.model.SaleTransaction;
import com.mall.model.ShopZone;
import com.mall.model.UrgencyLevel;
import com.mall.service.DemandPredictor;
import com.mall.service.InventoryManager;
import com.mall.service.SimulationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

/**
 * Interactive Console Interface and Menu Controller for Mall of Tomorrow.
 * 
 * Demonstrates:
 * - Exception Handling: robust try/catch/finally blocks handling InputMismatchException,
 *   NumberFormatException, and custom business exceptions without crashing.
 * - String parsing and validation: trim(), toUpperCase(), isEmpty().
 * - Collections and Iterators: explicit traversal of Maps, Lists, PriorityQueues.
 * - Multithreading coordination: starts, displays, interrupts, and joins simulation thread.
 */
public class MenuController {

    private final InventoryManager manager;
    private final MallMapRenderer mapRenderer;
    private final Scanner scanner;

    public MenuController(InventoryManager manager) {
        this.manager = manager;
        this.mapRenderer = new MallMapRenderer(manager.getZones());
        this.scanner = new Scanner(System.in);
    }

    /**
     * Main application loop.
     */
    public void start() {
        ConsoleAnimation.playIntroAnimation();

        boolean exitRequested = false;
        while (!exitRequested) {
            printMainMenu();
            int choice = readIntInput("Enter menu selection (1 - 12): ", 1, 12);

            try {
                switch (choice) {
                    case 1:
                        handleViewMallMap();
                        break;
                    case 2:
                        handleViewProductsByZone();
                        break;
                    case 3:
                        handleRecordSale();
                        break;
                    case 4:
                        handleRestockProduct();
                        break;
                    case 5:
                        handleAddNewProduct();
                        break;
                    case 6:
                        handlePredictStockouts();
                        break;
                    case 7:
                        handleReorderSuggestions();
                        break;
                    case 8:
                        handleUrgencyRankingPriorityQueue();
                        break;
                    case 9:
                        handleReportsMenu();
                        break;
                    case 10:
                        handleUndoLastSale();
                        break;
                    case 11:
                        handleLiveSimulationMode();
                        break;
                    case 12:
                        exitRequested = true;
                        System.out.println(ConsoleColors.bold("\nThank you for visiting Mall of Tomorrow! Closing systems... Goodbye!\n"));
                        break;
                    default:
                        System.out.println(ConsoleColors.red("Invalid option. Please choose between 1 and 12."));
                }
            } catch (Exception e) {
                System.out.println(ConsoleColors.red("An unexpected error occurred: " + e.getMessage()));
            } finally {
                // College Concept: try/finally demonstrates guaranteed execution
                if (!exitRequested && choice != 11) {
                    pauseForEnter();
                }
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n" + ConsoleColors.cyan("================================================================================"));
        System.out.println(ConsoleColors.bold("           MALL OF TOMORROW - SMART INVENTORY DEMAND PREDICTOR"));
        System.out.println(ConsoleColors.cyan("================================================================================"));
        
        // Show active critical announcement ticker if any critical alerts exist
        Set<String> criticalAlerts = manager.getActiveCriticalAlerts();
        if (!criticalAlerts.isEmpty()) {
            StringBuilder alertMsg = new StringBuilder();
            for (String a : criticalAlerts) {
                alertMsg.append(a).append(" | ");
            }
            System.out.println(ConsoleColors.red("  ALERT: " + alertMsg.toString()));
        }

        System.out.println("  1.  View Mall Map (ASCII Color Health)");
        System.out.println("  2.  View All Products by Zone");
        System.out.println("  3.  Record a Sale (Synchronized, pushes to Undo Stack)");
        System.out.println("  4.  Restock a Product (Animated Courier Walk)");
        System.out.println("  5.  Add a New Product (Perishable or Non-Perishable)");
        System.out.println("  6.  Predict Stock-outs (Weighted Average Forecast)");
        System.out.println("  7.  Generate Reorder Suggestions (ROP & Safety Stock)");
        System.out.println("  8.  Urgency Ranking (PriorityQueue Min-Heap)");
        System.out.println("  9.  Inventory Reports (Top Sellers, Slow Movers, Dead Stock, 2D Matrix)");
        System.out.println("  10. Undo Last Sale (LIFO Deque Stack)");
        System.out.println("  11. Start Live Simulation Mode (Background Thread + Live Stock Bars)");
        System.out.println("  12. Exit Application");
        System.out.println(ConsoleColors.cyan("--------------------------------------------------------------------------------"));
        System.out.printf("  [Settings: ANSI Colors=%s | Animations=%s | Event Multiplier=%.2fx]\n",
                ConsoleColors.isAnsiEnabled() ? "ON" : "OFF",
                ConsoleAnimation.isAnimationsEnabled() ? "ON" : "OFF",
                manager.getDemandPredictor().getDemandMultiplier());
        System.out.println(ConsoleColors.cyan("================================================================================"));
    }

    // =========================================================================
    // MENU HANDLERS
    // =========================================================================

    private void handleViewMallMap() {
        mapRenderer.renderMap();
        mapRenderer.print2DGridArchitecture();
    }

    private void handleViewProductsByZone() {
        System.out.println("\n" + ConsoleColors.bold("--- MALL INVENTORY DIRECTORY BY ZONE ---"));

        Map<String, ShopZone> zones = manager.getZones();
        for (ShopZone zone : zones.values()) {
            System.out.println("\n" + ConsoleColors.yellow(">>> Zone [" + zone.getZoneCode() + "] " + zone.getName() 
                    + " (" + zone.getCategory() + ") - Status: " + zone.getZoneStockHealth().getLabel()));
            System.out.println("    " + String.format("%-6s | %-26s | %-8s | %-6s | %-12s | %-10s",
                    "ID", "Product Name", "Price", "Stock", "Days Left", "Type"));
            System.out.println("    -------------------------------------------------------------------------------");

            // Explicit Iterator traversal demonstrating College Concept:
            Iterator<Product> it = zone.getAllProducts().iterator();
            while (it.hasNext()) {
                Product p = it.next();
                int daysLeft = manager.getDemandPredictor().estimateDaysUntilStockout(p);
                String daysStr = (daysLeft >= 999) ? "No Sales" : (daysLeft + " days");
                String typeStr = (p instanceof PerishableProduct) ? "Perishable" : "Non-Perish";

                String colorCode = ConsoleColors.GREEN;
                if (daysLeft <= 2) colorCode = ConsoleColors.BOLD_RED;
                else if (daysLeft <= 5) colorCode = ConsoleColors.BOLD_YELLOW;

                System.out.printf("    %-6s | %-26s | $%-7.2f | %-6d | %s | %-10s\n",
                        p.getId(), p.getName(), p.getPrice(), p.getStock(),
                        ConsoleColors.colorize(String.format("%-12s", daysStr), colorCode),
                        typeStr);
            }
        }
    }

    private void handleRecordSale() {
        System.out.println("\n" + ConsoleColors.bold("--- RECORD A SALE TRANSACTION ---"));
        String productId = readStringInput("Enter Product ID (e.g. F101, E201): ");
        int quantity = readIntInput("Enter Quantity to Sell: ", 1, 10000);

        try {
            SaleTransaction tx = manager.recordSale(productId, quantity);
            System.out.println(ConsoleColors.green("\n[SUCCESS] " + tx.toString()));

            Product p = manager.getProductById(productId);
            System.out.println("Updated Stock Level:");
            ConsoleAnimation.animateStockBar(p.getName(), p.getStock(), 100);

        } catch (OutOfStockException oos) {
            System.out.println(ConsoleColors.red("\n[TRANSACTION REJECTED]: " + oos.getMessage()));
            System.out.println(ConsoleColors.yellow("Suggestion: Check Reorder Suggestions (Option 7) to replenish stock."));
        } catch (ProductNotFoundException pnfe) {
            System.out.println(ConsoleColors.red("\n[LOOKUP FAILED]: " + pnfe.getMessage()));
        } catch (InvalidQuantityException iqe) {
            System.out.println(ConsoleColors.red("\n[INVALID INPUT]: " + iqe.getMessage()));
        }
    }

    private void handleRestockProduct() {
        System.out.println("\n" + ConsoleColors.bold("--- RESTOCK INVENTORY ---"));
        String productId = readStringInput("Enter Product ID to Restock: ");
        int quantity = readIntInput("Enter Restock Units: ", 1, 5000);

        try {
            manager.restockProduct(productId, quantity);
            Product p = manager.getProductById(productId);

            // Trigger walking ASCII courier animation
            ConsoleAnimation.animateShopperWalk(p.getCategory());

            System.out.println(ConsoleColors.green("[SUCCESS] Successfully added " + quantity + " units to " + p.getName()));
            ConsoleAnimation.animateStockBar(p.getName(), p.getStock(), 100);

        } catch (ProductNotFoundException pnfe) {
            System.out.println(ConsoleColors.red("[ERROR]: " + pnfe.getMessage()));
        } catch (InvalidQuantityException iqe) {
            System.out.println(ConsoleColors.red("[ERROR]: " + iqe.getMessage()));
        }
    }

    private void handleAddNewProduct() {
        System.out.println("\n" + ConsoleColors.bold("--- ADD NEW PRODUCT TO ZONE ---"));
        System.out.println("Available Zones: FOOD (Food Court), ELEC (Electronics), FASH (Fashion), GROC (Grocery)");
        String zoneCode = readStringInput("Enter Target Zone Code: ").toUpperCase();

        if (manager.getZone(zoneCode) == null) {
            System.out.println(ConsoleColors.red("Error: Zone '" + zoneCode + "' does not exist."));
            return;
        }

        String id = readStringInput("Enter Unique Product ID (e.g. F105, E205): ").toUpperCase();
        try {
            if (manager.getProductById(id) != null) {
                System.out.println(ConsoleColors.red("Error: A product with ID '" + id + "' already exists!"));
                return;
            }
        } catch (ProductNotFoundException expected) {
            // ID is available
        }

        String name = readStringInput("Enter Product Name: ");
        double price = readDoubleInput("Enter Unit Retail Price ($): ", 0.01, 100000.0);
        int initialStock = readIntInput("Enter Initial Stock Quantity: ", 0, 5000);
        String category = manager.getZone(zoneCode).getCategory();

        String isPerishable = readStringInput("Is this product Perishable? (Y/N): ").toUpperCase();

        Product newProduct;
        if (isPerishable.startsWith("Y")) {
            int shelfLife = readIntInput("Enter Shelf Life (Days): ", 1, 365);
            // Polymorphism: Product reference holding PerishableProduct instance
            newProduct = new PerishableProduct(id, name, price, initialStock, category, shelfLife);
        } else {
            int warranty = readIntInput("Enter Warranty Period (Months): ", 0, 120);
            // Polymorphism: Product reference holding NonPerishableProduct instance
            newProduct = new NonPerishableProduct(id, name, price, initialStock, category, warranty);
        }

        try {
            manager.addProduct(zoneCode, newProduct);
            System.out.println(ConsoleColors.green("\n[SUCCESS] Registered new product: " + newProduct.toString()));
        } catch (ProductNotFoundException e) {
            System.out.println(ConsoleColors.red("Failed to add product: " + e.getMessage()));
        }
    }

    private void handlePredictStockouts() {
        System.out.println("\n" + ConsoleColors.cyan("================================================================================"));
        System.out.println(ConsoleColors.bold("             DEMAND FORECASTING & STOCKOUT EXHAUSTION TIMELINES                 "));
        System.out.println(ConsoleColors.cyan("================================================================================"));
        System.out.printf("  %-6s | %-24s | %-6s | %-10s | %-10s | %-10s | %-10s\n",
                "ID", "Name", "Stock", "Simple Avg", "WMA Avg", "Days Left", "Urgency");
        System.out.println("  --------------------------------------------------------------------------------");

        DemandPredictor predictor = manager.getDemandPredictor();
        Map<String, Product> catalog = manager.getAlphabeticalCatalog();

        for (Product p : catalog.values()) {
            double simpleAvg = p.averageDailySales();
            double weightedAvg = predictor.calculateWeightedDailySales(p);
            int daysLeft = predictor.estimateDaysUntilStockout(p);
            UrgencyLevel urgency = predictor.determineUrgency(daysLeft);

            String daysStr = (daysLeft >= 999) ? "N/A" : (daysLeft + " d");
            String urgencyColor;
            switch (urgency) {
                case CRITICAL: urgencyColor = ConsoleColors.BOLD_RED; break;
                case WARNING:  urgencyColor = ConsoleColors.BOLD_YELLOW; break;
                default:        urgencyColor = ConsoleColors.BOLD_GREEN; break;
            }

            System.out.printf("  %-6s | %-24s | %-6d | %-10.2f | %-10.2f | %-10s | %s\n",
                    p.getId(), p.getName(), p.getStock(), simpleAvg, weightedAvg, daysStr,
                    ConsoleColors.colorize(String.format("%-10s", urgency.getLabel()), urgencyColor));
        }
        System.out.println("================================================================================");
    }

    private void handleReorderSuggestions() {
        System.out.println("\n" + ConsoleColors.cyan("================================================================================"));
        System.out.println(ConsoleColors.bold("                 REORDER RECOMMENDATIONS & SAFETY BUFFERS                      "));
        System.out.println(ConsoleColors.cyan("================================================================================"));

        DemandPredictor predictor = manager.getDemandPredictor();
        Map<String, Product> catalog = manager.getAlphabeticalCatalog();

        for (Product p : catalog.values()) {
            ReorderSuggestion sug = predictor.evaluateReorder(p);
            if (sug != null && sug.getSuggestedQuantity() > 0) {
                System.out.println(ConsoleColors.yellow(">>> REORDER ALERT FOR: [" + p.getId() + "] " + p.getName()));
                System.out.printf("    Current Stock: %-4d | Days Left: %-3d | Safety Buffer: %-3d | Lead Time: %d days\n",
                        sug.getCurrentStock(), sug.getDaysUntilStockout(),
                        p.calculateSafetyStock(), p.calculateLeadTimeDays());
                System.out.printf("    Suggested Order: %s units (Urgency: %s)\n",
                        ConsoleColors.bold(String.valueOf(sug.getSuggestedQuantity())),
                        ConsoleColors.colorize(sug.getUrgency().getLabel(), 
                                sug.getUrgency() == UrgencyLevel.CRITICAL ? ConsoleColors.BOLD_RED : ConsoleColors.BOLD_YELLOW));
                System.out.println("    Rationale: " + sug.getRationale());
                System.out.println("    ----------------------------------------------------------------------------");
            }
        }
    }

    private void handleUrgencyRankingPriorityQueue() {
        System.out.println("\n" + ConsoleColors.cyan("================================================================================"));
        System.out.println(ConsoleColors.bold("          PRIORITY QUEUE URGENCY RANKING (MOST CRITICAL PROCESSED FIRST)        "));
        System.out.println(ConsoleColors.cyan("================================================================================"));
        System.out.println("  College Concept: PriorityQueue acts as a Min-Heap based on ReorderSuggestion");
        System.out.println("  implements Comparable. Items are polled in order of highest risk to lowest.");
        System.out.println("--------------------------------------------------------------------------------");

        // Generate min-heap
        PriorityQueue<ReorderSuggestion> pq = manager.generateUrgencyQueue();
        int rank = 1;

        while (!pq.isEmpty()) {
            ReorderSuggestion item = pq.poll(); // PriorityQueue poll() extracts root element
            String color = ConsoleColors.GREEN;
            if (item.getUrgency() == UrgencyLevel.CRITICAL) color = ConsoleColors.BOLD_RED;
            else if (item.getUrgency() == UrgencyLevel.WARNING) color = ConsoleColors.BOLD_YELLOW;

            System.out.printf("  #%-2d | %s\n", rank++, ConsoleColors.colorize(item.toString(), color));
        }
        System.out.println("================================================================================");
    }

    private void handleReportsMenu() {
        System.out.println("\n" + ConsoleColors.bold("--- INVENTORY ANALYTICS & REPORTS ---"));
        System.out.println("  1. Top Sellers Report (Sorted by Total Units Sold Descending)");
        System.out.println("  2. Slow Movers Report (Sorted by Sales Velocity Ascending)");
        System.out.println("  3. Dead Stock Report (Zero/Negligible Sales with Excess Stock)");
        System.out.println("  4. Alphabetical Catalog Index (TreeMap Sorted by Name)");
        System.out.println("  5. Multi-dimensional 2D Zone Sales Matrix Analysis");
        int subChoice = readIntInput("Select Report (1 - 5): ", 1, 5);

        switch (subChoice) {
            case 1:
                renderTopSellersReport();
                break;
            case 2:
                renderSlowMoversReport();
                break;
            case 3:
                renderDeadStockReport();
                break;
            case 4:
                renderAlphabeticalCatalog();
                break;
            case 5:
                render2DMatrixReport();
                break;
        }
    }

    private void renderTopSellersReport() {
        System.out.println("\n" + ConsoleColors.green("=== TOP SELLERS REPORT (Comparator: Total Units Sold Descending) ==="));
        List<Product> topSellers = manager.getTopSellersReport();
        System.out.printf("  %-4s | %-6s | %-24s | %-15s | %-10s\n", "Rank", "ID", "Name", "Category", "Total Units");
        System.out.println("  --------------------------------------------------------------------");
        int rank = 1;
        for (Product p : topSellers) {
            System.out.printf("  #%-3d | %-6s | %-24s | %-15s | %-10d\n",
                    rank++, p.getId(), p.getName(), p.getCategory(), p.getTotalUnitsSold());
        }
    }

    private void renderSlowMoversReport() {
        System.out.println("\n" + ConsoleColors.yellow("=== SLOW MOVERS REPORT (Comparator: Daily Velocity Ascending) ==="));
        List<Product> slowMovers = manager.getSlowMoversReport();
        System.out.printf("  %-4s | %-6s | %-24s | %-10s | %-12s\n", "Rank", "ID", "Name", "Stock", "Avg Sold/Day");
        System.out.println("  --------------------------------------------------------------------");
        int rank = 1;
        for (Product p : slowMovers) {
            System.out.printf("  #%-3d | %-6s | %-24s | %-10d | %-12.2f\n",
                    rank++, p.getId(), p.getName(), p.getStock(), p.averageDailySales());
        }
    }

    private void renderDeadStockReport() {
        System.out.println("\n" + ConsoleColors.red("=== DEAD STOCK REPORT (Zero Sales in 14 Days, Sorted by Excess Stock) ==="));
        List<Product> deadStock = manager.getDeadStockReport();
        if (deadStock.isEmpty()) {
            System.out.println("  No dead stock detected! All products are moving.");
            return;
        }
        System.out.printf("  %-6s | %-24s | %-12s | %-10s | %-15s\n", "ID", "Name", "Price", "Dead Units", "Tied-Up Capital");
        System.out.println("  -----------------------------------------------------------------------------");
        double totalDeadCapital = 0.0;
        for (Product p : deadStock) {
            double capital = p.getStock() * p.getPrice();
            totalDeadCapital += capital;
            System.out.printf("  %-6s | %-24s | $%-11.2f | %-10d | $%-14.2f\n",
                    p.getId(), p.getName(), p.getPrice(), p.getStock(), capital);
        }
        System.out.println("  -----------------------------------------------------------------------------");
        System.out.printf("  Total Tied-Up Capital in Dead Stock: $%.2f\n", totalDeadCapital);
    }

    private void renderAlphabeticalCatalog() {
        System.out.println("\n" + ConsoleColors.cyan("=== ALPHABETICAL CATALOG (TreeMap Automatic Natural Key Ordering) ==="));
        Map<String, Product> treeMap = manager.getAlphabeticalCatalog();
        for (Map.Entry<String, Product> entry : treeMap.entrySet()) {
            System.out.printf("  %-26s => %s\n", entry.getKey(), entry.getValue().toString());
        }
    }

    private void render2DMatrixReport() {
        System.out.println("\n" + ConsoleColors.purple("=== 2D ZONE SALES MATRIX [Zones][Days] ==="));
        System.out.println("  Demonstrates Multi-dimensional Array calculations across 4 zones over 14 days.");

        int[][] matrix = manager.getDemandPredictor().buildZoneSalesMatrix(manager.getZones(), 14);
        int[] zoneTotals = manager.getDemandPredictor().calculateZoneTotals(matrix);
        int[] dailyTotals = manager.getDemandPredictor().calculateMallDailyTotals(matrix);

        String[] zoneNames = {"FOOD COURT", "ELECTRONICS", "FASHION HUB", "SUPER GROC"};

        System.out.print("  Zone / Day       |");
        for (int d = 1; d <= 14; d++) {
            System.out.printf(" D%-2d", d);
        }
        System.out.println(" | TOTAL");
        System.out.println("  -----------------+---------------------------------------------------------+-------");

        for (int r = 0; r < matrix.length; r++) {
            System.out.printf("  %-16s |", zoneNames[r]);
            for (int c = 0; c < matrix[r].length; c++) {
                System.out.printf(" %3d", matrix[r][c]);
            }
            System.out.printf(" | %5d\n", zoneTotals[r]);
        }

        System.out.println("  -----------------+---------------------------------------------------------+-------");
        System.out.print("  MALL TOTAL DAILY |");
        int grandTotal = 0;
        for (int c = 0; c < dailyTotals.length; c++) {
            System.out.printf(" %3d", dailyTotals[c]);
            grandTotal += dailyTotals[c];
        }
        System.out.printf(" | %5d\n", grandTotal);
    }

    private void handleUndoLastSale() {
        System.out.println("\n" + ConsoleColors.bold("--- UNDO LAST SALE (LIFO Deque Stack) ---"));
        try {
            SaleTransaction reverted = manager.undoLastSale();
            System.out.println(ConsoleColors.green("[SUCCESS] Reverted: " + reverted.toString()));
            Product p = manager.getProductById(reverted.getProductId());
            System.out.printf("Restored %d units back into stock. Current Stock: %d\n", reverted.getQuantity(), p.getStock());
        } catch (IllegalStateException ise) {
            System.out.println(ConsoleColors.yellow("[INFO]: " + ise.getMessage()));
        } catch (ProductNotFoundException pnfe) {
            System.out.println(ConsoleColors.red("[ERROR]: " + pnfe.getMessage()));
        }
    }

    /**
     * Multithreaded Live Mall Simulation:
     * - Background Worker Thread (Runnable) executes random customer buying activity every second.
     * - Main Thread concurrently repaints the stock bars and terminal health statistics.
     * - Stock mutations are synchronized inside InventoryManager.
     * - Stopped cleanly via interrupt() and join().
     */
    private void handleLiveSimulationMode() {
        System.out.println("\n" + ConsoleColors.cyan("================================================================================"));
        System.out.println(ConsoleColors.bold("                 LIVE REAL-TIME MALL FOOTFALL SIMULATION                       "));
        System.out.println(ConsoleColors.cyan("================================================================================"));
        System.out.println("  College Concept: Basic Multithreading (Thread, Runnable, sleep, join, synchronized)");
        System.out.println("  - A background Thread generates customer purchases every 1 second.");
        System.out.println("  - Stock mutations are synchronized in InventoryManager.");
        System.out.println("  - Press ENTER to stop the simulation cleanly.");
        System.out.println(ConsoleColors.cyan("--------------------------------------------------------------------------------"));

        SimulationService simulation = new SimulationService(manager);
        Thread simThread = new Thread(simulation, "MallCustomerSimulatorThread");

        // Start background worker thread
        simThread.start();

        System.out.println(ConsoleColors.green("Simulation thread [" + simThread.getName() + "] started!"));
        System.out.println("Monitoring live inventory health... (Running 5 cycles or press Enter):\n");

        // Run live monitor loop for 5 ticks
        for (int tick = 1; tick <= 5 && simulation.isRunning(); tick++) {
            try {
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            System.out.println("\n--- [SIMULATION TICK #" + tick + "] " + simulation.getLastEvent() + " ---");
            // Display live stock bars for 4 representative products
            try {
                Product f101 = manager.getProductById("F101");
                Product e201 = manager.getProductById("E201");
                Product s301 = manager.getProductById("S301");
                Product g401 = manager.getProductById("G401");

                System.out.println(f101.getName() + " (" + f101.getId() + "): " + 
                        ConsoleAnimation.buildStockBar(f101.getStock(), 60, 15) + " [" + f101.getStock() + " units]");
                System.out.println(e201.getName() + " (" + e201.getId() + "): " + 
                        ConsoleAnimation.buildStockBar(e201.getStock(), 40, 15) + " [" + e201.getStock() + " units]");
                System.out.println(s301.getName() + " (" + s301.getId() + "): " + 
                        ConsoleAnimation.buildStockBar(s301.getStock(), 30, 15) + " [" + s301.getStock() + " units]");
                System.out.println(g401.getName() + " (" + g401.getId() + "): " + 
                        ConsoleAnimation.buildStockBar(g401.getStock(), 80, 15) + " [" + g401.getStock() + " units]");
            } catch (Exception ignored) {
            }
        }

        // Clean termination using join()
        System.out.println(ConsoleColors.yellow("\nStopping simulation worker thread..."));
        simulation.stopSimulation();
        simThread.interrupt();

        try {
            simThread.join(2000); // Wait up to 2 seconds for thread to finish
            System.out.println(ConsoleColors.green("Simulation thread joined and terminated successfully."));
        } catch (InterruptedException ie) {
            System.out.println(ConsoleColors.red("Thread join interrupted: " + ie.getMessage()));
            Thread.currentThread().interrupt();
        }

        System.out.println(ConsoleColors.bold(String.format(
                "\nSIMULATION SUMMARY: %d Ticks | %d Purchases Fulfilled | %d Stockouts Encountered",
                simulation.getSimulationTicks(), simulation.getSuccessfulPurchases(), simulation.getOutOfStockIncidents())));
    }

    // =========================================================================
    // INPUT VALIDATION HELPERS (Guaranteed No Crashes)
    // =========================================================================

    private int readIntInput(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return 12; // Return Exit option on input stream termination / EOF
            }
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf(ConsoleColors.red("Please enter a number between %d and %d.\n"), min, max);
            } catch (NumberFormatException e) {
                System.out.println(ConsoleColors.red("Invalid numeric input. Please enter a valid whole number."));
            }
        }
    }

    private double readDoubleInput(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return min;
            }
            String line = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(line);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf(ConsoleColors.red("Please enter a value between %.2f and %.2f.\n"), min, max);
            } catch (NumberFormatException e) {
                System.out.println(ConsoleColors.red("Invalid decimal input. Please enter a valid number."));
            }
        }
    }

    private String readStringInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return "";
            }
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println(ConsoleColors.red("Input cannot be blank. Please try again."));
        }
    }

    private void pauseForEnter() {
        System.out.print(ConsoleColors.yellow("\nPress [ENTER] to return to the main menu..."));
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
}
