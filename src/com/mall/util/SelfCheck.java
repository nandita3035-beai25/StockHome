package com.mall.util;

import com.mall.model.PerishableProduct;
import com.mall.model.Product;
import com.mall.model.ReorderSuggestion;
import com.mall.model.ShopZone;
import com.mall.model.UrgencyLevel;
import com.mall.service.DemandPredictor;
import com.mall.service.InventoryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SelfCheck verification suite for "Smart Inventory Demand Predictor".
 * 
 * Asserts:
 * 1. Weighted average on a known sales history list
 * 2. Days-until-stockout prediction calculation
 * 3. Reorder quantity estimation (ROP and target coverage)
 * 4. Undo last sale restores both stock and todaysSales
 * 5. closeDay appends todaysSales, resets it to 0, and bounds history at max 14 entries
 * 
 * Prints [PASS] or [FAIL] per check with detailed diagnostics.
 */
public class SelfCheck {

    private static int passedCount = 0;
    private static int totalCount = 0;

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("       STOCKHOME / MALL OF TOMORROW - VERIFICATION SUITE    ");
        System.out.println("============================================================\n");

        testWeightedAverage();
        testDaysUntilStockout();
        testReorderQuantity();
        testUndoRestoresStockAndTodaysSales();
        testCloseDayBoundedHistory();

