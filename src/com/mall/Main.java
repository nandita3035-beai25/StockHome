package com.mall;

import com.mall.service.InventoryManager;
import com.mall.ui.ConsoleAnimation;
import com.mall.ui.ConsoleColors;
import com.mall.ui.MenuController;
import com.mall.util.DataSeeder;

/**
 * Application Entry Point for "Smart Inventory Demand Predictor"
 * Shopping-Mall Theme: Mall of Tomorrow.
 * 
 * College Project for "Programming in Java" course.
 * 
 * Supports command line flags:
 *   --no-ansi  : Disables ANSI color escape codes (fallback mode)
 *   --no-anim  : Disables Thread.sleep animation delays (instant rendering)
 *   --event <val> : Sets weekend/festival demand multiplier (e.g. --event 1.3)
 */
public class Main {

    public static void main(String[] args) {
        // Parse command line arguments for environment configuration
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i].trim().toLowerCase();
                if (arg.equals("--gui")) {
                    // Launch graphical interface
                    com.mall.ui.gui.GuiMain.main(args);
                    return;
                } else if (arg.equals("--no-ansi")) {
                    ConsoleColors.setAnsiEnabled(false);
                    System.out.println("[INFO] ANSI terminal colors disabled.");
                } else if (arg.equals("--no-anim")) {
                    ConsoleAnimation.setAnimationsEnabled(false);
                    System.out.println("[INFO] Terminal animation pauses disabled.");
                } else if (arg.equals("--event") && i + 1 < args.length) {
                    try {
                        double mult = Double.parseDouble(args[i + 1]);
                        System.out.println("[INFO] Custom event demand multiplier set to: " + mult);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        // Initialize core manager
        InventoryManager inventoryManager = new InventoryManager();

        // Seed Mall of Tomorrow dataset (16 products, 4 zones, 14 days history)
        DataSeeder.seedMallData(inventoryManager);

        // Start user menu controller
        MenuController menuController = new MenuController(inventoryManager);
        menuController.start();
    }
}
