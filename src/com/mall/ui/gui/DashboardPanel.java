package com.mall.ui.gui;

import com.mall.model.Product;
import com.mall.model.ShopZone;
import com.mall.model.UrgencyLevel;
import com.mall.service.InventoryManager;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Screen 2: Executive Inventory Analytics & Custom Charts Dashboard.
 * 
 * Features:
 * - Animated count-up summary metric cards
 * - Custom Graphics2D Bar Chart of Top Sellers
 * - Custom Graphics2D Line Chart with gradient fill of 14-day Mall Footfall/Sales
 * - Visual days-until-stockout horizontal timeline gauges
 */
public class DashboardPanel extends JPanel implements InventoryListener {

    private final GuiMediator mediator;

    // Animated count-up targets
    private int targetTotalProducts = 0;
    private int targetCriticalCount = 0;
    private int targetWarningCount = 0;
    private int targetTotalSold = 0;

    private int animTotalProducts = 0;
    private int animCriticalCount = 0;
    private int animWarningCount = 0;
    private int animTotalSold = 0;

    private final JLabel lblValProducts;
    private final JLabel lblValCritical;
    private final JLabel lblValWarning;
    private final JLabel lblValSold;

    private final Timer countUpTimer;

    public DashboardPanel(final GuiMediator mediator) {
        this.mediator = mediator;
        mediator.addInventoryListener(this);

        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Top KPI Summary Cards Row (4 Cards)
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow.setOpaque(false);

        lblValProducts = new JLabel("0", SwingConstants.CENTER);
        lblValCritical = new JLabel("0", SwingConstants.CENTER);
        lblValWarning = new JLabel("0", SwingConstants.CENTER);
        lblValSold = new JLabel("0", SwingConstants.CENTER);

        kpiRow.add(createKpiCard("TOTAL PRODUCTS", lblValProducts, Theme.GOLD, "Active retail catalog SKUs"));
        kpiRow.add(createKpiCard("CRITICAL ALERTS", lblValCritical, Theme.RED_ALERT, "Depletion in <= 2 days"));
        kpiRow.add(createKpiCard("WARNING BUFFER", lblValWarning, Theme.YELLOW_WARN, "Depletion in <= 5 days"));
        kpiRow.add(createKpiCard("UNITS SOLD", lblValSold, Theme.TEAL, "Total units sold to date"));

        add(kpiRow, BorderLayout.NORTH);

        // 2. Charts Container (Scrollable)
        JPanel chartsContainer = new JPanel(new GridLayout(2, 1, 0, 16));
        chartsContainer.setOpaque(false);

        // Chart Row 1: Left = Top Sellers Bar Chart, Right = Days Left Horizontal Gauges
        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);
        row1.add(new TopSellersBarChartPanel());
        row1.add(new StockoutGaugePanel());
        chartsContainer.add(row1);

        // Chart Row 2: 14-Day Mall Daily Sales Line Chart
        chartsContainer.add(new MallDailySalesLineChartPanel());

        JScrollPane scrollPane = new JScrollPane(chartsContainer);
        scrollPane.getViewport().setBackground(Theme.bg());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Timer for count-up animation
        countUpTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean done = true;
                if (animTotalProducts < targetTotalProducts) {
                    animTotalProducts += Math.max(1, (targetTotalProducts - animTotalProducts) / 5);
                    done = false;
                }
                if (animCriticalCount < targetCriticalCount) {
                    animCriticalCount += Math.max(1, (targetCriticalCount - animCriticalCount) / 5);
                    done = false;
                }
                if (animWarningCount < targetWarningCount) {
                    animWarningCount += Math.max(1, (targetWarningCount - animWarningCount) / 5);
                    done = false;
                }
                if (animTotalSold < targetTotalSold) {
                    animTotalSold += Math.max(1, (targetTotalSold - animTotalSold) / 5);
                    done = false;
                }

                lblValProducts.setText(String.valueOf(animTotalProducts));
                lblValCritical.setText(String.valueOf(animCriticalCount));
                lblValWarning.setText(String.valueOf(animWarningCount));
                lblValSold.setText(String.valueOf(animTotalSold));

