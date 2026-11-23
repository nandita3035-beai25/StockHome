package com.mall.ui.gui;

import com.mall.exceptions.InvalidQuantityException;
import com.mall.exceptions.ProductNotFoundException;
import com.mall.model.Product;
import com.mall.model.ReorderSuggestion;
import com.mall.model.ShopZone;
import com.mall.model.UrgencyLevel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

/**
 * Screen 4: Intelligent Reorder Queue & Automated Replenishment.
 * 
 * Demonstrates:
 * - java.util.PriorityQueue (Min-Heap) ordered by Urgency and Days Left
 * - Polling reorder suggestions in natural priority sequence
 * - Interactive "Mark as Ordered" button triggering courier delivery truck animation
 */
public class ReorderPanel extends JPanel implements InventoryListener {

    private final GuiMediator mediator;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final List<ReorderSuggestion> currentSuggestions = new ArrayList<ReorderSuggestion>();

    public ReorderPanel(final GuiMediator mediator) {
        this.mediator = mediator;
        mediator.addInventoryListener(this);

        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Header Card
        JPanel topHeader = Theme.createCard();
        topHeader.setLayout(new BorderLayout());

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerLeft.setOpaque(false);

        JLabel title = new JLabel("SUPPLY-CHAIN REORDER QUEUE (PRIORITY QUEUE)");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.GOLD);
        headerLeft.add(title);