        System.out.println("\n------------------------------------------------------------");
        System.out.printf("RESULT: %d / %d checks passed.\n", passedCount, totalCount);
        if (passedCount == totalCount) {
            System.out.println("ALL SYSTEM INVARIANTS SATISFIED [PASS]");
        } else {
            System.out.println("SOME CHECKS FAILED [FAIL]");
            System.exit(1);
        }
        System.out.println("============================================================\n");
    }

    private static void testWeightedAverage() {
        totalCount++;
        // Known history: [10, 20, 30]
        // Weights: 1, 2, 3 -> Sum of weights: 6
        // Weighted sum: 1*10 + 2*20 + 3*30 = 10 + 40 + 90 = 140
        // Expected WMA: 140.0 / 6.0 = 23.333333333333332
        List<Integer> history = Arrays.asList(10, 20, 30);
        PerishableProduct p = new PerishableProduct("TEST-01", "Test Item", 10.0, 50, "Food", 5, history);
        DemandPredictor predictor = new DemandPredictor(1.0);

        double expected = 140.0 / 6.0;
        double actualModel = p.calculateWeightedDailySales();
        double actualPredictor = predictor.calculateWeightedDailySales(p);

        boolean pass = Math.abs(actualModel - expected) < 1e-4 && Math.abs(actualPredictor - expected) < 1e-4;
        printResult("Weighted Average Calculation (Linear WMA)", pass,
                String.format("Expected: %.4f | Product: %.4f | Predictor: %.4f", expected, actualModel, actualPredictor));
    }

    private static void testDaysUntilStockout() {
        totalCount++;
        // Daily rate = 23.3333... Stock = 46 -> floor(46 / 23.3333) = 1 day left
        List<Integer> history = Arrays.asList(10, 20, 30);
        PerishableProduct p = new PerishableProduct("TEST-02", "Test Item 2", 10.0, 46, "Food", 5, history);
        DemandPredictor predictor = new DemandPredictor(1.0);

        int daysFromProduct = p.daysUntilStockout();
        int daysFromPredictor = predictor.estimateDaysUntilStockout(p);

        boolean pass = (daysFromProduct == 1) && (daysFromPredictor == 1);
        printResult("Days Until Stockout Prediction", pass,
                String.format("Stock: 46, Daily: 23.33 -> Expected: 1 | Product: %d | Predictor: %d",
                        daysFromProduct, daysFromPredictor));
    }

    private static void testReorderQuantity() {
        totalCount++;
        // Daily rate = 23.3333, Lead time = 2 days, Shelf life = 5 days
        // Safety stock = ceil(23.3333 * 1.5) = ceil(35.0) = 35
        // ROP = (23.3333 * 2) + 35 = 46.6667 + 35 = 81.6667
        // Review cycle = 7 days -> Target stock = (9 * 23.3333) + 35 = 210 + 35 = 245
        // Current stock = 10 -> Suggested Qty = ceil(245 - 10) = 235
        List<Integer> history = Arrays.asList(10, 20, 30);
        PerishableProduct p = new PerishableProduct("TEST-03", "Test Item 3", 10.0, 10, "Food", 5, history);
        DemandPredictor predictor = new DemandPredictor(1.0);

        ReorderSuggestion sug = predictor.evaluateReorder(p);
        boolean pass = (sug != null) && (sug.getSuggestedQuantity() == 235) && (sug.getUrgency() == UrgencyLevel.CRITICAL);
        printResult("Suggested Reorder Quantity & Urgency Evaluation", pass,
                String.format("Stock: 10 -> Expected Qty: 235, Urgency: CRITICAL | Actual Qty: %d, Urgency: %s",
                        sug != null ? sug.getSuggestedQuantity() : -1,
                        sug != null ? sug.getUrgency() : "null"));
    }

    private static void testUndoRestoresStockAndTodaysSales() {
        totalCount++;
        try {
            InventoryManager manager = new InventoryManager();
            ShopZone zone = new ShopZone("TEST", "Test Zone", "General");
            manager.registerZone(zone);

            PerishableProduct p = new PerishableProduct("UNDO-01", "Undo Test", 15.0, 50, "General", 10);
            manager.addProduct("TEST", p);

            // Record sale of 5 units
            manager.recordSale("UNDO-01", 5);
            boolean afterSale = (p.getStock() == 45) && (p.getTodaysSales() == 5);

            // Revert transaction
            manager.undoLastSale();
            boolean afterUndo = (p.getStock() == 50) && (p.getTodaysSales() == 0);

            boolean pass = afterSale && afterUndo;
            printResult("Undo Restores Stock and TodaysSales", pass,
                    String.format("Initial stock: 50 -> Sold 5 (Stock=%d, Today=%d) -> Reverted (Stock=%d, Today=%d)",
                            45, 5, p.getStock(), p.getTodaysSales()));
        } catch (Exception e) {
            printResult("Undo Restores Stock and TodaysSales", false, "Exception thrown: " + e.getMessage());
        }
    }

    private static void testCloseDayBoundedHistory() {
        totalCount++;
        // Start with 14 historical sales entries: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
        List<Integer> initial14 = new ArrayList<Integer>();
        for (int i = 1; i <= 14; i++) {
            initial14.add(i);
        }

        PerishableProduct p = new PerishableProduct("DAY-01", "Day Test", 5.0, 100, "Grocery", 7, initial14);

        // Record a sale today
        p.recordSaleEntry(25);
        boolean todayRecorded = (p.getTodaysSales() == 25) && (p.getSalesHistory().size() == 14);

        // Close day
        p.closeDay();

        List<Integer> updatedHistory = p.getSalesHistory();
        boolean sizeIs14 = updatedHistory.size() == 14;
        boolean oldestDropped = updatedHistory.get(0) == 2; // Oldest '1' was removed
        boolean latestAppended = updatedHistory.get(13) == 25; // 25 appended
        boolean todayReset = p.getTodaysSales() == 0;

        boolean pass = todayRecorded && sizeIs14 && oldestDropped && latestAppended && todayReset;
        printResult("closeDay Keeps SalesHistory Bounded at Max 14 Entries", pass,
                String.format("Size: %d (max 14) | Oldest: %d (expected 2) | Latest: %d (expected 25) | Today: %d",
                        updatedHistory.size(), updatedHistory.get(0), updatedHistory.get(13), p.getTodaysSales()));
    }

    private static void printResult(String testName, boolean pass, String details) {
        if (pass) {
            passedCount++;
            System.out.printf("[PASS] %s\n       Details: %s\n", testName, details);
        } else {
            System.out.printf("[FAIL] %s\n       Details: %s\n", testName, details);
        }
    }
}
