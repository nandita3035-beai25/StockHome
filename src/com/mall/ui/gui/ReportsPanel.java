package com.mall.ui.gui;

import com.mall.model.Product;
import com.mall.model.ShopZone;
import com.mall.service.DemandPredictor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

/**
 * Screen 5: Strategic Reports & Multiplier Sandbox.
 * 
 * Features:
 * - Real-time Weekend / Festival Demand Multiplier slider (1.0x - 2.0x)
 * - Tabs for Top Sellers, Slow Movers, Dead Stock, and Multi-Dimensional 2D Sales Matrix
 */
public class ReportsPanel extends JPanel implements InventoryListener {

    private final GuiMediator mediator;
    private final JSlider multiplierSlider;
    private final JLabel multiplierValueLabel;

    // Report Tables
    private final JTable topSellersTable;
    private final DefaultTableModel topSellersModel;

    private final JTable slowMoversTable;
    private final DefaultTableModel slowMoversModel;

    private final JTable deadStockTable;
    private final DefaultTableModel deadStockModel;
    private final JLabel deadCapitalLabel;

    private final JTable matrixTable;
    private final DefaultTableModel matrixModel;

    public ReportsPanel(final GuiMediator mediator) {
        this.mediator = mediator;
        mediator.addInventoryListener(this);

        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Top Event Multiplier Control Card
        JPanel multiplierCard = Theme.createCard();
        multiplierCard.setLayout(new BorderLayout(16, 0));

        JPanel multLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        multLeft.setOpaque(false);

        JLabel multTitle = new JLabel("EVENT DEMAND MULTIPLIER SLIDER:");
        multTitle.setFont(Theme.FONT_BODY_BOLD);
        multTitle.setForeground(Theme.GOLD);
        multLeft.add(multTitle);

        multiplierValueLabel = new JLabel("1.00x (Normal Weekday Baseline)");
        multiplierValueLabel.setFont(Theme.FONT_BODY_BOLD);
        multiplierValueLabel.setForeground(Theme.TEAL);
        multLeft.add(multiplierValueLabel);

        multiplierCard.add(multLeft, BorderLayout.WEST);

        // Slider from 100 to 200 (1.00x to 2.00x)
        multiplierSlider = new JSlider(100, 200, 100);
        multiplierSlider.setOpaque(false);
        multiplierSlider.setPreferredSize(new Dimension(300, 30));
        multiplierSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double val = multiplierSlider.getValue() / 100.0;
                String desc = val >= 1.75 ? "Festival Mega Rush!" :
                              val >= 1.4 ? "Weekend Footfall Surge" :
                              val > 1.0 ? "Moderate Spike" : "Normal Baseline";

                multiplierValueLabel.setText(String.format("%.2fx (%s)", val, desc));
                mediator.getInventoryManager().getDemandPredictor().setDemandMultiplier(val);
                mediator.notifyInventoryUpdated();
            }
        });
        multiplierCard.add(multiplierSlider, BorderLayout.EAST);

        add(multiplierCard, BorderLayout.NORTH);

        // 2. Sub-Tabs for Reports
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.FONT_BODY_BOLD);
        tabbedPane.setBackground(Theme.cardBg());
        tabbedPane.setForeground(Theme.GOLD);

        // Tab 1: Top Sellers
        topSellersModel = new DefaultTableModel(new String[]{"Rank", "SKU ID", "Product Name", "Category", "Total Units Sold", "Unit Price", "Est. Revenue"}, 0);
        topSellersTable = createStyledTable(topSellersModel);
        tabbedPane.addTab("Top Sellers (Volume)", new JScrollPane(topSellersTable));

        // Tab 2: Slow Movers
        slowMoversModel = new DefaultTableModel(new String[]{"Rank", "SKU ID", "Product Name", "Category", "Current Stock", "Sales Velocity (Units/Day)", "Days Left"}, 0);
        slowMoversTable = createStyledTable(slowMoversModel);
        tabbedPane.addTab("Slow Movers (Low Velocity)", new JScrollPane(slowMoversTable));

        // Tab 3: Dead Stock
        JPanel deadStockPanel = new JPanel(new BorderLayout());
        deadStockPanel.setOpaque(false);
        deadStockModel = new DefaultTableModel(new String[]{"SKU ID", "Product Name", "Category", "Unit Price", "Stuck Stock Units", "Tied-Up Working Capital"}, 0);
        deadStockTable = createStyledTable(deadStockModel);
        deadStockPanel.add(new JScrollPane(deadStockTable), BorderLayout.CENTER);

        deadCapitalLabel = new JLabel("Total Tied-Up Capital in Dead Stock: ₹0.00", JLabel.RIGHT);
        deadCapitalLabel.setFont(Theme.FONT_BODY_BOLD);
        deadCapitalLabel.setForeground(Theme.RED_ALERT);
        deadCapitalLabel.setBorder(new EmptyBorder(8, 10, 8, 10));
        deadStockPanel.add(deadCapitalLabel, BorderLayout.SOUTH);
        tabbedPane.addTab("Dead Stock (Zero Sales)", deadStockPanel);

        // Tab 4: 2D Sales Matrix
        String[] matrixCols = new String[]{
            "Zone", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "D10", "D11", "D12", "D13", "D14", "ZONE TOTAL"
        };
        matrixModel = new DefaultTableModel(matrixCols, 0);
        matrixTable = createStyledTable(matrixModel);
        tabbedPane.addTab("2D Zone Matrix (14-Day)", new JScrollPane(matrixTable));

        add(tabbedPane, BorderLayout.CENTER);

        refreshReports();
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(Theme.cardBorder());
        table.setBackground(Theme.cardBg());
        table.setForeground(Theme.text());
        table.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
        table.getTableHeader().setBackground(Theme.headerBg());
        table.getTableHeader().setForeground(Theme.GOLD);
        return table;
    }

    @Override
    public void onInventoryUpdated() {
        refreshReports();
    }

    private void refreshReports() {
        // 1. Top Sellers
        topSellersModel.setRowCount(0);
        List<Product> top = mediator.getInventoryManager().getTopSellersReport();
        int rank = 1;
        for (Product p : top) {
            double rev = p.getTotalUnitsSold() * p.getPrice();
            topSellersModel.addRow(new Object[]{
                "#" + rank++,
                p.getId(),
                p.getName(),
                p.getCategory(),
                p.getTotalUnitsSold() + " units",
                String.format("₹%.2f", p.getPrice()),
                String.format("₹%.2f", rev)
            });
        }

        // 2. Slow Movers
        slowMoversModel.setRowCount(0);
        List<Product> slow = mediator.getInventoryManager().getSlowMoversReport();
        rank = 1;
        for (Product p : slow) {
            int days = mediator.getInventoryManager().getDemandPredictor().estimateDaysUntilStockout(p);
            String daysStr = days >= 999 ? "No Sales" : (days + " days");
            slowMoversModel.addRow(new Object[]{
                "#" + rank++,
                p.getId(),
                p.getName(),
                p.getCategory(),
                p.getStock() + " units",
                String.format("%.2f units/day", p.averageDailySales()),
                daysStr
            });
        }

        // 3. Dead Stock
        deadStockModel.setRowCount(0);
        List<Product> dead = mediator.getInventoryManager().getDeadStockReport();
        double totalDeadCapital = 0.0;
        for (Product p : dead) {
            double cap = p.getStock() * p.getPrice();
            totalDeadCapital += cap;
            deadStockModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategory(),
                String.format("₹%.2f", p.getPrice()),
                p.getStock() + " units",
                String.format("₹%.2f", cap)
            });
        }
        deadCapitalLabel.setText(String.format("Total Capital Locked in Dead Stock: ₹%.2f", totalDeadCapital));

        // 4. 2D Zone Sales Matrix
        matrixModel.setRowCount(0);
        Map<String, ShopZone> zones = mediator.getInventoryManager().getZones();
        DemandPredictor predictor = mediator.getInventoryManager().getDemandPredictor();

        int[][] matrix = predictor.buildZoneSalesMatrix(zones, 14);
        int[] zoneTotals = predictor.calculateZoneTotals(matrix);
        int[] dailyTotals = predictor.calculateMallDailyTotals(matrix);

        String[] zoneNames = {"Food Court", "Electronics", "Fashion Hub", "Super Grocery"};
        for (int r = 0; r < matrix.length; r++) {
            Object[] row = new Object[16];
            row[0] = zoneNames[r];
            for (int c = 0; c < 14; c++) {
                row[c + 1] = matrix[r][c];
            }
            row[15] = zoneTotals[r];
            matrixModel.addRow(row);
        }

        // Add daily totals summary row
        Object[] totalRow = new Object[16];
        totalRow[0] = "MALL TOTAL DAILY";
        int grandTotal = 0;
        for (int c = 0; c < dailyTotals.length; c++) {
            totalRow[c + 1] = dailyTotals[c];
            grandTotal += dailyTotals[c];
        }
        totalRow[15] = grandTotal;
        matrixModel.addRow(totalRow);
    }
}
