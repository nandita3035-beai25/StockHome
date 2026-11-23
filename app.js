/**
 * StockHome — Calm & Soothing 2D Illustrated Promenade Studio
 * Pure Vector Graphics (SVG), Razor-Sharp Charts, and Accurate Forecasting Math
 */

(function () {
  'use strict';

  // =========================================================================
  // 1. DATA MODEL & SEEDED CATALOG
  // =========================================================================

  const BOUTIQUES = {
    FOOD: { code: 'FOOD', name: 'The Botanical Cafe', category: 'Artisan Food & Bakery', color: '#D9776E', icon: '🥐', node: 'shopNodeFood' },
    ELEC: { code: 'ELEC', name: 'Studio Nordics Tech', category: 'Acoustics & Workspace', color: '#456990', icon: '🎧', node: 'shopNodeElec' },
    FASH: { code: 'FASH', name: 'Maison Atelier', category: 'Linen & Apparel', color: '#8E6582', icon: '🧥', node: 'shopNodeFash' },
    GROC: { code: 'GROC', name: 'The Rustic Harvest', category: 'Organic Essentials', color: '#4A7C59', icon: '🥖', node: 'shopNodeGroc' }
  };

  // 16 seeded products with 14 days of realistic sales histories (Indian Rupee Pricing)
  const INITIAL_PRODUCTS = [
    // FOOD COURT - Botanical Cafe (Perishables)
    { id: 'F101', name: 'Artisan Brioche Burger', price: 299.00, stock: 35, zone: 'FOOD', isPerishable: true, shelfLife: 2, icon: '🍔', history: [25, 28, 30, 27, 32, 35, 40, 38, 42, 45, 48, 50, 52, 55] },
    { id: 'F102', name: 'Truffle Mushroom Tart', price: 499.00, stock: 80, zone: 'FOOD', isPerishable: true, shelfLife: 3, icon: '🍕', history: [10, 12, 11, 14, 12, 15, 13, 16, 14, 15, 17, 18, 19, 20] },
    { id: 'F103', name: 'Sparkling Jasmine Tea', price: 180.00, stock: 240, zone: 'FOOD', isPerishable: true, shelfLife: 4, icon: '🧋', history: [15, 16, 15, 18, 17, 20, 19, 22, 21, 20, 24, 25, 23, 26] },
    { id: 'F104', name: 'Cardamom Fig Mousse', price: 220.00, stock: 50, zone: 'FOOD', isPerishable: true, shelfLife: 5, icon: '🍰', history: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] },

    // ELECTRONICS - Studio Nordics (Non-Perishables)
    { id: 'E201', name: 'Walnut ANC Headphones', price: 3499.00, stock: 20, zone: 'ELEC', isPerishable: false, warranty: 24, icon: '🎧', history: [8, 9, 10, 12, 11, 14, 15, 16, 18, 20, 22, 25, 24, 28] },
    { id: 'E202', name: 'Minimalist E-Ink Watch', price: 4999.00, stock: 110, zone: 'ELEC', isPerishable: false, warranty: 24, icon: '⌚', history: [4, 5, 4, 6, 5, 7, 6, 8, 7, 8, 9, 8, 10, 10] },
    { id: 'E203', name: 'Brass Mechanical Keyboard', price: 2899.00, stock: 35, zone: 'ELEC', isPerishable: false, warranty: 12, icon: '⌨️', history: [3, 4, 5, 4, 6, 5, 7, 6, 8, 7, 8, 9, 8, 10] },
    { id: 'E204', name: 'Braided Audio Adapter', price: 399.00, stock: 75, zone: 'ELEC', isPerishable: false, warranty: 6, icon: '🔌', history: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] },

    // FASHION - Maison Atelier (Non-Perishables)
    { id: 'S301', name: 'Washed Linen Overshirt', price: 1999.00, stock: 15, zone: 'FASH', isPerishable: false, warranty: 6, icon: '🧥', history: [5, 6, 7, 8, 7, 9, 8, 10, 11, 12, 11, 13, 14, 15] },
    { id: 'S302', name: 'Organic Raw Cotton Tee', price: 699.00, stock: 190, zone: 'FASH', isPerishable: false, warranty: 3, icon: '👕', history: [10, 12, 14, 15, 13, 16, 18, 17, 19, 20, 22, 21, 23, 25] },
    { id: 'S303', name: 'Merino Wool Knit Hoodie', price: 2499.00, stock: 25, zone: 'FASH', isPerishable: false, warranty: 6, icon: '🧶', history: [2, 3, 2, 4, 3, 5, 4, 5, 6, 5, 6, 7, 6, 8] },
    { id: 'S304', name: 'Pleated Corduroy Trousers', price: 1499.00, stock: 55, zone: 'FASH', isPerishable: false, warranty: 3, icon: '👖', history: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] },

    // GROCERY - The Rustic Harvest (Perishable & Non-Perishable mix)
    { id: 'G401', name: 'Grass-Fed A2 Farm Milk', price: 75.00, stock: 45, zone: 'GROC', isPerishable: true, shelfLife: 5, icon: '🥛', history: [30, 32, 35, 38, 40, 42, 45, 48, 50, 52, 55, 58, 60, 65] },
    { id: 'G402', name: 'Ancient Grain Sourdough', price: 140.00, stock: 95, zone: 'GROC', isPerishable: true, shelfLife: 3, icon: '🥖', history: [12, 14, 15, 16, 18, 17, 20, 19, 22, 24, 23, 25, 27, 30] },
    { id: 'G403', name: 'Coarse Mineral Sea Salt', price: 95.00, stock: 160, zone: 'GROC', isPerishable: false, warranty: 36, icon: '🧂', history: [3, 4, 3, 5, 4, 5, 4, 6, 5, 6, 5, 7, 6, 7] },
    { id: 'G404', name: 'Roasted Botanical Seeds', price: 120.00, stock: 65, zone: 'GROC', isPerishable: true, shelfLife: 60, icon: '🌾', history: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] }
  ];

  // Storage and State Management
  const STORAGE_KEY = 'stockhome_inventory_state_v2';

  function createFreshState() {
    return {
      products: INITIAL_PRODUCTS.map(p => ({
        ...p,
        history: [...p.history],
        todaysSales: 0
      })),
      currentDay: 1,
      undoStack: [],
      demandMultiplier: 1.0,
      soundEnabled: true,
      twilightMode: false,
      activeTab: 'tab-painted-mall',
      activeReportSub: 'tab-top-sellers',
      simulation: {
        active: false,
        intervalId: null,
        intervalMs: 1200,
        ticks: 0,
        purchases: 0,
        stockouts: 0
      }
    };
  }

  function loadPersistedState() {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed && Array.isArray(parsed.products) && parsed.products.length > 0) {
          const fresh = createFreshState();
          return {
            ...fresh,
            products: parsed.products.map(p => ({
              ...p,
              history: Array.isArray(p.history) ? p.history : [],
              todaysSales: typeof p.todaysSales === 'number' ? p.todaysSales : 0
            })),
            currentDay: typeof parsed.currentDay === 'number' && parsed.currentDay >= 1 ? parsed.currentDay : 1,
            undoStack: Array.isArray(parsed.undoStack) ? parsed.undoStack.slice(-20) : [],
            demandMultiplier: typeof parsed.demandMultiplier === 'number' ? parsed.demandMultiplier : 1.0,
            soundEnabled: parsed.soundEnabled !== undefined ? Boolean(parsed.soundEnabled) : true,
            twilightMode: parsed.twilightMode !== undefined ? Boolean(parsed.twilightMode) : false,
            activeTab: parsed.activeTab || 'tab-painted-mall',
            activeReportSub: parsed.activeReportSub || 'tab-top-sellers'
          };
        }
      }
    } catch (e) {
      console.warn('Unable to load from localStorage; using fresh initial state', e);
    }
    return createFreshState();
  }

  const state = loadPersistedState();

  function saveStateToStorage() {
    try {
      const payload = {
        products: state.products,
        currentDay: state.currentDay,
        undoStack: state.undoStack.slice(-20),
        demandMultiplier: state.demandMultiplier,
        soundEnabled: state.soundEnabled,
        twilightMode: state.twilightMode,
        activeTab: state.activeTab,
        activeReportSub: state.activeReportSub
      };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
    } catch (e) {
      console.warn('Unable to save to localStorage', e);
    }
  }

  function resetStateToFactory() {
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch (e) {}
    const fresh = createFreshState();
    state.products = fresh.products;
    state.currentDay = fresh.currentDay;
    state.undoStack = [];
    state.demandMultiplier = 1.0;
    saveStateToStorage();
    playSound('bell');
    logActivity('Reset all boutique inventories and sales history to Factory Demo defaults.', 'restock');
    showToast('Inventory reset to initial factory demo data.', 'info');
    updateAllViews();
  }

  // =========================================================================
  // 2. MATHEMATICAL FORECASTING LOGIC
  // =========================================================================

  /**
   * Weighted Moving Average (WMA): Recent days receive linearly higher weight.
   * Day 1 weight = 1, Day 14 weight = 14. Denominator = 105.
   */
  function calculateWeightedAvg(product) {
    const history = product.history;
    if (!history || history.length === 0) return 0;
    const slice = history.slice(-14);
    const n = slice.length;
    let weightedSum = 0;
    let totalWeight = 0;
    for (let i = 0; i < n; i++) {
      const weight = i + 1;
      weightedSum += weight * slice[i];
      totalWeight += weight;
    }
    const baseWma = totalWeight > 0 ? (weightedSum / totalWeight) : 0;
    return baseWma * state.demandMultiplier;
  }

  function getLeadTimeDays(product) {
    return product.isPerishable ? 2 : 5;
  }

  function getSafetyStock(product) {
    const daily = calculateWeightedAvg(product);
    const multiplier = product.isPerishable ? 1.5 : 3.0;
    return Math.ceil(daily * multiplier);
  }

  function estimateDaysUntilStockout(product) {
    const daily = calculateWeightedAvg(product);
    if (daily <= 0.001) return 999;
    return Math.max(0, Math.floor(product.stock / daily));
  }

  function getUrgencyLevel(product) {
    const days = estimateDaysUntilStockout(product);
    if (days <= 2) return 'CRITICAL';
    if (days <= 5) return 'WARNING';
    return 'SAFE';
  }

  function evaluateReorder(product) {
    const daily = calculateWeightedAvg(product);
    const daysLeft = estimateDaysUntilStockout(product);
    const urgency = getUrgencyLevel(product);
    const leadTime = getLeadTimeDays(product);
    const safetyStock = getSafetyStock(product);
    const rop = (daily * leadTime) + safetyStock;

    const reviewCycleDays = 7;
    const targetStock = ((leadTime + reviewCycleDays) * daily) + safetyStock;

    let suggestedQty = 0;
    let rationale = '';

    if (product.stock <= rop) {
      suggestedQty = Math.max(10, Math.ceil(targetStock - product.stock));
      rationale = `Stock below ROP buffer (${Math.round(rop)} units). Reorder ${suggestedQty} units to cover ${leadTime}-day lead time + safety buffer.`;
    } else {
      suggestedQty = 0;
      rationale = 'Inventory level is healthy and above threshold.';
    }

    return {
      product,
      currentStock: product.stock,
      dailyRate: daily,
      daysLeft,
      urgency,
      leadTime,
      safetyStock,
      suggestedQty,
      rationale
    };
  }

  function getZoneHealth(zoneCode) {
    const prods = state.products.filter(p => p.zone === zoneCode);
    if (prods.length === 0) return 'SAFE';
    let hasWarning = false;
    for (const p of prods) {
      const days = estimateDaysUntilStockout(p);
      if (days <= 2) return 'CRITICAL';
      if (days <= 5) hasWarning = true;
    }
    return hasWarning ? 'WARNING' : 'SAFE';
  }

  function getOverallMallHealth() {
    for (const code of Object.keys(BOUTIQUES)) {
      if (getZoneHealth(code) === 'CRITICAL') return 'CRITICAL';
    }
    for (const code of Object.keys(BOUTIQUES)) {
      if (getZoneHealth(code) === 'WARNING') return 'WARNING';
    }
    return 'SAFE';
  }

  function getTotalUnitsSold(product) {
    return product.history.reduce((a, b) => a + b, 0);
  }

  // =========================================================================
  // 3. CALM & SOOTHING AUDIO SYNTHESIZER (Pure Web Audio)
  // =========================================================================

  let audioCtx = null;

  function initAudio() {
    if (!audioCtx) {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      if (AudioContext) audioCtx = new AudioContext();
    }
    if (audioCtx && audioCtx.state === 'suspended') {
      audioCtx.resume();
    }
  }

  function playSoftChime(freq = 587.33, duration = 0.4, gainLevel = 0.06) {
    if (!state.soundEnabled) return;
    try {
      initAudio();
      if (!audioCtx) return;
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();

      osc.type = 'sine'; // pure calm harmonic sine wave
      osc.frequency.setValueAtTime(freq, audioCtx.currentTime);

      gain.gain.setValueAtTime(gainLevel, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.0001, audioCtx.currentTime + duration);

      osc.connect(gain);
      gain.connect(audioCtx.destination);

      osc.start();
      osc.stop(audioCtx.currentTime + duration);
    } catch (e) {
    }
  }

  function playSound(type) {
    if (!state.soundEnabled) return;
    switch (type) {
      case 'chime':
        playSoftChime(659.25, 0.4, 0.05); // E5
        setTimeout(() => playSoftChime(880.00, 0.5, 0.04), 100); // A5
        break;
      case 'bell':
        playSoftChime(523.25, 0.6, 0.07); // C5
        break;
      case 'pop':
        playSoftChime(440.00, 0.15, 0.03); // A4
        break;
    }
  }

  function showToast(message, type = 'info') {
    const stack = document.getElementById('toastStack');
    if (!stack) return;
    const msg = document.createElement('div');
    msg.className = `toast-msg ${type}`;
    msg.textContent = message;
    stack.appendChild(msg);
    setTimeout(() => {
      msg.style.opacity = '0';
      msg.style.transform = 'translateY(6px)';
      msg.style.transition = 'all 0.25s ease';
      setTimeout(() => msg.remove(), 250);
    }, 3000);
  }

  function logActivity(text, type = 'system') {
    const auditEl = document.getElementById('tickerAuditPill');
    if (auditEl) auditEl.textContent = text;

    const feed = document.getElementById('simEventFeed');
    if (feed) {
      const entry = document.createElement('div');
      entry.className = `feed-entry ${type}`;
      const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
      entry.textContent = `[${time}] ${text}`;
      feed.prepend(entry);
      while (feed.children.length > 25) feed.lastElementChild.remove();
    }
  }

  // =========================================================================
  // 4. CRISP 2D SVG PROMENADE PAINTING ENGINE
  // =========================================================================

  class StrollingVisitor {
    constructor(id) {
      this.id = id;
      this.x = 440 + Math.random() * 220;
      this.y = 120 + Math.random() * 340;
      this.targetX = this.x;
      this.targetY = this.y;
      this.speed = 0.5 + Math.random() * 0.4;
      this.coatColor = ['#5C7C99', '#4A7C59', '#C49A6C', '#8E6582', '#6B635A'][Math.floor(Math.random() * 5)];
      this.element = this.createSvgElement();
    }

    createSvgElement() {
      const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
      g.setAttribute('class', 'visitor-figure');

      // Shadow
      const shadow = document.createElementNS('http://www.w3.org/2000/svg', 'ellipse');
      shadow.setAttribute('cx', '0');
      shadow.setAttribute('cy', '14');
      shadow.setAttribute('rx', '7');
      shadow.setAttribute('ry', '3');
      shadow.setAttribute('fill', '#2D2823');
      shadow.setAttribute('opacity', '0.12');

      // Coat body
      const body = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
      body.setAttribute('x', '-5');
      body.setAttribute('y', '-2');
      body.setAttribute('width', '10');
      body.setAttribute('height', '13');
      body.setAttribute('rx', '3');
      body.setAttribute('fill', this.coatColor);

      // Head
      const head = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
      head.setAttribute('cx', '0');
      head.setAttribute('cy', '-7');
      head.setAttribute('r', '5');
      head.setAttribute('fill', '#E8D4C0');

      g.appendChild(shadow);
      g.appendChild(body);
      g.appendChild(head);
      return g;
    }

    update() {
      const dx = this.targetX - this.x;
      const dy = this.targetY - this.y;
      const dist = Math.hypot(dx, dy);

      if (dist < 4) {
        // Pick new flagstone walkway point
        this.targetX = 400 + Math.random() * 300;
        this.targetY = 80 + Math.random() * 420;
      } else {
        this.x += (dx / dist) * this.speed;
        this.y += (dy / dist) * this.speed;
      }
      this.element.setAttribute('transform', `translate(${this.x.toFixed(1)}, ${this.y.toFixed(1)})`);
    }
  }

  const visitors = [];
  const visitorsLayer = document.getElementById('visitorsLayer');

  function initPromenadeArtwork() {
    if (visitorsLayer) {
      for (let i = 0; i < 6; i++) {
        const v = new StrollingVisitor(i);
        visitors.push(v);
        visitorsLayer.appendChild(v.element);
      }
    }

    // Peaceful animation loop
    function animatePromenade() {
      visitors.forEach(v => v.update());
      requestAnimationFrame(animatePromenade);
    }
    animatePromenade();

    // Click handler for 4 shops
    document.querySelectorAll('.shop-node').forEach(node => {
      node.addEventListener('click', () => {
        const zone = node.getAttribute('data-zone');
        playSound('pop');
        openBoutiqueInspectionModal(zone);
      });
    });
  }

  // Delivery Courier Van Animation along flagstones
  let isVanInTransit = false;
  function dispatchCourierVan(targetZone) {
    const van = document.getElementById('courierVan');
    if (!van || isVanInTransit) return;
    isVanInTransit = true;
    playSound('bell');

    van.style.display = 'block';
    let progress = 0;
    const startX = -90;
    const endX = targetZone === 'FOOD' || targetZone === 'FASH' ? 270 : 810;
    const targetY = targetZone === 'FOOD' || targetZone === 'ELEC' ? 140 : 420;

    const interval = setInterval(() => {
      progress += 0.02;
      const currentX = startX + (endX - startX) * progress;
      van.setAttribute('transform', `translate(${currentX.toFixed(1)}, ${targetY})`);

      if (progress >= 1) {
        clearInterval(interval);
        setTimeout(() => {
          van.style.display = 'none';
          isVanInTransit = false;
        }, 1200);
      }
    }, 25);
  }

  // =========================================================================
  // 5. INVENTORY ACTIONS (Sale, Restock, Undo, Add)
  // =========================================================================

  function recordSale(productId, quantity) {
    const product = state.products.find(p => p.id === productId);
    if (!product) return false;

    if (quantity <= 0) {
      showToast('Quantity must be at least 1.', 'warning');
      return false;
    }
    if (product.stock < quantity) {
      showToast(`Stockout: Only ${product.stock} units available for '${product.name}'!`, 'error');
      return false;
    }

    product.stock -= quantity;
    product.todaysSales = (product.todaysSales || 0) + quantity;
    const totalAmount = quantity * product.price;

    state.undoStack.push({
      id: product.id,
      name: product.name,
      quantity,
      amount: totalAmount,
      timestamp: Date.now()
    });
    if (state.undoStack.length > 20) {
      state.undoStack.shift();
    }

    playSound('chime');
    logActivity(`Checkout: ${quantity}x '${product.name}' (₹${totalAmount.toFixed(2)}). Stock left: ${product.stock}`, 'sale');
    showToast(`Purchased ${quantity}x ${product.name} (₹${totalAmount.toFixed(2)})`, 'info');

    saveStateToStorage();
    updateAllViews();
    return true;
  }

  function restockProduct(productId, quantity) {
    const product = state.products.find(p => p.id === productId);
    if (!product || quantity <= 0) return false;

    product.stock += quantity;
    dispatchCourierVan(product.zone);
    logActivity(`Restocked +${quantity} units to '${product.name}'. In stock: ${product.stock}`, 'restock');
    showToast(`Replenished +${quantity} units of ${product.name}`, 'info');

    saveStateToStorage();
    updateAllViews();
    return true;
  }

  function undoLastSale() {
    if (state.undoStack.length === 0) {
      showToast('No recent transactions to undo.', 'warning');
      return;
    }

    const tx = state.undoStack[state.undoStack.length - 1];
    const product = state.products.find(p => p.id === tx.id);
    if (!product) {
      state.undoStack.pop();
      showToast('Product associated with this transaction no longer exists.', 'warning');
      return;
    }

    state.undoStack.pop();
    product.stock += tx.quantity;
    product.todaysSales = Math.max(0, (product.todaysSales || 0) - tx.quantity);

    playSound('pop');
    logActivity(`Reverted Sale: Restored ${tx.quantity} units of '${tx.name}'`);
    showToast(`Reverted: +${tx.quantity} ${tx.name} returned to shelves`, 'info');
    saveStateToStorage();
    updateAllViews();
  }

  function addProduct(newProduct) {
    if (newProduct.todaysSales === undefined) newProduct.todaysSales = 0;
    if (!Array.isArray(newProduct.history)) newProduct.history = new Array(14).fill(0);
    state.products.push(newProduct);
    playSound('bell');
    logActivity(`Registered new SKU: [${newProduct.id}] ${newProduct.name} in ${newProduct.zone}`);
    showToast(`Registered SKU '${newProduct.name}'`, 'info');
    saveStateToStorage();
    updateAllViews();
  }

  function removeProduct(productId) {
    const idx = state.products.findIndex(p => p.id === productId);
    if (idx !== -1) {
      const p = state.products.splice(idx, 1)[0];
      playSound('pop');
      logActivity(`Removed SKU: [${p.id}] ${p.name}`);
      showToast(`Removed SKU ${p.name}`, 'warning');
      saveStateToStorage();
      updateAllViews();
    }
  }

  function nextDay() {
    const closedDay = state.currentDay || 1;
    state.products.forEach(p => {
      const today = p.todaysSales || 0;
      if (!Array.isArray(p.history)) p.history = [];
      p.history.push(today);
      while (p.history.length > 14) {
        p.history.shift();
      }
      p.todaysSales = 0;
    });

    state.currentDay = closedDay + 1;
    saveStateToStorage();
    playSound('bell');
    logActivity(`🌅 Day ${closedDay} closed. Advanced to Day ${state.currentDay}. 14-day sales histories updated.`);
    showToast(`Advanced to Day ${state.currentDay}! Daily sales archived into 14-day history.`, 'info');
    updateAllViews();
  }

  // =========================================================================
  // 6. GENTLE SIMULATION ENGINE
  // =========================================================================

  function toggleSimulation() {
    if (state.simulation.active) {
      stopSimulation();
    } else {
      startSimulation();
    }
  }

  function startSimulation() {
    state.simulation.active = true;
    const btn = document.getElementById('btnToggleSim');
    btn.textContent = '⏸ Pause Gentle Simulation';
    btn.classList.add('active');
    document.getElementById('simLiveDot').style.display = 'inline-block';
    logActivity('Gentle visitor footfall simulation started.');

    state.simulation.intervalId = setInterval(() => {
      simulationStep();
    }, state.simulation.intervalMs);
  }

  function stopSimulation() {
    state.simulation.active = false;
    clearInterval(state.simulation.intervalId);
    state.simulation.intervalId = null;

    const btn = document.getElementById('btnToggleSim');
    btn.textContent = '▶ Start Gentle Simulation';
    btn.classList.remove('active');
    document.getElementById('simLiveDot').style.display = 'none';
    logActivity('Simulation paused.');
  }

  function simulationStep() {
    state.simulation.ticks++;
    document.getElementById('simValTicks').textContent = state.simulation.ticks;

    // Pick random product
    const p = state.products[Math.floor(Math.random() * state.products.length)];
    const qty = 1 + Math.floor(Math.random() * 2);

    if (p.stock >= qty) {
      p.stock -= qty;
      p.todaysSales = (p.todaysSales || 0) + qty;
      state.simulation.purchases++;
      document.getElementById('simValPurchases').textContent = state.simulation.purchases;
      logActivity(`Checkout: Customer purchased ${qty}x '${p.name}' (₹${(qty * p.price).toFixed(2)})`, 'sale');
    } else {
      state.simulation.stockouts++;
      document.getElementById('simValStockouts').textContent = state.simulation.stockouts;
      logActivity(`Turnaway: Stockout on '${p.name}'! (Requested: ${qty}, Stock: ${p.stock})`, 'stockout');
    }

    saveStateToStorage();
    updateAllViews();
  }

  // =========================================================================
  // 7. RAZOR-SHARP RENDERERS (SVG CHARTS & VIEWS)
  // =========================================================================

  function updateAllViews() {
    renderKPIs();
    renderTopSellersBars();
    renderStockoutHorizonGauges();
    renderSalesTrendCurve();
    renderProductCards();
    renderReordersTable();
    renderAnalyticsTable();
    renderSimulationMeters();
    updateHeaderPills();
    updatePromenadeSeals();
  }

  function updateHeaderPills() {
    const overall = getOverallMallHealth();
    const badge = document.getElementById('headerHealthBadge');
    const badgeText = document.getElementById('headerHealthText');

    badge.className = `pill-badge pill-health ${overall.toLowerCase()}`;
    badgeText.textContent = overall === 'CRITICAL' ? 'Critical Attention Required' :
                           overall === 'WARNING' ? 'Replenishment Warning' : 'Boutiques Optimal';

    document.getElementById('headerSurge').textContent = `Demand Rate: ${state.demandMultiplier.toFixed(2)}x Base`;

    const dayBadge = document.getElementById('headerDayBadge');
    if (dayBadge) dayBadge.textContent = `🌅 Day ${state.currentDay || 1}`;
    const simDayLabel = document.getElementById('simDayLabel');
    if (simDayLabel) simDayLabel.textContent = `Day ${state.currentDay || 1}`;

    // Bulletin ticker
    const criticals = state.products.filter(p => estimateDaysUntilStockout(p) <= 2);
    const ticker = document.getElementById('liveTickerText');
    if (criticals.length > 0) {
      ticker.textContent = 'Stock Alert: ' + criticals.map(p => `[${p.id}] ${p.name} (${p.stock} units left, ${estimateDaysUntilStockout(p)}d)`).join(' · ');
    } else {
      ticker.textContent = 'Welcome to StockHome. All 4 boutiques are operating with optimal safety stock buffers.';
    }

    const reorderCount = state.products.filter(p => evaluateReorder(p).suggestedQty > 0).length;
    document.getElementById('chipReorderCount').textContent = reorderCount;
  }

  function updatePromenadeSeals() {
    // Update SVG shop status seals
    for (const [code, b] of Object.entries(BOUTIQUES)) {
      const g = document.getElementById(b.node);
      if (!g) continue;
      const health = getZoneHealth(code);
      const prods = state.products.filter(p => p.zone === code);
      const totalUnits = prods.reduce((a, b) => a + b.stock, 0);

      const sealBg = g.querySelector('.seal-bg');
      const sealDot = g.querySelector('.seal-dot');
      const sealText = g.querySelector('.seal-text');
      const stockBadge = g.querySelector('.stock-badge');
      const glowRect = g.querySelector('.shop-glow-rect');

      if (stockBadge) {
        stockBadge.textContent = `${prods.length} SKUs · ${totalUnits} Units`;
      }

      if (health === 'CRITICAL') {
        if (sealBg) { sealBg.setAttribute('fill', '#F9EBEA'); sealBg.setAttribute('stroke', '#C05C54'); }
        if (sealDot) sealDot.setAttribute('fill', '#C05C54');
        if (sealText) { sealText.textContent = 'CRITICAL STOCK'; sealText.setAttribute('fill', '#8C312A'); }
        if (glowRect) { glowRect.setAttribute('fill', '#C05C54'); glowRect.setAttribute('opacity', '0.18'); }
      } else if (health === 'WARNING') {
        if (sealBg) { sealBg.setAttribute('fill', '#FDF3E7'); sealBg.setAttribute('stroke', '#D48B38'); }
        if (sealDot) sealDot.setAttribute('fill', '#D48B38');
        if (sealText) { sealText.textContent = 'LOW BUFFER'; sealText.setAttribute('fill', '#945815'); }
        if (glowRect) { glowRect.setAttribute('fill', '#D48B38'); glowRect.setAttribute('opacity', '0.12'); }
      } else {
        if (sealBg) { sealBg.setAttribute('fill', '#EAF3EC'); sealBg.setAttribute('stroke', '#4A7C59'); }
        if (sealDot) sealDot.setAttribute('fill', '#4A7C59');
        if (sealText) { sealText.textContent = 'SAFE BUFFER'; sealText.setAttribute('fill', '#2D5A3C'); }
        if (glowRect) { glowRect.setAttribute('fill', '#4A7C59'); glowRect.setAttribute('opacity', '0.08'); }
      }
    }
  }

  function renderKPIs() {
    document.getElementById('metricTotalSkus').textContent = state.products.length;
    const crit = state.products.filter(p => estimateDaysUntilStockout(p) <= 2).length;
    document.getElementById('metricCriticalCount').textContent = crit;
    const warn = state.products.filter(p => {
      const d = estimateDaysUntilStockout(p);
      return d > 2 && d <= 5;
    }).length;
    document.getElementById('metricWarningCount').textContent = warn;

    let totalSold = 0;
    let totalRevenue = 0;
    state.products.forEach(p => {
      const s = getTotalUnitsSold(p);
      totalSold += s;
      totalRevenue += s * p.price;
    });
    document.getElementById('metricUnitsSold').textContent = totalSold.toLocaleString();
    document.getElementById('metricRevenueText').textContent = `₹${totalRevenue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })} historical revenue`;
  }

  // Crisp High-DPI Bar Chart
  function renderTopSellersBars() {
    const wrap = document.getElementById('topSellersBarsWrapper');
    if (!wrap) return;
    const sorted = [...state.products].sort((a, b) => getTotalUnitsSold(b) - getTotalUnitsSold(a)).slice(0, 5);
    const maxVal = Math.max(1, getTotalUnitsSold(sorted[0]));

    wrap.innerHTML = sorted.map((p, idx) => {
      const units = getTotalUnitsSold(p);
      const pct = Math.round((units / maxVal) * 100);
      const colors = ['#5C7C99', '#4A7C59', '#D48B38', '#8E6582', '#A8829C'];

      return `
        <div class="soothing-bar-item">
          <div class="bar-meta-row">
            <span>${p.icon} <b>${p.name}</b> <small style="color: var(--text-tertiary);">(${BOUTIQUES[p.zone].name})</small></span>
            <span><b>${units}</b> units sold</span>
          </div>
          <div class="bar-track">
            <div class="bar-fill" style="width: ${pct}%; background: ${colors[idx % colors.length]};"></div>
          </div>
        </div>
      `;
    }).join('');
  }

  // Stockout Horizon Gauges
  function renderStockoutHorizonGauges() {
    const wrap = document.getElementById('stockoutHorizonWrapper');
    if (!wrap) return;
    const sorted = [...state.products].sort((a, b) => estimateDaysUntilStockout(a) - estimateDaysUntilStockout(b)).slice(0, 5);

    wrap.innerHTML = sorted.map(p => {
      const days = estimateDaysUntilStockout(p);
      const daysText = days >= 999 ? 'No Sales' : `${days} days`;
      const ratio = Math.min(100, Math.round((days / 15) * 100));
      const col = days <= 2 ? 'var(--terracotta-crit)' : (days <= 5 ? 'var(--amber-warn)' : 'var(--sage-safe)');

      return `
        <div class="soothing-bar-item">
          <div class="bar-meta-row">
            <span>${p.icon} ${p.name}</span>
            <span style="color: ${col}; font-weight: 700;">${daysText}</span>
          </div>
          <div class="bar-track">
            <div class="bar-fill" style="width: ${Math.max(6, ratio)}%; background: ${col};"></div>
          </div>
        </div>
      `;
    }).join('');
  }

  // Razor-Sharp Vector SVG Sales Trend Line
  function renderSalesTrendCurve() {
    const wrap = document.getElementById('salesTrendCurveContainer');
    if (!wrap) return;

    const dailyTotals = new Array(14).fill(0);
    state.products.forEach(p => {
      for (let d = 0; d < 14 && d < p.history.length; d++) {
        dailyTotals[d] += p.history[d];
      }
    });

    const max = Math.max(10, Math.max(...dailyTotals));
    const w = 1000;
    const h = 220;
    const padL = 50;
    const padR = 30;
    const padT = 20;
    const padB = 35;
    const plotW = w - padL - padR;
    const plotH = h - padT - padB;

    const points = dailyTotals.map((val, i) => {
      const x = padL + (i / 13) * plotW;
      const y = padT + plotH - (val / max) * plotH;
      return { x, y, val };
    });

    const linePath = points.map((pt, i) => `${i === 0 ? 'M' : 'L'} ${pt.x.toFixed(1)} ${pt.y.toFixed(1)}`).join(' ');
    const areaPath = `${linePath} L ${points[points.length - 1].x.toFixed(1)} ${(padT + plotH).toFixed(1)} L ${points[0].x.toFixed(1)} ${(padT + plotH).toFixed(1)} Z`;

    wrap.innerHTML = `
      <svg viewBox="0 0 ${w} ${h}" preserveAspectRatio="xMidYMid meet" style="width: 100%; height: auto; display: block;">
        <!-- Area fill gradient -->
        <path d="${areaPath}" fill="rgba(69, 105, 144, 0.12)" />
        <!-- Stroke curve -->
        <path d="${linePath}" fill="none" stroke="#456990" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
        <!-- Points & Day Labels -->
        ${points.map((pt, i) => `
          <circle cx="${pt.x.toFixed(1)}" cy="${pt.y.toFixed(1)}" r="4" fill="#FFFFFF" stroke="#456990" stroke-width="2" />
          <text x="${pt.x.toFixed(1)}" y="${h - 10}" text-anchor="middle" fill="#968D82" font-family="Plus Jakarta Sans, sans-serif" font-size="11">D${i + 1}</text>
        `).join('')}
      </svg>
    `;
  }

  // Catalog Cards
  function renderProductCards() {
    const grid = document.getElementById('productGridContainer');
    if (!grid) return;

    const query = document.getElementById('catalogSearch').value.trim().toLowerCase();
    const zoneFilter = document.getElementById('selectZoneFilter').value;
    const healthFilter = document.getElementById('selectHealthFilter').value;

    const filtered = state.products.filter(p => {
      if (zoneFilter !== 'ALL' && p.zone !== zoneFilter) return false;
      const urgency = getUrgencyLevel(p);
      if (healthFilter !== 'ALL' && urgency !== healthFilter) return false;
      if (query && !p.name.toLowerCase().includes(query) && !p.id.toLowerCase().includes(query)) return false;
      return true;
    });

    if (filtered.length === 0) {
      grid.innerHTML = '<div style="grid-column: 1/-1; padding: 40px; text-align: center; color: var(--text-tertiary);">No boutique items match your search.</div>';
      return;
    }

    grid.innerHTML = filtered.map(p => {
      const urgency = getUrgencyLevel(p);
      const days = estimateDaysUntilStockout(p);
      const daysText = days >= 999 ? 'No consumption' : `${days} days left`;
      const pct = Math.min(100, Math.round((p.stock / 100) * 100));

      return `
        <div class="boutique-card">
          <div class="card-top-row">
            <span class="sku-pill">${p.id}</span>
            <span class="status-pill ${urgency.toLowerCase()}">${urgency}</span>
          </div>

          <div class="card-body-info">
            <div class="card-emoji-wrap">${p.icon}</div>
            <div>
              <div class="card-product-name">${p.name}</div>
              <div class="card-zone-sub">${BOUTIQUES[p.zone].name}</div>
            </div>
          </div>

          <div class="card-meter-block">
            <div class="meter-meta">
              <span>Stock: <b>${p.stock} units</b></span>
              <span style="color: var(--text-tertiary);">${daysText}</span>
            </div>
            <div class="bar-track">
              <div class="bar-fill" style="width: ${pct}%; background: ${urgency === 'CRITICAL' ? 'var(--terracotta-crit)' : (urgency === 'WARNING' ? 'var(--amber-warn)' : 'var(--sage-safe)')};"></div>
            </div>
          </div>

          <div class="card-action-bar">
            <div class="price-text">₹${p.price.toFixed(2)}</div>
            <div class="mini-btn-group">
              <button class="btn-pill-small green btn-act-buy" data-id="${p.id}">Buy 1</button>
              <button class="btn-pill-small blue btn-act-restock" data-id="${p.id}">+25</button>
              <button class="btn-pill-small btn-act-delete" data-id="${p.id}" title="Remove SKU">✕</button>
            </div>
          </div>
        </div>
      `;
    }).join('');

    // Attach actions
    grid.querySelectorAll('.btn-act-buy').forEach(b => {
      b.onclick = () => recordSale(b.dataset.id, 1);
    });
    grid.querySelectorAll('.btn-act-restock').forEach(b => {
      b.onclick = () => restockProduct(b.dataset.id, 25);
    });
    grid.querySelectorAll('.btn-act-delete').forEach(b => {
      b.onclick = () => {
        if (confirm('Permanently remove this artisan item?')) {
          removeProduct(b.dataset.id);
        }
      };
    });
  }

  // Reorder Table
  function renderReordersTable() {
    const tbody = document.getElementById('tbodyReorders');
    if (!tbody) return;

    const list = state.products.map(p => evaluateReorder(p));
    list.sort((a, b) => {
      const order = { CRITICAL: 1, WARNING: 2, SAFE: 3 };
      if (order[a.urgency] !== order[b.urgency]) return order[a.urgency] - order[b.urgency];
      return a.daysLeft - b.daysLeft;
    });

    tbody.innerHTML = list.map((item, i) => {
      const daysText = item.daysLeft >= 999 ? 'N/A' : `${item.daysLeft} d`;
      const needOrder = item.suggestedQty > 0;

      return `
        <tr>
          <td><strong>#${i + 1}</strong></td>
          <td><span class="status-pill ${item.urgency.toLowerCase()}">${item.urgency}</span></td>
          <td><span class="sku-pill">${item.product.id}</span></td>
          <td><strong>${item.product.icon} ${item.product.name}</strong></td>
          <td>${item.currentStock} units</td>
          <td>${daysText}</td>
          <td><strong style="color: var(--slate-blue);">${item.suggestedQty > 0 ? item.suggestedQty + ' units' : 'None'}</strong></td>
          <td>${item.leadTime} days</td>
          <td style="font-size: 12px; color: var(--text-secondary); max-width: 320px;">${item.rationale}</td>
          <td>
            ${needOrder ? `<button class="btn-action btn-green btn-table-order" data-id="${item.product.id}" data-qty="${item.suggestedQty}">🚚 Restock</button>` : `<span style="color: var(--sage-safe); font-weight: 600;">Healthy</span>`}
          </td>
        </tr>
      `;
    }).join('');

    tbody.querySelectorAll('.btn-table-order').forEach(b => {
      b.onclick = () => restockProduct(b.dataset.id, parseInt(b.dataset.qty));
    });
  }

  // Analytics Sub-table
  function renderAnalyticsTable() {
    const area = document.getElementById('analyticsTableArea');
    if (!area) return;

    if (state.activeReportSub === 'tab-top-sellers') {
      const sorted = [...state.products].sort((a, b) => getTotalUnitsSold(b) - getTotalUnitsSold(a));
      area.innerHTML = `
        <table class="soothing-table">
          <thead><tr><th>Rank</th><th>SKU</th><th>Product</th><th>Boutique</th><th>Units Sold</th><th>Price</th><th>Gross Revenue</th></tr></thead>
          <tbody>
            ${sorted.map((p, i) => {
              const u = getTotalUnitsSold(p);
              return `<tr>
                <td>#${i + 1}</td>
                <td><span class="sku-pill">${p.id}</span></td>
                <td>${p.icon} ${p.name}</td>
                <td>${BOUTIQUES[p.zone].name}</td>
                <td><strong>${u}</strong></td>
                <td>₹${p.price.toFixed(2)}</td>
                <td style="color: var(--sage-safe); font-weight: 600;">₹${(u * p.price).toFixed(2)}</td>
              </tr>`;
            }).join('')}
          </tbody>
        </table>
      `;
    } else if (state.activeReportSub === 'tab-slow-movers') {
      const sorted = [...state.products].sort((a, b) => calculateWeightedAvg(a) - calculateWeightedAvg(b));
      area.innerHTML = `
        <table class="soothing-table">
          <thead><tr><th>Rank</th><th>SKU</th><th>Product</th><th>Boutique</th><th>Stock</th><th>Velocity (Units/Day)</th><th>Days Left</th></tr></thead>
          <tbody>
            ${sorted.map((p, i) => `<tr>
              <td>#${i + 1}</td>
              <td><span class="sku-pill">${p.id}</span></td>
              <td>${p.icon} ${p.name}</td>
              <td>${BOUTIQUES[p.zone].name}</td>
              <td>${p.stock} units</td>
              <td><strong>${calculateWeightedAvg(p).toFixed(2)}</strong></td>
              <td>${estimateDaysUntilStockout(p) >= 999 ? 'No consumption' : estimateDaysUntilStockout(p) + ' days'}</td>
            </tr>`).join('')}
          </tbody>
        </table>
      `;
    } else if (state.activeReportSub === 'tab-dead-stock') {
      const dead = state.products.filter(p => getTotalUnitsSold(p) === 0 || calculateWeightedAvg(p) <= 0.2);
      const totalTied = dead.reduce((sum, p) => sum + (p.stock * p.price), 0);

      area.innerHTML = `
        <div style="background: var(--terracotta-crit-bg); color: var(--terracotta-crit); padding: 12px 18px; border-radius: 8px; font-weight: 600; margin-bottom: 14px;">
          Working Capital Tied Up in Stagnant Stock: ₹${totalTied.toFixed(2)}
        </div>
        <table class="soothing-table">
          <thead><tr><th>SKU</th><th>Product</th><th>Boutique</th><th>Unit Price</th><th>Unsold Units</th><th>Locked Capital</th></tr></thead>
          <tbody>
            ${dead.map(p => `<tr>
              <td><span class="sku-pill">${p.id}</span></td>
              <td>${p.icon} ${p.name}</td>
              <td>${BOUTIQUES[p.zone].name}</td>
              <td>₹${p.price.toFixed(2)}</td>
              <td><b>${p.stock} units</b></td>
              <td style="color: var(--terracotta-crit); font-weight: 600;">₹${(p.stock * p.price).toFixed(2)}</td>
            </tr>`).join('')}
          </tbody>
        </table>
      `;
    } else if (state.activeReportSub === 'tab-matrix') {
      const zoneCodes = ['FOOD', 'ELEC', 'FASH', 'GROC'];
      const dailySums = new Array(14).fill(0);

      const rows = zoneCodes.map(code => {
        const prods = state.products.filter(p => p.zone === code);
        const sums = new Array(14).fill(0);
        let zoneTotal = 0;
        prods.forEach(p => {
          for (let d = 0; d < 14 && d < p.history.length; d++) {
            sums[d] += p.history[d];
            dailySums[d] += p.history[d];
            zoneTotal += p.history[d];
          }
        });
        return { name: BOUTIQUES[code].name, sums, zoneTotal };
      });

      const grandTotal = dailySums.reduce((a, b) => a + b, 0);

      area.innerHTML = `
        <table class="soothing-table">
          <thead>
            <tr>
              <th>Sector</th>
              ${Array.from({ length: 14 }, (_, i) => `<th>D${i + 1}</th>`).join('')}
              <th style="color: var(--slate-blue);">Total</th>
            </tr>
          </thead>
          <tbody>
            ${rows.map(r => `
              <tr>
                <td><strong>${r.name}</strong></td>
                ${r.sums.map(v => `<td>${v}</td>`).join('')}
                <td><strong>${r.zoneTotal}</strong></td>
              </tr>
            `).join('')}
            <tr style="background: var(--bg-surface-subtle); font-weight: 700;">
              <td>MALL TOTAL DAILY</td>
              ${dailySums.map(v => `<td>${v}</td>`).join('')}
              <td style="color: var(--sage-safe);">${grandTotal}</td>
            </tr>
          </tbody>
        </table>
      `;
    }
  }

  function renderSimulationMeters() {
    const container = document.getElementById('simShelfMetersContainer');
    if (!container) return;
    const items = ['F101', 'E201', 'S301', 'G401'].map(id => state.products.find(p => p.id === id)).filter(Boolean);

    container.innerHTML = items.map(p => {
      const maxCap = 70;
      const pct = Math.min(100, Math.round((p.stock / maxCap) * 100));
      const col = pct <= 25 ? 'var(--terracotta-crit)' : (pct <= 50 ? 'var(--amber-warn)' : 'var(--sage-safe)');

      return `
        <div>
          <div style="display: flex; justify-content: space-between; font-size: 13px; font-weight: 500; margin-bottom: 4px;">
            <span>${p.icon} ${p.name}</span>
            <span style="font-weight: 700; color: ${col};">${p.stock} units</span>
          </div>
          <div class="bar-track">
            <div class="bar-fill" style="width: ${pct}%; background: ${col};"></div>
          </div>
        </div>
      `;
    }).join('');
  }

  // Boutique Inspection Modal
  function openBoutiqueInspectionModal(zoneCode) {
    const modal = document.getElementById('modalInspect');
    const b = BOUTIQUES[zoneCode];
    const prods = state.products.filter(p => p.zone === zoneCode);
    const health = getZoneHealth(zoneCode);

    document.getElementById('inspectShopTitle').textContent = `${b.name} (${b.category})`;
    document.getElementById('inspectStatsBanner').innerHTML = `
      <div style="display: flex; gap: 24px; font-size: 13px;">
        <span>SKUs: <b>${prods.length}</b></span>
        <span>Units on Hand: <b>${prods.reduce((a, b) => a + b.stock, 0)}</b></span>
        <span>Health: <b class="status-pill ${health.toLowerCase()}">${health}</b></span>
      </div>
    `;

    document.getElementById('inspectTableWrap').innerHTML = `
      <table class="soothing-table">
        <thead><tr><th>Item</th><th>Stock</th><th>Price</th><th>Days Left</th><th>Action</th></tr></thead>
        <tbody>
          ${prods.map(p => `
            <tr>
              <td><strong>${p.icon} ${p.name}</strong></td>
              <td>${p.stock}</td>
              <td>₹${p.price.toFixed(2)}</td>
              <td>${estimateDaysUntilStockout(p)} d</td>
              <td><button class="btn-pill-small green btn-modal-buy" data-id="${p.id}">Buy 1</button></td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;

    modal.querySelectorAll('.btn-modal-buy').forEach(btn => {
      btn.onclick = () => {
        recordSale(btn.dataset.id, 1);
        openBoutiqueInspectionModal(zoneCode);
      };
    });

    document.getElementById('btnGoToBoutiqueCatalog').onclick = () => {
      modal.classList.remove('open');
      document.querySelector('[data-tab="tab-catalog"]').click();
      document.getElementById('selectZoneFilter').value = zoneCode;
      renderProductCards();
    };

    modal.classList.add('open');
  }

  // =========================================================================
  // 8. EVENT WIRE-UP
  // =========================================================================

  // Tab switching
  document.querySelectorAll('.nav-item').forEach(tab => {
    tab.onclick = () => {
      playSound('pop');
      document.querySelectorAll('.nav-item').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.tab-screen').forEach(s => s.classList.remove('active'));
      tab.classList.add('active');
      const target = document.getElementById(tab.dataset.tab);
      if (target) target.classList.add('active');
      state.activeTab = tab.dataset.tab;
      updateAllViews();
    };
  });

  // Analytics sub-tabs
  document.querySelectorAll('.subnav-btn').forEach(btn => {
    btn.onclick = () => {
      playSound('pop');
      document.querySelectorAll('.subnav-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.activeReportSub = btn.dataset.sub;
      renderAnalyticsTable();
    };
  });

  // Demand Multiplier Slider
  const slider = document.getElementById('sliderDemandMultiplier');
  slider.oninput = (e) => {
    const val = e.target.value / 100;
    state.demandMultiplier = val;
    document.getElementById('multiplierSliderText').textContent = `${val.toFixed(2)}x Demand`;
    updateAllViews();
  };

  // Search & Filters
  document.getElementById('catalogSearch').oninput = renderProductCards;
  document.getElementById('selectZoneFilter').onchange = renderProductCards;
  document.getElementById('selectHealthFilter').onchange = renderProductCards;

  // Sound Toggle
  const sndBtn = document.getElementById('soundToggleBtn');
  sndBtn.onclick = () => {
    state.soundEnabled = !state.soundEnabled;
    sndBtn.innerHTML = state.soundEnabled ? '<span class="btn-icon">🔔</span> Chimes On' : '<span class="btn-icon">🔕</span> Chimes Muted';
    showToast(state.soundEnabled ? 'Gentle chimes enabled' : 'Chimes muted');
  };

  // Twilight Theme Toggle
  const themeBtn = document.getElementById('themeModeBtn');
  themeBtn.onclick = () => {
    state.twilightMode = !state.twilightMode;
    document.body.classList.toggle('theme-twilight', state.twilightMode);
    themeBtn.innerHTML = state.twilightMode ? '<span class="btn-icon">☀️</span> Daylight' : '<span class="btn-icon">🌙</span> Twilight';
  };

  // Promenade Action Buttons
  document.getElementById('btnStrollVisitor').onclick = () => {
    playSound('pop');
    if (visitorsLayer) {
      const v = new StrollingVisitor(visitors.length);
      visitors.push(v);
      visitorsLayer.appendChild(v.element);
      showToast('A new visitor has entered the promenade.');
    }
  };

  document.getElementById('btnRestockCourier').onclick = () => {
    dispatchCourierVan('FOOD');
  };

  // Simulation Toggle
  document.getElementById('btnToggleSim').onclick = toggleSimulation;

  document.querySelectorAll('.speed-pill').forEach(pill => {
    pill.onclick = () => {
      document.querySelectorAll('.speed-pill').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      state.simulation.intervalMs = parseInt(pill.dataset.interval);
      if (state.simulation.active) {
        clearInterval(state.simulation.intervalId);
        state.simulation.intervalId = setInterval(simulationStep, state.simulation.intervalMs);
      }
    };
  });

  // Modal Closers
  document.querySelectorAll('[data-modal]').forEach(btn => {
    btn.onclick = () => {
      const m = document.getElementById(btn.dataset.modal);
      if (m) m.classList.remove('open');
    };
  });

  // Click outside modal or press Escape to close
  document.querySelectorAll('.modal-scrim').forEach(scrim => {
    scrim.addEventListener('click', (e) => {
      if (e.target === scrim) {
        scrim.classList.remove('open');
      }
    });
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal-scrim.open').forEach(m => m.classList.remove('open'));
    }
  });

  // Record Sale Modal
  document.getElementById('btnOpenSaleModal').onclick = () => {
    const sel = document.getElementById('inputSaleProduct');
    sel.innerHTML = state.products.map(p => `<option value="${p.id}">${p.icon} [${p.id}] ${p.name} — ₹${p.price.toFixed(2)} (In Stock: ${p.stock})</option>`).join('');
    document.getElementById('modalSale').classList.add('open');
  };

  document.getElementById('btnConfirmSale').onclick = () => {
    const pid = document.getElementById('inputSaleProduct').value;
    const qty = parseInt(document.getElementById('inputSaleQty').value);
    if (recordSale(pid, qty)) {
      document.getElementById('modalSale').classList.remove('open');
    }
  };

  // Restock Modal
  document.getElementById('btnOpenRestockModal').onclick = () => {
    const sel = document.getElementById('inputRestockProduct');
    sel.innerHTML = state.products.map(p => `<option value="${p.id}">${p.icon} [${p.id}] ${p.name} (Stock: ${p.stock})</option>`).join('');
    document.getElementById('modalRestock').classList.add('open');
  };

  document.getElementById('btnConfirmRestock').onclick = () => {
    const pid = document.getElementById('inputRestockProduct').value;
    const qty = parseInt(document.getElementById('inputRestockQty').value);
    if (restockProduct(pid, qty)) {
      document.getElementById('modalRestock').classList.remove('open');
    }
  };

  // Undo Last Sale
  document.getElementById('btnUndoTransaction').onclick = undoLastSale;

  // Add SKU Modal
  document.getElementById('btnOpenAddSkuModal').onclick = () => {
    document.getElementById('modalAddSku').classList.add('open');
  };

  document.querySelectorAll('input[name="newSkuType"]').forEach(r => {
    r.onchange = (e) => {
      document.getElementById('lblNewSkuParam').textContent = e.target.value === 'perishable' ? 'Shelf Life in Days:' : 'Warranty Period (Months):';
    };
  });

  document.getElementById('btnConfirmAddSku').onclick = () => {
    const zone = document.getElementById('inputNewZone').value;
    const id = document.getElementById('inputNewId').value.trim().toUpperCase();
    const name = document.getElementById('inputNewName').value.trim();
    const price = parseFloat(document.getElementById('inputNewPrice').value);
    const stock = parseInt(document.getElementById('inputNewStock').value);
    const type = document.querySelector('input[name="newSkuType"]:checked').value;
    const extra = parseInt(document.getElementById('inputNewParam').value);

    if (!id || !name) {
      alert('Please provide a SKU ID and Product Name.');
      return;
    }

    const icons = { FOOD: '🥐', ELEC: '🎧', FASH: '👒', GROC: '🥑' };

    const newProd = {
      id,
      name,
      price: isNaN(price) ? 9.50 : price,
      stock: isNaN(stock) ? 40 : stock,
      zone,
      isPerishable: type === 'perishable',
      shelfLife: type === 'perishable' ? extra : undefined,
      warranty: type === 'nonperishable' ? extra : undefined,
      icon: icons[zone] || '📦',
      history: [10, 12, 14, 15, 12, 16, 18, 17, 20, 22, 21, 24, 25, 26]
    };

    addProduct(newProd);
    document.getElementById('modalAddSku').classList.remove('open');
  };

  // Order All Critical
  document.getElementById('btnBatchOrderCritical').onclick = () => {
    const criticals = state.products.filter(p => estimateDaysUntilStockout(p) <= 2);
    if (criticals.length === 0) {
      showToast('All boutiques have healthy buffers.');
      return;
    }
    criticals.forEach(p => {
      const sug = evaluateReorder(p);
      if (sug.suggestedQty > 0) restockProduct(p.id, sug.suggestedQty);
    });
    showToast(`Dispatched delivery van for all ${criticals.length} critical items.`);
  };

  // Live Clock
  setInterval(() => {
    const el = document.getElementById('headerClock');
    if (el) {
      const now = new Date();
      el.textContent = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
  }, 1000);

  // Initialize
  initPromenadeArtwork();
  updateAllViews();

})();
