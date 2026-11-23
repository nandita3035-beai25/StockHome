package com.mall.ui;

import com.mall.model.Product;
import com.mall.model.ShopZone;
import com.mall.model.UrgencyLevel;

import java.util.Map;

/**
 * ASCII Mall Floorplan visualizer.
 * 
 * Demonstrates:
 * - Multi-dimensional Arrays: 2D char array (char[][]) for floor plan rendering
 * - Nested loops for matrix population and transformation
 * - Dynamic color mapping based on aggregated zone stock health:
 *     Green  = SAFE (healthy stock)
 *     Yellow = WARNING (reorder soon)
 *     Red    = CRITICAL (imminent stockout)
 */
public class MallMapRenderer {

    private final Map<String, ShopZone> zones;

    public MallMapRenderer(Map<String, ShopZone> zones) {
        this.zones = zones;
    }

    /**
     * Renders the full 2D ASCII floor map of Mall of Tomorrow.
     */
    public void renderMap() {
        System.out.println("\n" + ConsoleColors.cyan("================================================================================"));
        System.out.println(ConsoleColors.bold("                 MALL OF TOMORROW - 2D FLOOR PLAN & HEALTH MAP                 "));
        System.out.println(ConsoleColors.cyan("================================================================================"));

        ShopZone foodZone = zones.get("FOOD");
        ShopZone elecZone = zones.get("ELEC");
        ShopZone fashZone = zones.get("FASH");
        ShopZone grocZone = zones.get("GROC");

        // Format health badges
        String foodHealth = formatHealthTag(foodZone != null ? foodZone.getZoneStockHealth() : UrgencyLevel.SAFE);
        String elecHealth = formatHealthTag(elecZone != null ? elecZone.getZoneStockHealth() : UrgencyLevel.SAFE);
        String fashHealth = formatHealthTag(fashZone != null ? fashZone.getZoneStockHealth() : UrgencyLevel.SAFE);
        String grocHealth = formatHealthTag(grocZone != null ? grocZone.getZoneStockHealth() : UrgencyLevel.SAFE);

        // Display 2D Zone Layout
        System.out.println("  +-------------------------------------+  +-------------------------------------+");
        System.out.println("  |           ZONE A: FOOD COURT        |  |        ZONE B: TECH & ELECTRONICS   |");
        System.out.printf("  | Status: %-27s |  | Status: %-27s |\n", foodHealth, elecHealth);
        System.out.printf("  | Total Stock: %-3d items | Items: %-3d  |  | Total Stock: %-3d items | Items: %-3d  |\n",
                foodZone != null ? foodZone.getTotalStockCount() : 0,
                foodZone != null ? foodZone.getProductCount() : 0,
                elecZone != null ? elecZone.getTotalStockCount() : 0,
                elecZone != null ? elecZone.getProductCount() : 0);
        System.out.println("  +-------------------------------------+  +-------------------------------------+");
        System.out.println("                     ||                                      ||                   ");
        System.out.println("                     || ======= CENTRAL ATRIUM ======= ||                   ");
        System.out.println("                     ||        [Escalators & Fountains]       ||                   ");
        System.out.println("                     || ============================== ||                   ");
        System.out.println("                     ||                                      ||                   ");
        System.out.println("  +-------------------------------------+  +-------------------------------------+");
        System.out.println("  |          ZONE C: FASHION HUB        |  |         ZONE D: SUPER GROCERY       |");
        System.out.printf("  | Status: %-27s |  | Status: %-27s |\n", fashHealth, grocHealth);
        System.out.printf("  | Total Stock: %-3d items | Items: %-3d  |  | Total Stock: %-3d items | Items: %-3d  |\n",
                fashZone != null ? fashZone.getTotalStockCount() : 0,
                fashZone != null ? fashZone.getProductCount() : 0,
                grocZone != null ? grocZone.getTotalStockCount() : 0,
                grocZone != null ? grocZone.getProductCount() : 0);
        System.out.println("  +-------------------------------------+  +-------------------------------------+");
        System.out.println("                                [ MAIN ENTRANCE ]                                ");
        System.out.println("================================================================================");

        // Render stock health legend
        System.out.print("  LEGEND: ");
        System.out.print(ConsoleColors.green("[SAFE > 5 Days]   "));
        System.out.print(ConsoleColors.yellow("[WARNING <= 5 Days]   "));
        System.out.println(ConsoleColors.red("[CRITICAL <= 2 Days]"));
        System.out.println();
    }

    /**
     * Demonstrates explicit 2D char array grid manipulation.
     * Generates a 2D matrix representing store stalls and pedestrian walkways.
     */
    public void print2DGridArchitecture() {
        int rows = 9;
        int cols = 45;
        // Multi-dimensional array declaration:
        char[][] grid = new char[rows][cols];

        // Fill grid with walkway dots
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = ' ';
            }
        }

        // Draw outer perimeter border
        for (int c = 0; c < cols; c++) {
            grid[0][c] = '=';
            grid[rows - 1][c] = '=';
        }
        for (int r = 0; r < rows; r++) {
            grid[r][0] = '|';
            grid[r][cols - 1] = '|';
        }

        // Place 4 shops into 2D array cells
        stampShop(grid, 1, 3, "FOOD_COURT");
        stampShop(grid, 1, 26, "ELECTRONICS");
        stampShop(grid, 5, 3, "FASHION_HUB");
        stampShop(grid, 5, 26, "SUPER_GROC");

        // Stamp central fountain walkway
        stampShop(grid, 3, 17, "[ATRIUM]");

        // Print 2D matrix
        System.out.println(ConsoleColors.purple("  --- 2D ARCHITECTURAL GRID MATRIX ---"));
        for (int r = 0; r < rows; r++) {
            System.out.print("  ");
            for (int c = 0; c < cols; c++) {
                char ch = grid[r][c];
                if (ch == '=') System.out.print(ConsoleColors.cyan(String.valueOf(ch)));
                else if (ch == '|') System.out.print(ConsoleColors.cyan(String.valueOf(ch)));
                else if (ch == '[') System.out.print(ConsoleColors.yellow(String.valueOf(ch)));
                else if (ch == ']') System.out.print(ConsoleColors.yellow(String.valueOf(ch)));
                else System.out.print(ch);
            }
            System.out.println();
        }
        System.out.println();
    }

    private void stampShop(char[][] grid, int startRow, int startCol, String label) {
        String box = "[" + label + "]";
        for (int i = 0; i < box.length() && (startCol + i) < grid[0].length - 1; i++) {
            grid[startRow][startCol + i] = box.charAt(i);
        }
    }

    private String formatHealthTag(UrgencyLevel health) {
        switch (health) {
            case CRITICAL:
                return ConsoleColors.red("[!] CRITICAL");
            case WARNING:
                return ConsoleColors.yellow("[*] WARNING ");
            case SAFE:
            default:
                return ConsoleColors.green("[v] SAFE    ");
        }
    }
}
