# StockHome — Smart Inventory Demand Predictor

A Java desktop and terminal inventory management system modeled around a shopping mall ("Mall of Tomorrow"). Built for Chitkara University's Java course (25CSE0202).

It tracks stock across 4 retail zones, predicts depletion using a 14-day weighted moving average, and automatically flags items needing reorder before they run out.

- **Live Web Demo:** https://stockhome-sigma.vercel.app/
- **Requirements:** Java 8 or higher (no external libraries needed)

---

## How to Run

### 1. Run the Swing GUI (Default)
On Windows, double-click `run.bat`, or compile and launch from the terminal:
```bash
# Compile
javac -d bin src/com/mall/*.java src/com/mall/*/*.java src/com/mall/ui/gui/*.java

# Run Desktop GUI
java -cp bin com.mall.Main --gui
```

### 2. Run the Console / Terminal Interface
```bash
java -cp bin com.mall.Main
```

### 3. Run Self-Verification Tests
Asserts math formulas, undo stack, and 14-day rolling boundaries:
```bash
java -cp bin com.mall.util.SelfCheck
```

### 4. Open the Web App
Open `index.html` in your browser, or visit the [live deployment](https://stockhome-sigma.vercel.app/).

---

## Core Features

- **4 Retail Zones:** Food Court (perishable), Electronics (non-perishable), Fashion, and Grocery.
- **Demand Forecasting:**
  - **14-Day Weighted Moving Average (WMA):** Recent days carry higher weight (Day 1 weight = 1, Day 14 weight = 14, sum of weights = 105).
  - **Days to Stockout:** `Current Stock / Daily Demand Rate`.
  - **Reorder Point (ROP):** `(Daily Demand * Lead Time) + Safety Stock`.
  - **Reorder Priority Queue:** Min-heap (`PriorityQueue`) sorting items into CRITICAL (<= 2 days), WARNING (<= 5 days), or SAFE.
- **Next Day / Close Day Action:** Sales during the day accumulate in `todaysSales`. Advancing the day appends `todaysSales` into history, trims history to 14 days, and resets the daily counter.
- **Undo Transactions:** LIFO undo stack (capped at 20) restoring both shelf stock and today's sales.
- **Footfall Simulation:** Background thread (`SimulationService`) simulating random customer purchases in real time.
- **Custom Event Multipliers:** Adjust demand surge via GUI slider or CLI flag (e.g. `--event 1.5` for holidays/festivals).

---

## Project Structure

```
Smart Inventory/
├── src/com/mall/
│   ├── Main.java               # App launcher (routes to GUI or Console)
│   ├── model/                  # Product, Perishable/NonPerishable, ShopZone, ReorderSuggestion
│   ├── service/                # InventoryManager, DemandPredictor, SimulationService
│   ├── exceptions/             # OutOfStockException, InvalidQuantityException, ProductNotFoundException
│   ├── ui/                     # MenuController, MallMapRenderer, ConsoleColors, ConsoleAnimation
│   │   └── gui/                # Swing GUI (MainFrame, DashboardPanel, ProductsPanel, etc.)
│   └── util/                   # DataSeeder (16 sample items), SelfCheck (test suite)
├── webapp/                     # Browser demo (HTML, CSS, JS)
├── index.html                  # Root webapp entry point
├── run.bat                     # Windows launch script
└── README.md
```

---

## Java Concepts Demonstrated

- **OOP:** Abstract base class (`Product`), polymorphic subclasses (`PerishableProduct`, `NonPerishableProduct`), interface implementation (`Predictable`, `Comparable`).
- **Data Structures & Collections:** `LinkedHashMap` for zone ordering, `HashMap` for fast SKU lookup, `PriorityQueue` for reorder urgency ranking, `ArrayDeque` for undo stack.
- **Multithreading:** Background simulation worker implementing `Runnable`, thread synchronization (`synchronized`), `volatile` counters, and clean shutdown without blocking the Swing Event Dispatch Thread (EDT).
- **Custom Exceptions:** Checked exceptions (`OutOfStockException`, `InvalidQuantityException`, `ProductNotFoundException`) handled with user-friendly recovery.
- **GUI Programming:** Native Swing & Java 2D canvas, observer/mediator pattern for syncing UI panels upon inventory mutations.
