package com.mall.ui;

/**
 * ANSI Escape Code utility providing colors and text styles for terminal output.
 * 
 * Includes a global toggle flag (ansiEnabled) to provide full fallback capability
 * for older terminal environments (e.g. legacy Windows CMD) where ANSI codes
 * are unsupported.
 * 
 * Demonstrates:
 * - OOP: Utility class with private constructor, static fields, and methods
 * - Strings & String formatting
 */
public final class ConsoleColors {

    // Toggle flag for ANSI support fallback
    private static boolean ansiEnabled = true;

    // Reset code
    public static final String RESET = "\u001B[0m";

    // Regular Colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Bold Colors
    public static final String BOLD = "\u001B[1m";
    public static final String BOLD_RED = "\u001B[1;31m";
    public static final String BOLD_GREEN = "\u001B[1;32m";
    public static final String BOLD_YELLOW = "\u001B[1;33m";
    public static final String BOLD_BLUE = "\u001B[1;34m";
    public static final String BOLD_PURPLE = "\u001B[1;35m";
    public static final String BOLD_CYAN = "\u001B[1;36m";
    public static final String BOLD_WHITE = "\u001B[1;37m";

    // Background Colors
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_CYAN = "\u001B[46m";

    // Private constructor prevents instantiation of utility class
    private ConsoleColors() {
    }

    public static boolean isAnsiEnabled() {
        return ansiEnabled;
    }

    public static void setAnsiEnabled(boolean enabled) {
        ansiEnabled = enabled;
    }

    /**
     * Colorizes the given text string using the specified ANSI color code.
     * If ANSI colors are disabled via the fallback toggle, returns plain text.
     */
    public static String colorize(String text, String colorCode) {
        if (!ansiEnabled || colorCode == null || text == null) {
            return text;
        }
        return colorCode + text + RESET;
    }

    public static String red(String text) {
        return colorize(text, BOLD_RED);
    }

    public static String green(String text) {
        return colorize(text, BOLD_GREEN);
    }

    public static String yellow(String text) {
        return colorize(text, BOLD_YELLOW);
    }

    public static String cyan(String text) {
        return colorize(text, BOLD_CYAN);
    }

    public static String blue(String text) {
        return colorize(text, BOLD_BLUE);
    }

    public static String purple(String text) {
        return colorize(text, BOLD_PURPLE);
    }

    public static String bold(String text) {
        return colorize(text, BOLD);
    }
}
