# 🌿 StockHome — Smart Inventory Demand Predictor ("Mall of Tomorrow")

[![Live Cloud App](https://img.shields.io/badge/Live%20Demo-Vercel%20Deployment-success?style=flat&logo=vercel)](https://stockhome-sigma.vercel.app/)
[![Java](https://img.shields.io/badge/Java-8%20%7C%2011%20%7C%2017%2B-blue.svg)](https://www.java.com/)
[![SelfCheck](https://img.shields.io/badge/SelfCheck-5%2F5%20PASS-success.svg)]()
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20OOP%20%7C%20Observer%20%7C%20Mediator-orange.svg)]()
[![Course](https://img.shields.io/badge/Chitkara-25CSE0202%20Programming%20in%20Java-purple.svg)]()

> 🚀 **Live Cloud Webapp:** [https://stockhome-sigma.vercel.app/](https://stockhome-sigma.vercel.app/)
>
> **StockHome** is an enterprise-grade retail inventory demand forecasting system modeled around a high-traffic shopping complex (**"Mall of Tomorrow"**). It implements operational inventory science (14-day linear weighted moving average velocity, lead-time buffering, stockout horizon forecasting, and min-heap priority queue replenishment) across:
> 1. **Core Java Backend**: Strict OOP modeling, multithreaded simulation, custom checked exceptions, and synchronized thread-safe operations.
> 2. **Desktop Swing/AWT Graphical Frontend**: Modern Java 2D canvas with antialiased storefront rendering, animated sprite walkers, interactive charts, and observer mediation.
> 3. **Console Terminal Interface**: High-efficiency ASCII terminal menu featuring an ANSI 2D grid floor plan buffer and live ANSI animations.
> 4. **Optional Bonus Webapp (`webapp/`)**: An interactive Scandinavian architectural vector painting of *The Garden Promenade* with SVG charts and client-side simulation.

---

## 🏛️ System Architecture

```
                                  ┌─────────────────────────────────────────┐
                                  │               StockHome                 │
                                  │      Inventory Demand Predictor         │
                                  └────────────────────┬────────────────────┘
                                                       │
         ┌─────────────────────────────────────────────┼─────────────────────────────────────────────┐
         ▼                                             ▼                                             ▼
┌──────────────────┐                         ┌───────────────────┐                         ┌──────────────────┐
│  Swing / AWT GUI │                         │  Console Engine   │                         │ Optional Webapp  │
│  Desktop Client  │                         │  ANSI Terminal    │                         │  (webapp/ Demo)  │
│ (Java 2D Canvas) │                         │ (char[9][45] 2D)  │                         │ (HTML5/CSS3/JS)  │
└────────┬─────────┘                         └─────────┬─────────┘                         └────────┬─────────┘
         │                                             │                                            │
         │                                   ┌─────────┴─────────┐                                  │
         │                                   │   GuiMediator     │                                  │
         │                                   │ (Observer Pattern)│                                  │
         │                                   └─────────┬─────────┘                                  │
         │                                             │                                            │
         └─────────────────────────────────────────────┼────────────────────────────────────────────┘
                                                       ▼
                                     ┌───────────────────────────────────┐
                                     │     Core Business Logic Layer     │
                                     │       (com.mall.service)          │
                                     ├───────────────────────────────────┤
                                     │  • InventoryManager (synchronized)│
                                     │  • DemandPredictor (WMA & ROP)    │
                                     │  • SimulationService (Runnable)   │
                                     └─────────────────┬─────────────────┘
                                                       │
                                     ┌─────────────────┴─────────────────┐
                                     │       Domain Entities Layer       │
                                     │        (com.mall.model)           │
                                     ├───────────────────────────────────┤
                                     │  • Product (Abstract Base)        │
                                     │  • Perishable / NonPerishable     │
                                     │  • ShopZone (Map Encapsulation)   │
                                     │  • ReorderSuggestion (Comparable) │
                                     │  • SaleTransaction (LIFO Stack)   │
                                     └───────────────────────────────────┘
```

---

## ✨ Key Features Across All Interfaces

### 1. 🏬 Multi-Zone Retail Architecture
StockHome manages four distinct mall retail zones:
- 🍔 **Gourmet Food Court** (`FOOD`): Artisan Burgers, Truffle Pizzas, Boba Teas (*Perishable, shelf-life expiry buffers*).
- 💻 **TechNova Electronics** (`ELEC`): ANC Earbuds, Smartwatches, Keyboards (*Non-perishable, warranty & supplier lead times*).
- 👗 **Vogue & Velvet Fashion** (`FASH`): Denim Jackets, Organic Tees, Hoodies (*Apparel & seasonal replenishment*).
- 🍎 **Apex Super Grocery** (`GROC`): Organic Milk, Sourdough Loaf, Pink Salt (*Daily consumables, fast-decay buffers*).

### 2. 📐 Operations Research & Demand Forecasting Engine
- **14-Day Linear Weighted Moving Average (WMA)**:
  For $N$ days of sales history ($N \le 14$), each day $i$ from 1 to $N$ is assigned linear weight $w_i = i$:
  $$\text{WMA} = \frac{\sum_{i=1}^N (i \times \text{Sales}_i)}{\sum_{i=1}^N i} \times \text{Demand Multiplier}$$
  where $\sum_{i=1}^N i = \frac{N(N+1)}{2}$. Older days receive weight 1, while recent days receive progressively higher weights (up to $N$) to capture demand surges quickly.
- **Stockout Depletion Horizon**:
  $$\text{Days Until Stockout} = \left\lfloor \frac{\text{Current Stock}}{\text{WMA}} \right\rfloor$$
- **Reorder Point (ROP) & Safety Stock**:
  $$\text{ROP} = (\text{WMA} \times \text{LeadTime}) + \text{SafetyStock}$$
  *Perishable* goods compute safety stock factoring shelf-life decay ($\lceil \text{WMA} \times 1.5 \rceil$); *Non-Perishable* goods factor supplier transit delays ($\lceil \text{WMA} \times 3.0 \rceil$).
- **Suggested Reorder Quantity**:
  $$\text{Target Stock} = ((\text{LeadTime} + 7) \times \text{WMA}) + \text{SafetyStock}$$
  $$\text{Suggested Quantity} = \max(10, \lceil \text{Target Stock} - \text{Current Stock} \rceil)$$
- **Min-Heap Priority Queue Ranking**:
  Replenishment suggestions implement `Comparable<ReorderSuggestion>` and are evaluated by urgency:
  - 🔴 **CRITICAL**: $\le 2\text{ days remaining}$
  - 🟡 **WARNING**: $\le 5\text{ days remaining}$
  - 🟢 **SAFE**: Healthy buffer stock

### 3. 🌅 Daily Sales Tracking & "Next Day" Lifecycle
- Purchases during the active business day accumulate in `Product.todaysSales` without polluting historical averages prematurely.
- **`closeDay()`** appends `todaysSales` into `salesHistory`, resets `todaysSales` to 0, and trims `salesHistory` so it stays strictly bounded at a maximum of 14 days (discarding the oldest day).
- **`InventoryManager.nextDay()`** closes the day across all products and zones, and records an entry in the activity log.
- Available via Option 12 in the console menu and the **"NEXT DAY (CLOSE DAY)"** button in the Swing GUI.

### 4. 🖥️ Desktop Swing/AWT Graphical Frontend
- **100% Native Java 2D**: No JavaFX, no heavy third-party UI libraries. Runs seamlessly on standard JRE 8+.
- **Splash Screen**: Animated rolling mall shutter and glowing neon letters using `javax.swing.Timer`.
- **CardLayout Navigation**: Seamless switching between Map, Dashboard, Products, Reorder Queue, Reports, and Simulation.
- **Observer & Mediator Pattern**: `InventoryListener` and `GuiMediator` ensure all panels repaint synchronously on the Event Dispatch Thread (EDT) upon any stock mutation.

### 5. ⚡ Multithreaded Simulation Engine
- Background worker thread (`SimulationService` implementing `Runnable`) generates real-time customer footfall.
- Thread-safe concurrency: All stock mutations in `InventoryManager` are guarded by `synchronized` monitor locks.
- Graceful shutdown sequence using `volatile boolean running`, `workerThread.interrupt()`, and non-blocking join via helper thread.

### 6. 🌐 Live Cloud Webapp & Bonus Demo (`webapp/`)
- **Live Cloud Deployment**: [https://stockhome-sigma.vercel.app/](https://stockhome-sigma.vercel.app/) (Instant zero-install access hosted on Vercel).
- **Local Access**: Open `index.html` or `open_webapp.bat` directly in the project root folder or within `webapp/`.
- **Client-Side Persistence (`localStorage`)**: All purchases, restocks, custom registered SKUs, daily sales, and next-day progressions are automatically saved to browser `localStorage` (`stockhome_inventory_state_v2`). The state automatically persists across browser tab reloads or revisits, with a single-click **"Reset Demo"** button to restore initial factory demo defaults.
- **Mathematical Consistency**: Shares identical 14-day linear WMA, stockout depletion horizon, lead-time buffering, and ROP formulas matching the Java core.

---

## 🚀 Quickstart & How to Run

### Option A: Run Built-in SelfCheck Suite
Verifies all mathematical invariants, undo transactions, and day-closing boundaries:
```bash
# Compile
javac -d bin src\com\mall\*.java src\com\mall\exceptions\*.java src\com\mall\model\*.java src\com\mall\service\*.java src\com\mall\ui\*.java src\com\mall\ui\gui\*.java src\com\mall\util\*.java

# Execute SelfCheck
java -cp bin com.mall.util.SelfCheck
```

---

### Option B: Java Desktop GUI (Swing/AWT)
Requirements: JDK 8 or higher.
- **Windows**: Double-click `run.bat` (defaults to GUI mode).
- **Command Line**:
  ```bash
  java -cp bin com.mall.Main --gui
  ```
- **Custom Event Multiplier**:
  ```bash
  java -cp bin com.mall.Main --gui --event 1.5
  ```

---

### Option C: Java Console Terminal Interface
Runs the interactive ASCII terminal with ANSI colors, 2D floor plan rendering, and keyboard menus:
- **Command Line**:
  ```bash
  java -cp bin com.mall.Main
  ```

---

### Option D: Live Cloud Webapp & Local Browser Demo
- **Live Cloud App**: Open [https://stockhome-sigma.vercel.app/](https://stockhome-sigma.vercel.app/) in any web browser.
- **Local Offline Launch**: Double-click `open_webapp.bat` or open `index.html` directly in the project root folder.

---

## 📂 Project Directory Structure

```
Smart Inventory/
├── src/                                  # Java Core Backend & UI Source
│   └── com/
│       └── mall/
│           ├── Main.java                 # Universal launcher (Console, GUI, and --event flag)
│           ├── model/                    # Domain Layer (Lect. 1, 5, 8)
│           │   ├── Product.java          # Abstract base entity (todaysSales, closeDay, WMA)
│           │   ├── PerishableProduct.java# Subclass (shelf-life polymorphism)
│           │   ├── NonPerishableProduct.java# Subclass (lead-time polymorphism)
│           │   ├── Predictable.java      # Interface for predictive math
│           │   ├── ShopZone.java         # Zone container (Map encapsulation)
│           │   ├── ReorderSuggestion.java# Comparable Min-Heap DTO
│           │   ├── SaleTransaction.java  # Immutable transaction record
│           │   └── UrgencyLevel.java     # Urgency enum (CRITICAL/WARNING/SAFE)
│           ├── service/                  # Business Logic Layer (Lect. 6, 9-16)
│           │   ├── InventoryManager.java # Thread-safe CRUD, LinkedHashMap, Deque undo (cap 20), nextDay()
│           │   ├── DemandPredictor.java  # 14-day WMA, 2D matrix (int[4][14]), ROP math
│           │   └── SimulationService.java# Runnable worker thread, volatile fields, safe shutdown
│           ├── exceptions/               # Checked Exception Hierarchy (Lect. 3)
│           │   ├── OutOfStockException.java
│           │   ├── InvalidQuantityException.java
│           │   └── ProductNotFoundException.java
│           ├── util/
│           │   ├── DataSeeder.java       # Seeds 16 products across 4 zones with 14-day history
│           │   └── SelfCheck.java        # Assertion test suite (5/5 PASS)
│           └── ui/                       # Presentation Layers
│               ├── AnsiTheme.java        # Terminal colors & fallback
│               ├── ConsoleColors.java    # ANSI color formatters
│               ├── ConsoleAnimation.java # Shutter & typing animations
│               ├── MallMapRenderer.java  # char[9][45] 2D floor plan buffer
│               ├── MenuController.java   # Defensive console loop & Next Day option
│               └── gui/                  # Swing/AWT Desktop Application
│                   ├── GuiMain.java      # EDT launcher via SwingUtilities
│                   ├── MainFrame.java    # CardLayout desktop window shell
│                   ├── SplashScreen.java # Animated rolling shutter splash
│                   ├── Theme.java        # Color theory, card painting helpers
│                   ├── GuiMediator.java  # Mediator pattern & non-blocking simulation stop
│                   ├── InventoryListener.java # Observer interface
│                   ├── SidebarPanel.java # Navigation sidebar
│                   ├── TickerPanel.java  # Marquee critical alert banner
│                   ├── MallMapPanel.java # Graphics2D storefronts & walkers
│                   ├── DashboardPanel.java# Native Java 2D Bar & Line charts
│                   ├── ProductsPanel.java# JTable with animated progress bars
│                   ├── ReorderPanel.java # PriorityQueue interactive restock
│                   ├── ReportsPanel.java # Multiplier slider & analytical tabs
│                   └── SimulationPanel.java# Live footfall simulation control & Next Day button
├── webapp/                               # Optional Bonus Web Application Demo
│   ├── index.html                        # Standalone Web Application
│   ├── style.css                         # Calm Scandinavian styling & SVG layout
│   ├── app.js                            # Web engine, SVG charts, 14-day WMA & ROP math
│   ├── open_webapp.bat                   # 1-click Windows Webapp launcher
│   └── open_webapp.sh                    # 1-click Unix Webapp launcher
├── open_webapp.bat                       # Root convenience launcher for webapp
├── run.bat                               # 1-click Windows Java GUI launcher
├── run.sh                                # 1-click Unix Java GUI launcher
├── .gitignore                            # Clean git exclusion rules
└── README.md                             # Comprehensive project documentation
```

---

## 🎓 Academic Alignment (Chitkara 25CSE0202)

| Lecture & Topic | Java Feature | Implementation in StockHome |
| :--- | :--- | :--- |
| **Lect. 1: OOP** | Inheritance, Polymorphism, Abstraction | `Product` base class; `PerishableProduct` & `NonPerishableProduct` override `calculateSafetyStock()`. |
| **Lect. 2: Strings & Arrays** | Multi-Dimensional Arrays, StringBuilder | `int[4][14]` sales matrix in `DemandPredictor`; `char[9][45]` 2D ASCII grid in `MallMapRenderer`; `StringBuilder` in formatting loops. |
| **Lect. 3: Exceptions** | Custom Checked Exceptions | `OutOfStockException`, `InvalidQuantityException`, `ProductNotFoundException` caught with graceful UI dialogs. |
| **Lect. 4: Validation** | Console input defensive handling | Robust `Scanner` parsing and input recovery in `MenuController`. |
| **Lect. 5-7: Collections** | ArrayList, HashMap, LinkedHashMap, PriorityQueue, ArrayDeque, Iterator | `ArrayList` (14-day sales), `HashMap` (SKU lookup), `LinkedHashMap` (zone order), `PriorityQueue` (Min-heap reorders), `ArrayDeque` (undo stack capped at 20). |
| **Lect. 8: Sorting** | Comparable & Comparator | `Comparable` on `ReorderSuggestion` (urgency ranking); custom `Comparator`s on Top Sellers & Dead Stock. |
| **Lect. 9-16: Multithreading** | Runnable, Synchronization, Thread Safety | `SimulationService` worker thread; `synchronized` monitor locks on `InventoryManager`; non-blocking EDT dispatch; clean `interrupt()`/`join()` shutdown. |
