package com.mall.util;

import com.mall.model.NonPerishableProduct;
import com.mall.model.PerishableProduct;
import com.mall.model.ShopZone;
import com.mall.service.InventoryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * In-memory dataset initialization utility.
 * Seeds Mall of Tomorrow with 4 major retail zones and 16 diverse products
 * containing 14 consecutive days of realistic sales histories.
 * 
 * Demonstrates:
 * - OOP: Instantiation of polymorphic subclasses (PerishableProduct & NonPerishableProduct)
 * - Collections: ArrayList and Arrays.asList wrapper conversions
 * - Seeded test cases guaranteed to demonstrate CRITICAL, WARNING, SAFE, and DEAD STOCK categories.
 */
public class DataSeeder {

    public static void seedMallData(InventoryManager manager) {
        if (manager == null) return;

        // 1. Initialize and register the 4 Mall Zones (LinkedHashMap preserves order)
        ShopZone foodCourt = new ShopZone("FOOD", "Gourmet Food Court", "Food & Beverage");
        ShopZone electronics = new ShopZone("ELEC", "TechNova Electronics", "Electronics");
        ShopZone fashionHub = new ShopZone("FASH", "Vogue & Velvet Fashion", "Fashion & Apparel");
        ShopZone superGrocery = new ShopZone("GROC", "Apex Super Grocery", "Groceries");

        manager.registerZone(foodCourt);
        manager.registerZone(electronics);
        manager.registerZone(fashionHub);
        manager.registerZone(superGrocery);

        // =========================================================================
        // ZONE 1: FOOD COURT (Perishables)
        // =========================================================================
        // F101: High demand, critically low stock -> CRITICAL (days left < 2)
        List<Integer> f101Sales = new ArrayList<Integer>(Arrays.asList(
                25, 28, 30, 27, 32, 35, 40, 38, 42, 45, 48, 50, 52, 55));
        PerishableProduct f101 = new PerishableProduct(
                "F101", "Artisan Burger Combo", 299.00, 35, "Food & Beverage", 2, f101Sales);

        // F102: Moderate demand -> WARNING (days left ~ 4.5 days)
        List<Integer> f102Sales = new ArrayList<Integer>(Arrays.asList(
                10, 12, 11, 14, 12, 15, 13, 16, 14, 15, 17, 18, 19, 20));
        PerishableProduct f102 = new PerishableProduct(
                "F102", "Truffle Mushroom Pizza", 499.00, 80, "Food & Beverage", 3, f102Sales);

        // F103: Healthy stock -> SAFE (days left > 8 days)
        List<Integer> f103Sales = new ArrayList<Integer>(Arrays.asList(
                15, 16, 15, 18, 17, 20, 19, 22, 21, 20, 24, 25, 23, 26));
        PerishableProduct f103 = new PerishableProduct(
                "F103", "Sparkling Boba Tea", 180.00, 240, "Food & Beverage", 4, f103Sales);

        // F104: Zero recent sales -> DEAD STOCK
        List<Integer> f104Sales = new ArrayList<Integer>(Arrays.asList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        PerishableProduct f104 = new PerishableProduct(
                "F104", "Durian Mousse Dessert", 220.00, 50, "Food & Beverage", 5, f104Sales);

        try {
            manager.addProduct("FOOD", f101);
            manager.addProduct("FOOD", f102);
            manager.addProduct("FOOD", f103);
            manager.addProduct("FOOD", f104);
        } catch (Exception e) {
            System.err.println("Seeding error for FOOD: " + e.getMessage());
        }

        // =========================================================================
        // ZONE 2: ELECTRONICS (Non-Perishables)
        // =========================================================================
        // E201: Fast-selling tech, almost out -> CRITICAL (days left ~ 1 day)
        List<Integer> e201Sales = new ArrayList<Integer>(Arrays.asList(
                8, 9, 10, 12, 11, 14, 15, 16, 18, 20, 22, 25, 24, 28));
        NonPerishableProduct e201 = new NonPerishableProduct(
                "E201", "Aero ANC Earbuds", 3499.00, 20, "Electronics", 12, e201Sales);

        // E202: High stock -> SAFE (days left ~ 12 days)
        List<Integer> e202Sales = new ArrayList<Integer>(Arrays.asList(
                4, 5, 4, 6, 5, 7, 6, 8, 7, 8, 9, 8, 10, 10));
        NonPerishableProduct e202 = new NonPerishableProduct(
                "E202", "Titanium Smartwatch Pro", 4999.00, 110, "Electronics", 24, e202Sales);

        // E203: Moderate stock -> WARNING (days left ~ 4 days)
        List<Integer> e203Sales = new ArrayList<Integer>(Arrays.asList(
                3, 4, 5, 4, 6, 5, 7, 6, 8, 7, 8, 9, 8, 10));
        NonPerishableProduct e203 = new NonPerishableProduct(
                "E203", "Mechanical RGB Keyboard", 2899.00, 35, "Electronics", 12, e203Sales);

        // E204: Zero sales -> DEAD STOCK
        List<Integer> e204Sales = new ArrayList<Integer>(Arrays.asList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        NonPerishableProduct e204 = new NonPerishableProduct(
                "E204", "Legacy VGA Adapter Cable", 399.00, 75, "Electronics", 6, e204Sales);

        try {
            manager.addProduct("ELEC", e201);
            manager.addProduct("ELEC", e202);
            manager.addProduct("ELEC", e203);
            manager.addProduct("ELEC", e204);
        } catch (Exception e) {
            System.err.println("Seeding error for ELEC: " + e.getMessage());
        }

        // =========================================================================
        // ZONE 3: FASHION (Non-Perishables)
        // =========================================================================
        // S301: Fast-selling denim jacket -> CRITICAL
        List<Integer> s301Sales = new ArrayList<Integer>(Arrays.asList(
                5, 6, 7, 8, 7, 9, 8, 10, 11, 12, 11, 13, 14, 15));
        NonPerishableProduct s301 = new NonPerishableProduct(
                "S301", "Vintage Denim Jacket", 1999.00, 15, "Fashion & Apparel", 6, s301Sales);

        // S302: High stock -> SAFE
        List<Integer> s302Sales = new ArrayList<Integer>(Arrays.asList(
                10, 12, 14, 15, 13, 16, 18, 17, 19, 20, 22, 21, 23, 25));
        NonPerishableProduct s302 = new NonPerishableProduct(
                "S302", "Organic Cotton Tee", 699.00, 190, "Fashion & Apparel", 3, s302Sales);

        // S303: Moderate stock -> WARNING
        List<Integer> s303Sales = new ArrayList<Integer>(Arrays.asList(
                2, 3, 2, 4, 3, 5, 4, 5, 6, 5, 6, 7, 6, 8));
        NonPerishableProduct s303 = new NonPerishableProduct(
                "S303", "Thermal Winter Hoodie", 2499.00, 25, "Fashion & Apparel", 6, s303Sales);

        // S304: Zero sales -> DEAD STOCK
        List<Integer> s304Sales = new ArrayList<Integer>(Arrays.asList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        NonPerishableProduct s304 = new NonPerishableProduct(
                "S304", "Retro Corduroy Flares", 1499.00, 55, "Fashion & Apparel", 3, s304Sales);

        try {
            manager.addProduct("FASH", s301);
            manager.addProduct("FASH", s302);
            manager.addProduct("FASH", s303);
            manager.addProduct("FASH", s304);
        } catch (Exception e) {
            System.err.println("Seeding error for FASH: " + e.getMessage());
        }

        // =========================================================================
        // ZONE 4: GROCERY (Perishable & Non-Perishable mix)
        // =========================================================================
        // G401: Daily essential milk -> CRITICAL
        List<Integer> g401Sales = new ArrayList<Integer>(Arrays.asList(
                30, 32, 35, 38, 40, 42, 45, 48, 50, 52, 55, 58, 60, 65));
        PerishableProduct g401 = new PerishableProduct(
                "G401", "Farm Fresh Organic Milk", 75.00, 45, "Groceries", 5, g401Sales);

        // G402: Artisan bread -> WARNING
        List<Integer> g402Sales = new ArrayList<Integer>(Arrays.asList(
                12, 14, 15, 16, 18, 17, 20, 19, 22, 24, 23, 25, 27, 30));
        PerishableProduct g402 = new PerishableProduct(
                "G402", "Artisan Sourdough Loaf", 140.00, 95, "Groceries", 3, g402Sales);

        // G403: Mineral salt -> SAFE
        List<Integer> g403Sales = new ArrayList<Integer>(Arrays.asList(
                3, 4, 3, 5, 4, 5, 4, 6, 5, 6, 5, 7, 6, 7));
        NonPerishableProduct g403 = new NonPerishableProduct(
                "G403", "Himalayan Pink Rock Salt", 95.00, 160, "Groceries", 36, g403Sales);

        // G404: Unpopular snack -> DEAD STOCK
        List<Integer> g404Sales = new ArrayList<Integer>(Arrays.asList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        PerishableProduct g404 = new PerishableProduct(
                "G404", "Dried Kelp Seaweed Crisps", 120.00, 65, "Groceries", 60, g404Sales);

        try {
            manager.addProduct("GROC", g401);
            manager.addProduct("GROC", g402);
            manager.addProduct("GROC", g403);
            manager.addProduct("GROC", g404);
        } catch (Exception e) {
            System.err.println("Seeding error for GROC: " + e.getMessage());
        }
    }
}
