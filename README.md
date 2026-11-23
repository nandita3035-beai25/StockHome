# 🌿 StockHome — Smart Inventory Demand Predictor ("Mall of Tomorrow")

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011%20%7C%2017%2B-blue.svg)](https://www.java.com/)
[![Webapp](https://img.shields.io/badge/Webapp-Zero--Dependency%20HTML5%2FCSS3%2FJS-success.svg)](open_webapp.bat)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20OOP%20%7C%20Observer%20%7C%20Mediator-orange.svg)]()
[![Course](https://img.shields.io/badge/Chitkara-25CSE0202%20Programming%20in%20Java-purple.svg)]()

> **StockHome** is an enterprise-grade retail inventory demand forecasting system modeled around a high-traffic lifestyle shopping center (**"Mall of Tomorrow"**). It combines operational inventory science (weighted velocity, lead-time buffering, stockout horizon forecasting, and min-heap replenishment) with three presentation layers:
> 1. **Standalone Web Application**: A calm, soothing Scandinavian architectural painting of *The Garden Promenade* with vector SVG analytics, live simulation, and zero build setup.
> 2. **Desktop Swing/AWT Graphical Frontend**: Modern Java 2D canvas with antialiased storefront rendering, animated sprite walkers, interactive charts, and observer mediation.
> 3. **Console Terminal Interface**: A high-efficiency ASCII terminal menu featuring an ANSI 2D grid floor plan buffer and live ANSI animations.

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
│  Standalone Web  │                         │  Swing / AWT GUI  │                         │  Console Engine  │
│  The Promenade   │                         │  Desktop Client   │                         │  ANSI Terminal   │
│ (HTML5/CSS3/JS)  │                         │  (Java 2D Canvas) │                         │ (char[9][45] 2D) │
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
- **3-Day Weighted Moving Average (ADS)**:
  $$\text{ADS} = \frac{1 \cdot D_{t-2} + 2 \cdot D_{t-1} + 3 \cdot D_{t}}{1 + 2 + 3} \times \text{Multiplier}$$
  Weights recent days progressively higher ($[1, 2, 3]$) to capture demand velocity immediately.
- **Stockout Depletion Horizon**:
  $$\text{Days Until Stockout} = \left\lfloor \frac{\text{Current Stock}}{\text{ADS}} \right\rfloor$$
- **Reorder Point (ROP) & Safety Stock**:
  $$\text{ROP} = (\text{ADS} \times \text{LeadTime}) + \text{SafetyStock}$$
  *Perishable* goods compute safety stock factoring shelf-life decay; *Non-Perishable* goods factor supplier transit delays.
- **Min-Heap Priority Queue Ranking**:
  Replenishment suggestions implement `Comparable<ReorderSuggestion>` and are evaluated by urgency:
  - 🔴 **CRITICAL**: $\le 2\text{ days remaining}$
  - 🟡 **WARNING**: $\le 5\text{ days remaining}$
  - 🟢 **SAFE**: Healthy buffer stock

### 3. 🌿 The Garden Promenade (Modern Webapp)
- **Serene Scandinavian Aesthetic**: Calm sage, warm slate, crisp cream, and forest green palette with clean typography and zero blurry glow filters.
- **Layered 2D Architectural Vector Painting**: An architectural SVG composition of "The Garden Promenade" with striped boutique awnings, arched glass clerestory, planter boxes, ficus trees, and walking visitors.
- **Zero-Dependency Vector Visualizations**: Pure SVG donut gauges, depletion bar charts, and 14-day area trend curves.
- **Built-in Web Audio Feedback**: Soft acoustic chimes, wooden tap effects, and cash register tones synthesized natively via the browser's Web Audio API (no external MP3/WAV files).

### 4. 🖥️ Desktop Swing/AWT Graphical Frontend
- **100% Native Java 2D**: No JavaFX, no heavy third-party UI libraries. Runs seamlessly on standard JRE 8+.
- **Splash Screen**: Animated rolling mall shutter and glowing neon letters using `javax.swing.Timer`.
- **CardLayout Navigation**: Seamless switching between Map, Dashboard, Products, Reorder Queue, Reports, and Simulation.
- **Observer & Mediator Pattern**: `InventoryListener` and `GuiMediator` ensure all panels repaint synchronously on the Event Dispatch Thread (EDT) upon any stock mutation.

### 5. ⚡ Multithreaded Simulation Engine
- Background worker thread (`SimulationService` implementing `Runnable`) generates real-time customer footfall.
- Thread-safe concurrency: All stock mutations in `InventoryManager` are guarded by `synchronized` monitor locks.
- Graceful shutdown sequence using `volatile boolean running`, `workerThread.interrupt()`, and `workerThread.join(2000)`.

---

## 🚀 Quickstart & How to Run

### Option A: Standalone Web Application (Zero Setup / Instant)
No compiler or web server needed. Double-click to launch in any modern browser:
- **Windows**: Double-click `open_webapp.bat` or open `index.html`.
- **macOS / Linux**: Open `index.html` in your browser or run:
  ```bash
  chmod +x open_webapp.sh
  ./open_webapp.sh
  ```

---

### Option B: Java Desktop GUI (Swing/AWT)
Requirements: JDK 8 or higher.
- **Windows**: Double-click `run.bat` (defaults to GUI mode).
- **Command Line**:
  ```bash
  # Compile
  javac -d bin -sourcepath src src/com/mall/Main.java

  # Run GUI
  java -cp bin com.mall.Main --gui
  ```

---

### Option C: Java Console Terminal Interface
Runs the interactive ASCII terminal with ANSI colors, 2D floor plan rendering, and keyboard menus:
- **Command Line**:
  ```bash
  java -cp bin com.mall.Main
  ```

---

## 📂 Project Directory Structure

```
Smart Inventory/
├── src/                                  # Java Core Backend & UI Source
│   └── com/
│       └── mall/
│           ├── Main.java                 # Universal launcher (Console & GUI flag)
│           ├── model/                    # Domain Layer (Lect. 1, 5, 8)
│           │   ├── Product.java          # Abstract base entity
│           │   ├── PerishableProduct.java# Subclass (shelf-life polymorphism)
│           │   ├── NonPerishableProduct.java# Subclass (lead-time polymorphism)
│           │   ├── Predictable.java      # Interface for predictive math
│           │   ├── ShopZone.java         # Zone container (Map encapsulation)
│           │   ├── ReorderSuggestion.java# Comparable Min-Heap DTO
│           │   ├── SaleTransaction.java  # Immutable transaction record
│           │   └── UrgencyLevel.java     # Urgency enum (CRITICAL/WARNING/SAFE)
│           ├── service/                  # Business Logic Layer (Lect. 6, 9-16)
│           │   ├── InventoryManager.java # Thread-safe CRUD, LinkedHashMap, Deque undo
│           │   ├── DemandPredictor.java  # WMA, 2D matrix (int[4][14]), ROP math
│           │   └── SimulationService.java# Runnable worker thread, safe shutdown
│           ├── exception/                # Checked Exception Hierarchy (Lect. 3)
│           │   ├── OutOfStockException.java
│           │   ├── InvalidQuantityException.java
│           │   └── ProductNotFoundException.java
│           ├── util/
│           │   └── DataSeeder.java       # Seeds 12+ products with 14-day history
│           └── ui/                       # Presentation Layers
│               ├── AnsiTheme.java        # Terminal colors & fallback
│               ├── MallMapRenderer.java  # char[9][45] 2D floor plan buffer
│               ├── MenuController.java   # Defensive console loop & validation
│               └── gui/                  # Swing/AWT Desktop Application
│                   ├── GuiMain.java      # EDT launcher via SwingUtilities
│                   ├── MainFrame.java    # CardLayout desktop window shell
│                   ├── SplashScreen.java # Animated rolling shutter splash
│                   ├── Theme.java        # Color theory, card painting helpers
│                   ├── GuiMediator.java  # Mediator pattern for EDT dispatches
│                   ├── InventoryListener.java # Observer interface
│                   ├── SidebarPanel.java # Navigation sidebar
│                   ├── TickerPanel.java  # Marquee critical alert banner
│                   ├── MallMapPanel.java # Graphics2D storefronts & walkers
│                   ├── DashboardPanel.java# Native Java 2D Bar & Line charts
│                   ├── ProductsPanel.java# JTable with animated progress bars
│                   ├── ReorderPanel.java # PriorityQueue interactive restock
│                   ├── ReportsPanel.java # Multiplier slider & analytical tabs
│                   └── SimulationPanel.java# Live footfall simulation control
├── index.html                            # Standalone Web Application
├── style.css                             # Calm Scandinavian styling & SVG layout
├── app.js                                # Web engine, SVG charts, Web Audio
├── open_webapp.bat                       # 1-click Windows Webapp launcher
├── open_webapp.sh                        # 1-click Unix Webapp launcher
├── run.bat                               # 1-click Windows Java GUI launcher
├── run.sh                                # 1-click Unix Java GUI launcher
└── README.md                             # Project documentation
```

---

## 🎓 Academic Alignment (Chitkara 25CSE0202)

| Lecture & Topic | Java Feature | Implementation in StockHome |
| :--- | :--- | :--- |
| **Lect. 1: OOP** | Inheritance, Polymorphism, Abstraction | `Product` base class; `PerishableProduct` & `NonPerishableProduct` override `calculateSafetyStock()`. |
| **Lect. 2: Strings & Arrays** | Multi-Dimensional Arrays, StringBuilder | `int[4][14]` sales matrix in `DemandPredictor`; `char[9][45]` 2D ASCII grid in `MallMapRenderer`; `StringBuilder` in formatting loops. |
| **Lect. 3: Exceptions** | Custom Checked Exceptions | `OutOfStockException`, `InvalidQuantityException`, `ProductNotFoundException` caught with graceful UI dialogs. |
| **Lect. 4: Validation** | Console input defensive handling | Robust `Scanner` parsing and input recovery in `MenuController`. |
| **Lect. 5-7: Collections** | ArrayList, HashMap, LinkedHashMap, PriorityQueue, ArrayDeque, Iterator | `ArrayList` (14-day sales), `HashMap` (SKU lookup), `LinkedHashMap` (zone order), `PriorityQueue` (Min-heap reorders), `ArrayDeque` (undo stack). |
| **Lect. 8: Sorting** | Comparable & Comparator | `Comparable` on `ReorderSuggestion` (urgency ranking); custom `Comparator`s on Top Sellers & Dead Stock. |
| **Lect. 9-16: Multithreading** | Runnable, Synchronization, Thread Safety | `SimulationService` worker thread; `synchronized` monitor locks on `InventoryManager`; non-blocking EDT dispatch; clean `interrupt()`/`join()` shutdown. |

---

## 👥 Contributors & Academic Viva Preparation

- Comprehensive **Viva Voce Cheat Sheet** (`Viva_Cheat_Sheet.pdf`) and **Component & Concept Mapping Guide** (`StockHome_What_Is_Used_Where_Guide.pdf`) are available as separate deliverables on your Desktop for viva examination preparation.
- Developed for **Programming in Java (25CSE0202)**, Chitkara University.
