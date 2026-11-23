package com.mall.ui;

import com.mall.model.UrgencyLevel;

/**
 * Terminal animation engine powered purely by ANSI escape codes and Thread.sleep.
 * 
 * Demonstrates:
 * - Multithreading basics: Thread.sleep for timed frames and delays
 * - String and StringBuilder manipulation: dynamic progress bars and marquee substrings
 * - Fallback mode: animations can be toggled off for instant rendering in test environments
 */
public final class ConsoleAnimation {

    private static boolean animationsEnabled = true;

    private ConsoleAnimation() {
    }

    public static boolean isAnimationsEnabled() {
        return animationsEnabled;
    }

    public static void setAnimationsEnabled(boolean enabled) {
        animationsEnabled = enabled;
    }

    /**
     * Pauses thread execution safely, catching InterruptedException.
     */
    public static void sleepMs(int millis) {
        if (!animationsEnabled || millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

    /**
     * Prints text with a typewriter effect (character by character).
     */
    public static void typeWrite(String text, int delayPerCharMs) {
        if (!animationsEnabled || delayPerCharMs <= 0) {
            System.out.print(text);
            return;
        }

        for (int i = 0; i < text.length(); i++) {
            System.out.print(text.charAt(i));
            System.out.flush();
            sleepMs(delayPerCharMs);
        }
    }

    /**
     * Plays the Grand Opening / Startup Intro:
     * 1. Rising shutter animation
     * 2. Typewritten welcome sign
     */
    public static void playIntroAnimation() {
        if (!animationsEnabled) {
            System.out.println(ConsoleColors.bold("=== WELCOME TO MALL OF TOMORROW ==="));
            return;
        }

        System.out.println("\n" + ConsoleColors.cyan("Connecting to Mall Mainframe Systems..."));
        sleepMs(400);

        // Rising Shutter Animation: 6 frames of rolling security gates
        String[] shutterFrames = {
            "============================================================",
            "|| [######] SHUTTER ROLLING UP: LEVEL 1 (20%) [######]     ||",
            "|| [######] SHUTTER ROLLING UP: LEVEL 2 (45%) [######]     ||",
            "|| [######] SHUTTER ROLLING UP: LEVEL 3 (70%) [######]     ||",
            "|| [######] SHUTTER ROLLING UP: LEVEL 4 (95%) [######]     ||",
            "\\*=================== GATES OPENED =====================*/"
        };

        for (String frame : shutterFrames) {
            System.out.println(ConsoleColors.yellow("  " + frame));
            sleepMs(150);
        }
        System.out.println();

        // Neon Typed Welcome Banner
        String banner = 
            "  *****************************************************************\n" +
            "  *                                                               *\n" +
            "  *        M A L L   O F   T O M O R R O W   -   S M A R T        *\n" +
            "  *             I N V E N T O R Y   P R E D I C T O R             *\n" +
            "  *                                                               *\n" +
            "  *****************************************************************\n";

        typeWrite(ConsoleColors.cyan(banner), 3);
        sleepMs(300);
    }

    /**
     * Renders a stock progress bar string: [██████░░░░] 60%
     * Uses StringBuilder for construction.
     */
    public static String buildStockBar(int currentStock, int maxCapacity, int barWidth) {
        if (maxCapacity <= 0) maxCapacity = 100;
        int clampedStock = Math.max(0, Math.min(currentStock, maxCapacity));
        double ratio = (double) clampedStock / maxCapacity;
        int filledBlocks = (int) Math.round(ratio * barWidth);
        int emptyBlocks = barWidth - filledBlocks;

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < filledBlocks; i++) {
            sb.append("█");
        }
        for (int i = 0; i < emptyBlocks; i++) {
            sb.append("░");
        }
        sb.append("] ");

        int percentage = (int) Math.round(ratio * 100);
        String pctStr = String.format("%3d%%", percentage);

        // Colorize bar based on fill level
        if (percentage <= 25) {
            return ConsoleColors.red(sb.toString() + pctStr);
        } else if (percentage <= 50) {
            return ConsoleColors.yellow(sb.toString() + pctStr);
        } else {
            return ConsoleColors.green(sb.toString() + pctStr);
        }
    }

    /**
     * Displays an animated fill-up of a stock bar from 0% to the target stock.
     */
    public static void animateStockBar(String productName, int currentStock, int maxCapacity) {
        if (!animationsEnabled) {
            System.out.println("  " + productName + ": " + buildStockBar(currentStock, maxCapacity, 20));
            return;
        }

        System.out.print("  " + String.format("%-18s: ", productName));
        int targetPct = (int) Math.min(100, Math.round(((double) currentStock / maxCapacity) * 100));
        
        for (int p = 0; p <= targetPct; p += 10) {
            int simulatedUnits = (int) Math.round((p / 100.0) * maxCapacity);
            String bar = buildStockBar(simulatedUnits, maxCapacity, 15);
            System.out.print("\r  " + String.format("%-18s: %s", productName, bar));
            System.out.flush();
            sleepMs(40);
        }
        System.out.println();
    }

    /**
     * Scrolling "Mall Announcements" ticker showing urgent critical alerts.
     * Uses String.substring to create a continuous marquee scroll.
     */
    public static void playScrollingTicker(String announcement, int cycles) {
        if (!animationsEnabled || announcement == null || announcement.isEmpty()) {
            System.out.println(ConsoleColors.yellow("[ANNOUNCEMENT TICKER]: " + announcement));
            return;
        }

        int windowSize = 50;
        String padded = "   *** MALL ALERT ***  " + announcement + "               ";
        int len = padded.length();

        System.out.print("\n  " + ConsoleColors.bold("LIVE TICKER: "));
        for (int c = 0; c < cycles; c++) {
            for (int i = 0; i < len; i++) {
                StringBuilder display = new StringBuilder();
                for (int j = 0; j < windowSize; j++) {
                    display.append(padded.charAt((i + j) % len));
                }
                System.out.print("\r  " + ConsoleColors.bold("LIVE TICKER: [") + 
                                 ConsoleColors.yellow(display.toString()) + 
                                 ConsoleColors.bold("]"));
                System.out.flush();
                sleepMs(50);
            }
        }
        System.out.println("\n");
    }

    /**
     * ASCII shopper that "walks" to a shop when it is restocked!
     * Animates a friendly shopper character step-by-step across the floor to the store.
     */
    public static void animateShopperWalk(String targetShopName) {
        if (!animationsEnabled) {
            System.out.println(ConsoleColors.green("  [Restock courier delivered supplies to " + targetShopName + "!]"));
            return;
        }

        String[] walkingFrames = {
            "  (o_o)7  -->",
            "  \\(o_o)/ -->",
            "  <(o_o)> -->",
            "  (^o^)/  -->"
        };

        int steps = 10;
        System.out.println(ConsoleColors.cyan("\n  Restock Courier dispatched to " + targetShopName + "..."));

        for (int i = 0; i <= steps; i++) {
            StringBuilder path = new StringBuilder();
            for (int s = 0; s < i; s++) {
                path.append(". ");
            }
            String character = walkingFrames[i % walkingFrames.length];
            System.out.print("\r  [Warehouse] " + path.toString() + character + " [" + targetShopName + "]");
            System.out.flush();
            sleepMs(120);
        }
        System.out.println("  " + ConsoleColors.green("DELIVERED! [OK]\n"));
        sleepMs(200);
    }
}
