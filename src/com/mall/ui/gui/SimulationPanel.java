package com.mall.ui.gui;

import com.mall.model.Product;
import com.mall.service.SimulationService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Screen 6: Real-time Multi-threaded Mall Traffic Simulator.
 * 
 * Features:
 * - Start/Stop worker Thread with clean interruption and join
 * - Live synchronized counters for ticks, purchases, and stockouts
 * - Real-time animated stock gauges that deplete as simulated customers buy goods
 */
public class SimulationPanel extends JPanel implements InventoryListener {

    private final GuiMediator mediator;
    private final JButton toggleSimBtn;
    private final JLabel statusPill;
    private final JLabel lblTicks;
    private final JLabel lblPurchases;
    private final JLabel lblStockouts;
    private final JLabel lblLastEvent;
    private final Timer pollingTimer;

    // Visual Stock Bars for key products
    private final LiveProductBar barBurger;
    private final LiveProductBar barEarbuds;
    private final LiveProductBar barJacket;
    private final LiveProductBar barMilk;

    public SimulationPanel(final GuiMediator mediator) {
        this.mediator = mediator;
        mediator.addInventoryListener(this);

        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Top Control Bar Card
        JPanel topCard = Theme.createCard();
        topCard.setLayout(new BorderLayout());

        JPanel leftControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        leftControl.setOpaque(false);

        toggleSimBtn = Theme.createButton("> START LIVE SIMULATION", Theme.GREEN_SAFE);
        toggleSimBtn.setPreferredSize(new Dimension(240, 42));
        toggleSimBtn.setFont(Theme.FONT_TITLE);
        toggleSimBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mediator.isSimulationActive()) {
                    mediator.stopSimulation();
                } else {
                    mediator.startSimulation();
                }
                updateUiState();
            }
        });
        leftControl.add(toggleSimBtn);

        statusPill = new JLabel("STATUS: IDLE");
        statusPill.setFont(Theme.FONT_BODY_BOLD);
        statusPill.setForeground(Theme.textMuted());
        leftControl.add(statusPill);

        topCard.add(leftControl, BorderLayout.WEST);

        JLabel infoLbl = new JLabel("Multithreading: Background Runnable simulates customer sales every 1000ms");
        infoLbl.setFont(Theme.FONT_SMALL);
        infoLbl.setForeground(Theme.TEAL);
        topCard.add(infoLbl, BorderLayout.EAST);

        add(topCard, BorderLayout.NORTH);

        // 2. Metrics & Stock Bars Center Area
        JPanel centerContent = new JPanel(new BorderLayout(0, 16));
        centerContent.setOpaque(false);

        // Counters Row
        JPanel countersRow = new JPanel(new GridLayout(1, 3, 14, 0));
        countersRow.setOpaque(false);

        lblTicks = new JLabel("0", SwingConstants.CENTER);
        lblPurchases = new JLabel("0", SwingConstants.CENTER);
        lblStockouts = new JLabel("0", SwingConstants.CENTER);

        countersRow.add(createCounterCard("SIMULATION TICKS", lblTicks, Theme.TEAL, "1-second mall traffic intervals"));
        countersRow.add(createCounterCard("PURCHASES COMPLETED", lblPurchases, Theme.GREEN_SAFE, "Successful customer checkouts"));
        countersRow.add(createCounterCard("STOCKOUTS AVOIDED / HIT", lblStockouts, Theme.RED_ALERT, "Customers turned away due to stockouts"));

        centerContent.add(countersRow, BorderLayout.NORTH);

        // Live Event Ticker Card & Real-Time Product Stock Bars
        JPanel bodyCard = Theme.createCard();
        bodyCard.setLayout(new BorderLayout(0, 14));

        lblLastEvent = new JLabel("Simulation awaiting activation. Press 'Start Live Simulation' above.");
        lblLastEvent.setFont(Theme.FONT_HEADER);
        lblLastEvent.setForeground(Theme.GOLD);
        lblLastEvent.setBorder(new EmptyBorder(4, 8, 10, 8));
        bodyCard.add(lblLastEvent, BorderLayout.NORTH);

        // 4 Representative Product Bars
        JPanel barsList = new JPanel(new GridLayout(4, 1, 0, 12));
        barsList.setOpaque(false);

        barBurger = new LiveProductBar("F101", "Artisan Burger Combo (Food Court)", 60);
        barEarbuds = new LiveProductBar("E201", "Aero ANC Earbuds (Electronics)", 40);
        barJacket = new LiveProductBar("S301", "Vintage Denim Jacket (Fashion)", 30);
        barMilk = new LiveProductBar("G401", "Farm Fresh Organic Milk (Grocery)", 80);

        barsList.add(barBurger);
        barsList.add(barEarbuds);
        barsList.add(barJacket);
        barsList.add(barMilk);

        bodyCard.add(barsList, BorderLayout.CENTER);
        centerContent.add(bodyCard, BorderLayout.CENTER);

        add(centerContent, BorderLayout.CENTER);

        // Polling Timer for live UI sync on EDT (every 500ms)
        pollingTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mediator.isSimulationActive()) {
                    updateUiState();
                    refreshBars();
                }
            }
        });
        pollingTimer.start();

        updateUiState();
        refreshBars();
    }

    private JPanel createCounterCard(String title, JLabel valLabel, Color col, String desc) {
        JPanel card = Theme.createCard();
        card.setLayout(new BorderLayout(0, 6));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_HEADER);
        titleLbl.setForeground(Theme.textMuted());
        card.add(titleLbl, BorderLayout.NORTH);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valLabel.setForeground(col);
        card.add(valLabel, BorderLayout.CENTER);

        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(Theme.FONT_SMALL);
        descLbl.setForeground(Theme.textMuted());
        card.add(descLbl, BorderLayout.SOUTH);

        return card;
    }

    private void updateUiState() {
        boolean running = mediator.isSimulationActive();
        if (running) {
            toggleSimBtn.setText("|| STOP SIMULATION");
            toggleSimBtn.setBackground(Theme.RED_ALERT);
            statusPill.setText("STATUS: RUNNING [LIVE]");
            statusPill.setForeground(Theme.GREEN_SAFE);
        } else {
            toggleSimBtn.setText("> START LIVE SIMULATION");
            toggleSimBtn.setBackground(Theme.GREEN_SAFE);
            statusPill.setText("STATUS: IDLE");
            statusPill.setForeground(Theme.textMuted());
        }

        SimulationService sim = mediator.getSimulationService();
        if (sim != null) {
            lblTicks.setText(String.valueOf(sim.getSimulationTicks()));
            lblPurchases.setText(String.valueOf(sim.getSuccessfulPurchases()));
            lblStockouts.setText(String.valueOf(sim.getOutOfStockIncidents()));
            lblLastEvent.setText("LATEST EVENT: " + sim.getLastEvent());
        }
    }

    private void refreshBars() {
        barBurger.updateStock();
        barEarbuds.updateStock();
        barJacket.updateStock();
        barMilk.updateStock();
    }

    @Override
    public void onInventoryUpdated() {
        updateUiState();
        refreshBars();
    }

    // Visual Real-Time Depletion Stock Bar
    private class LiveProductBar extends JPanel {
        private final String productId;
        private final String displayName;
        private final int maxCap;
        private int currentStock = 0;

        LiveProductBar(String productId, String displayName, int maxCap) {
            this.productId = productId;
            this.displayName = displayName;
            this.maxCap = maxCap;
            setOpaque(false);
            setPreferredSize(new Dimension(800, 38));
            updateStock();
        }

        void updateStock() {
            try {
                Product p = mediator.getInventoryManager().getProductById(productId);
                if (p != null) {
                    this.currentStock = p.getStock();
                    repaint();
                }
            } catch (Exception ignored) {
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Label
            g2.setColor(Theme.text());
            g2.setFont(Theme.FONT_BODY_BOLD);
            g2.drawString(displayName, 10, 24);

            int barX = 320;
            int barW = w - barX - 100;
            int barH = 20;
            int barY = 8;

            // Bar background
            g2.setColor(Theme.isDarkMode() ? new Color(25, 36, 56) : new Color(220, 228, 238));
            g2.fillRoundRect(barX, barY, barW, barH, 8, 8);

            // Fill
            double ratio = Math.min(1.0, (double) currentStock / maxCap);
            int fillW = (int) (barW * ratio);
            Color col = ratio <= 0.25 ? Theme.RED_ALERT : (ratio <= 0.5 ? Theme.YELLOW_WARN : Theme.GREEN_SAFE);
            g2.setColor(col);
            g2.fillRoundRect(barX, barY, Math.max(6, fillW), barH, 8, 8);

            // Stock Count text
            g2.setColor(Theme.text());
            g2.setFont(Theme.FONT_BODY_BOLD);
            g2.drawString(currentStock + " / " + maxCap + " units", w - 90, 24);

            g2.dispose();
        }
    }
}