                if (done) {
                    countUpTimer.stop();
                }
            }
        });

        calculateMetrics();
        startCountUp();
    }

    private JPanel createKpiCard(String title, JLabel valLabel, Color accentColor, String sub) {
        JPanel card = Theme.createCard();
        card.setLayout(new BorderLayout(0, 6));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_HEADER);
        titleLbl.setForeground(Theme.textMuted());
        card.add(titleLbl, BorderLayout.NORTH);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valLabel.setForeground(accentColor);
        card.add(valLabel, BorderLayout.CENTER);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(Theme.textMuted());
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    private void calculateMetrics() {
        InventoryManager mgr = mediator.getInventoryManager();
        targetTotalProducts = mgr.getTotalProductCount();

        int crit = 0;
        int warn = 0;
        int totalSold = 0;

        for (Product p : mgr.getAlphabeticalCatalog().values()) {
            totalSold += p.getTotalUnitsSold();
            int days = mgr.getDemandPredictor().estimateDaysUntilStockout(p);
            if (days <= 2) {
                crit++;
            } else if (days <= 5) {
                warn++;
            }
        }

        targetCriticalCount = crit;
        targetWarningCount = warn;
        targetTotalSold = totalSold;
    }

    private void startCountUp() {
        animTotalProducts = 0;
        animCriticalCount = 0;
        animWarningCount = 0;
        animTotalSold = 0;
        countUpTimer.start();
    }

    @Override
    public void onInventoryUpdated() {
        calculateMetrics();
        lblValProducts.setText(String.valueOf(targetTotalProducts));
        lblValCritical.setText(String.valueOf(targetCriticalCount));
        lblValWarning.setText(String.valueOf(targetWarningCount));
        lblValSold.setText(String.valueOf(targetTotalSold));
        repaint();
    }

    // =========================================================================
    // CUSTOM GRAPHICS2D CHART 1: TOP SELLERS BAR CHART
    // =========================================================================
    private class TopSellersBarChartPanel extends JPanel {
        TopSellersBarChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(500, 260));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background card
            g2.setColor(Theme.cardBg());
            g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
            g2.setColor(Theme.cardBorder());
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

            // Title
            g2.setColor(Theme.GOLD);
            g2.setFont(Theme.FONT_HEADER);
            g2.drawString("TOP SELLERS BY VOLUME (TOTAL UNITS)", 20, 28);

            List<Product> top = mediator.getInventoryManager().getTopSellersReport();
            if (top.isEmpty()) {
                g2.dispose();
                return;
            }

            int count = Math.min(5, top.size());
            int maxUnits = Math.max(1, top.get(0).getTotalUnitsSold());

            int startY = 55;
            int barHeight = 22;
            int gap = 16;
            int maxBarWidth = w - 240;

            Color[] barColors = {
                new Color(0, 229, 255),
                new Color(52, 199, 89),
                new Color(229, 184, 66),
                new Color(175, 110, 245),
                new Color(255, 140, 0)
            };

            for (int i = 0; i < count; i++) {
                Product p = top.get(i);
                int y = startY + i * (barHeight + gap);

                // Label
                g2.setColor(Theme.text());
                g2.setFont(Theme.FONT_SMALL);
                String label = p.getName();
                if (label.length() > 18) label = label.substring(0, 16) + "..";
                g2.drawString(label, 20, y + 16);

                // Bar
                int barW = (int) (((double) p.getTotalUnitsSold() / maxUnits) * maxBarWidth);
                barW = Math.max(6, barW);

                g2.setColor(barColors[i % barColors.length]);
                g2.fillRoundRect(160, y, barW, barHeight, 6, 6);

                // Value Text
                g2.setColor(Theme.text());
                g2.drawString(p.getTotalUnitsSold() + " units", 170 + barW, y + 16);
            }

            g2.dispose();
        }
    }

    // =========================================================================
    // CUSTOM GRAPHICS2D CHART 2: DAYS-UNTIL-STOCKOUT HORIZONTAL GAUGES
    // =========================================================================
    private class StockoutGaugePanel extends JPanel {
        StockoutGaugePanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(500, 260));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(Theme.cardBg());
            g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
            g2.setColor(Theme.cardBorder());
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

            g2.setColor(Theme.RED_ALERT);
            g2.setFont(Theme.FONT_HEADER);
            g2.drawString("STOCKOUT DEPLETION HORIZON (DAYS LEFT)", 20, 28);

            List<Product> list = mediator.getInventoryManager().getTopSellersReport();
            List<Product> urgent = new ArrayList<Product>();
            for (Product p : list) {
                int days = mediator.getInventoryManager().getDemandPredictor().estimateDaysUntilStockout(p);
                if (days < 999) {
                    urgent.add(p);
                }
            }

            int count = Math.min(5, urgent.size());
            int startY = 55;
            int barHeight = 22;
            int gap = 16;
            int maxBarWidth = w - 240;

            for (int i = 0; i < count; i++) {
                Product p = urgent.get(i);
                int days = mediator.getInventoryManager().getDemandPredictor().estimateDaysUntilStockout(p);
                int y = startY + i * (barHeight + gap);

                g2.setColor(Theme.text());
                g2.setFont(Theme.FONT_SMALL);
                String label = p.getName();
                if (label.length() > 18) label = label.substring(0, 16) + "..";
                g2.drawString(label, 20, y + 16);

                // Days bar (scaled up to 15 days max)
                int barW = Math.min(maxBarWidth, (int) ((days / 15.0) * maxBarWidth));
                barW = Math.max(8, barW);

                Color barCol = days <= 2 ? Theme.RED_ALERT : (days <= 5 ? Theme.YELLOW_WARN : Theme.GREEN_SAFE);
                g2.setColor(barCol);
                g2.fillRoundRect(160, y, barW, barHeight, 6, 6);

                g2.setColor(Theme.text());
                g2.drawString(days + " days left", 170 + barW, y + 16);
            }

            g2.dispose();
        }
    }

    // =========================================================================
    // CUSTOM GRAPHICS2D CHART 3: 14-DAY MALL DAILY SALES LINE CHART
    // =========================================================================
    private class MallDailySalesLineChartPanel extends JPanel {
        MallDailySalesLineChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(1000, 260));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(Theme.cardBg());
            g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
            g2.setColor(Theme.cardBorder());
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

            g2.setColor(Theme.TEAL);
            g2.setFont(Theme.FONT_HEADER);
            g2.drawString("14-DAY MALL-WIDE AGGREGATED DAILY SALES TREND (2D MATRIX SUM)", 20, 28);

            // Compute daily totals from 2D matrix
            Map<String, ShopZone> zones = mediator.getInventoryManager().getZones();
            int[][] matrix = mediator.getInventoryManager().getDemandPredictor().buildZoneSalesMatrix(zones, 14);
            int[] dailyTotals = mediator.getInventoryManager().getDemandPredictor().calculateMallDailyTotals(matrix);

            if (dailyTotals.length == 0) {
                g2.dispose();
                return;
            }

            int maxSale = 1;
            for (int v : dailyTotals) {
                if (v > maxSale) maxSale = v;
            }

            int paddingLeft = 50;
            int paddingRight = 40;
            int paddingTop = 60;
            int paddingBottom = 45;

            int plotW = w - paddingLeft - paddingRight;
            int plotH = h - paddingTop - paddingBottom;

            // Draw horizontal grid lines
            g2.setColor(Theme.cardBorder());
            g2.setStroke(new BasicStroke(1));
            for (int i = 0; i <= 4; i++) {
                int y = paddingTop + (int) (plotH * (i / 4.0));
                g2.drawLine(paddingLeft, y, paddingLeft + plotW, y);

                int val = (int) (maxSale * (1.0 - (i / 4.0)));
                g2.setColor(Theme.textMuted());
                g2.setFont(Theme.FONT_SMALL);
                g2.drawString(String.valueOf(val), 15, y + 4);
                g2.setColor(Theme.cardBorder());
            }

            // Plot data points
            int n = dailyTotals.length;
            int[] xPoints = new int[n];
            int[] yPoints = new int[n];

            for (int i = 0; i < n; i++) {
                xPoints[i] = paddingLeft + (int) ((i / (double) (n - 1)) * plotW);
                yPoints[i] = paddingTop + plotH - (int) (((double) dailyTotals[i] / maxSale) * plotH);
            }

            // Draw gradient fill under curve
            int[] polyX = new int[n + 2];
            int[] polyY = new int[n + 2];
            System.arraycopy(xPoints, 0, polyX, 0, n);
            System.arraycopy(yPoints, 0, polyY, 0, n);
            polyX[n] = xPoints[n - 1];
            polyY[n] = paddingTop + plotH;
            polyX[n + 1] = xPoints[0];
            polyY[n + 1] = paddingTop + plotH;

            g2.setColor(new Color(0, 229, 255, 35));
            g2.fillPolygon(polyX, polyY, n + 2);

            // Draw main curve line
            g2.setColor(Theme.TEAL);
            g2.setStroke(new BasicStroke(2.8f));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
            }

            // Draw data points & X-axis Day labels
            for (int i = 0; i < n; i++) {
                g2.setColor(Theme.TEAL);
                g2.fillOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
                g2.setColor(Color.WHITE);
                g2.drawOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);

                // Day Label
                g2.setColor(Theme.textMuted());
                g2.setFont(Theme.FONT_SMALL);
                String dayLbl = "D" + (i + 1);
                g2.drawString(dayLbl, xPoints[i] - 8, paddingTop + plotH + 18);
            }

            g2.dispose();
        }
    }
}