        JLabel subtitle = new JLabel("  |  Min-Heap ranked by Urgency Level and Stockout Timeline");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.textMuted());
        headerLeft.add(subtitle);

        topHeader.add(headerLeft, BorderLayout.WEST);

        JButton refreshBtn = Theme.createButton("Refresh Queue", Theme.TEAL);
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshQueueData();
            }
        });
        topHeader.add(refreshBtn, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);

        // Center Table
        String[] columnNames = {
            "Rank", "Urgency", "Product ID", "Product Name", "Current Stock",
            "Days Left", "Suggested Qty", "Replenishment Rationale", "Action"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // Only Action button is interactive
            }
        };

        table = new JTable(tableModel);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(Theme.cardBorder());
        table.setBackground(Theme.cardBg());
        table.setForeground(Theme.text());
        table.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
        table.getTableHeader().setBackground(Theme.headerBg());
        table.getTableHeader().setForeground(Theme.GOLD);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(75);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(85);
        table.getColumnModel().getColumn(5).setPreferredWidth(75);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(320);
        table.getColumnModel().getColumn(8).setPreferredWidth(130);

        // Custom Renderers
        table.getColumnModel().getColumn(1).setCellRenderer(new UrgencyBadgeRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(8).setCellEditor(new ButtonEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Theme.cardBg());
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        refreshQueueData();
    }

    @Override
    public void onInventoryUpdated() {
        refreshQueueData();
    }

    private void refreshQueueData() {
        tableModel.setRowCount(0);
        currentSuggestions.clear();

        // Generate Min-Heap PriorityQueue
        PriorityQueue<ReorderSuggestion> pq = mediator.getInventoryManager().generateUrgencyQueue();
        int rank = 1;

        while (!pq.isEmpty()) {
            ReorderSuggestion sug = pq.poll(); // PriorityQueue poll() extracts root element
            currentSuggestions.add(sug);

            String daysStr = sug.getDaysUntilStockout() >= 999 ? "N/A" : (sug.getDaysUntilStockout() + " d");

            tableModel.addRow(new Object[]{
                "#" + rank++,
                sug.getUrgency(),
                sug.getProduct().getId(),
                sug.getProduct().getName(),
                sug.getCurrentStock(),
                daysStr,
                sug.getSuggestedQuantity() > 0 ? (sug.getSuggestedQuantity() + " units") : "None",
                sug.getRationale(),
                sug.getSuggestedQuantity() > 0 ? "Mark as Ordered" : "Stock Healthy"
            });
        }
    }

    private void handleOrderAction(int row) {
        if (row < 0 || row >= currentSuggestions.size()) return;
        ReorderSuggestion sug = currentSuggestions.get(row);

        if (sug.getSuggestedQuantity() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Product '" + sug.getProduct().getName() + "' currently has healthy stock above reorder point.",
                    "No Reorder Needed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm purchase order for:\n" + sug.getProduct().getName() + " [" + sug.getProduct().getId() + "]\n" +
                "Quantity: " + sug.getSuggestedQuantity() + " units\n" +
                "Supplier Lead Time: " + sug.getProduct().calculateLeadTimeDays() + " days",
                "Approve Purchase Order", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                mediator.getInventoryManager().restockProduct(sug.getProduct().getId(), sug.getSuggestedQuantity());

                // Find host zone for delivery truck animation
                String zoneCode = "FOOD";
                for (ShopZone z : mediator.getInventoryManager().getZones().values()) {
                    if (z.hasProduct(sug.getProduct().getId())) {
                        zoneCode = z.getZoneCode();
                        break;
                    }
                }

                // Dispatch animated truck on map
                mediator.dispatchDeliveryTruck(zoneCode);

                JOptionPane.showMessageDialog(this,
                        "Order successfully placed!\n" +
                        sug.getSuggestedQuantity() + " units dispatched to " + zoneCode + ".\n" +
                        "Stock replenished to: " + sug.getProduct().getStock(),
                        "Order Dispatched", JOptionPane.INFORMATION_MESSAGE);

                mediator.notifyInventoryUpdated();

            } catch (ProductNotFoundException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (InvalidQuantityException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // CUSTOM RENDERERS & EDITORS
    // =========================================================================

    private static class UrgencyBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            final UrgencyLevel level = (value instanceof UrgencyLevel) ? (UrgencyLevel) value : UrgencyLevel.SAFE;

            JPanel pillPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Color bgCol = level == UrgencyLevel.CRITICAL ? new Color(255, 59, 48, 40) :
                                  level == UrgencyLevel.WARNING ? new Color(255, 179, 0, 40) : new Color(52, 199, 89, 40);
                    Color fgCol = level == UrgencyLevel.CRITICAL ? Theme.RED_ALERT :
                                  level == UrgencyLevel.WARNING ? Theme.YELLOW_WARN : Theme.GREEN_SAFE;

                    g2.setColor(bgCol);
                    g2.fillRoundRect(6, 4, getWidth() - 12, getHeight() - 8, 10, 10);
                    g2.setColor(fgCol);
                    g2.drawRoundRect(6, 4, getWidth() - 12, getHeight() - 8, 10, 10);

                    g2.setFont(Theme.FONT_BODY_BOLD);
                    String txt = level.getLabel();
                    int tw = g2.getFontMetrics().stringWidth(txt);
                    g2.drawString(txt, (getWidth() - tw) / 2, (getHeight() + g2.getFontMetrics().getAscent()) / 2 - 2);
                    g2.dispose();
                }
            };
            pillPanel.setOpaque(true);
            pillPanel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return pillPanel;
        }
    }

    private class ButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String txt = (value != null) ? value.toString() : "Order";
            boolean isOrder = "Mark as Ordered".equals(txt);
            JButton btn = Theme.createButton(txt, isOrder ? Theme.GREEN_SAFE : new Color(75, 90, 110));
            btn.setFont(Theme.FONT_SMALL);
            return btn;
        }
    }

    private class ButtonEditor extends javax.swing.AbstractCellEditor implements TableCellEditor, ActionListener {
        private final JButton btn;
        private int currentRow;

        ButtonEditor() {
            btn = Theme.createButton("Mark as Ordered", Theme.GREEN_SAFE);
            btn.setFont(Theme.FONT_SMALL);
            btn.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.currentRow = row;
            String txt = (value != null) ? value.toString() : "Order";
            btn.setText(txt);
            btn.setEnabled("Mark as Ordered".equals(txt));
            return btn;
        }

        @Override
        public Object getCellEditorValue() {
            return btn.getText();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped();
            handleOrderAction(currentRow);
        }
    }
}
