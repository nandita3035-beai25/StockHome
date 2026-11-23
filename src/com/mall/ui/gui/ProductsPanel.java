package com.mall.ui.gui;

import com.mall.exceptions.InvalidQuantityException;
import com.mall.exceptions.OutOfStockException;
import com.mall.exceptions.ProductNotFoundException;
import com.mall.model.NonPerishableProduct;
import com.mall.model.PerishableProduct;
import com.mall.model.Product;
import com.mall.model.SaleTransaction;
import com.mall.model.ShopZone;
import com.mall.model.UrgencyLevel;
import com.mall.service.InventoryManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Screen 3: Interactive Products Catalog, Inventory Transactions, and Management.
 * 
 * Demonstrates:
 * - JTable with custom cell renderers for visual progress bars and colored urgency badges
 * - Case-insensitive search and zone filtering
 * - Transaction dialogs catching custom checked exceptions (OutOfStockException, etc.)
 */
public class ProductsPanel extends JPanel implements InventoryListener {

    private final GuiMediator mediator;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField searchField;
    private final JComboBox<String> zoneFilterCombo;
    private String activeZoneFilter = "ALL";

    public ProductsPanel(final GuiMediator mediator) {
        this.mediator = mediator;
        mediator.addInventoryListener(this);

        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Filter Bar
        JPanel topBar = Theme.createCard();
        topBar.setLayout(new BorderLayout());

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftFilter.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(Theme.FONT_BODY_BOLD);
        searchLbl.setForeground(Theme.text());
        leftFilter.add(searchLbl);

        searchField = new JTextField(16);
        searchField.setFont(Theme.FONT_BODY);
        searchField.setBackground(Theme.inputBg());
        searchField.setForeground(Theme.text());
        searchField.setCaretColor(Theme.text());
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refreshTableData();
            }
        });
        leftFilter.add(searchField);

        JLabel zoneLbl = new JLabel("Zone:");
        zoneLbl.setFont(Theme.FONT_BODY_BOLD);
        zoneLbl.setForeground(Theme.text());
        leftFilter.add(zoneLbl);

        zoneFilterCombo = new JComboBox<String>(new String[]{"ALL ZONES", "FOOD COURT", "ELECTRONICS", "FASHION", "GROCERY"});
        zoneFilterCombo.setFont(Theme.FONT_BODY);
        zoneFilterCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = zoneFilterCombo.getSelectedIndex();
                String[] codes = {"ALL", "FOOD", "ELEC", "FASH", "GROC"};
                activeZoneFilter = codes[idx];
                refreshTableData();
            }
        });
        leftFilter.add(zoneFilterCombo);

        JButton resetBtn = Theme.createButton("Clear Filter", new Color(70, 85, 105));
        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.setText("");
                zoneFilterCombo.setSelectedIndex(0);
                activeZoneFilter = "ALL";
                refreshTableData();
            }
        });
        leftFilter.add(resetBtn);

        topBar.add(leftFilter, BorderLayout.WEST);

        // Action Buttons on Right
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionButtons.setOpaque(false);

        JButton recordSaleBtn = Theme.createButton("+ Record Sale", Theme.GREEN_SAFE);
        recordSaleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRecordSaleDialog();
            }
        });

        JButton restockBtn = Theme.createButton("^ Restock", Theme.TEAL);
        restockBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRestockDialog();
            }
        });

        JButton undoBtn = Theme.createButton("Undo Sale", new Color(130, 90, 180));
        undoBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUndoSale();
            }
        });

        JButton addProductBtn = Theme.createButton("+ Add Product", Theme.GOLD);
        addProductBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAddProductDialog();
            }
        });

        JButton removeProductBtn = Theme.createButton("- Remove", Theme.RED_ALERT);
        removeProductBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRemoveProduct();
            }
        });

        actionButtons.add(recordSaleBtn);
        actionButtons.add(restockBtn);
        actionButtons.add(undoBtn);
        actionButtons.add(addProductBtn);
        actionButtons.add(removeProductBtn);

        topBar.add(actionButtons, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table
        String[] columnNames = {
            "ID", "Product Name", "Zone Category", "Price ($)", "Stock Units",
            "Stock Level Bar", "Avg Daily", "Days Left", "Health Urgency", "Classification"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(Theme.cardBorder());
        table.setBackground(Theme.cardBg());
        table.setForeground(Theme.text());
        table.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
        table.getTableHeader().setBackground(Theme.headerBg());
        table.getTableHeader().setForeground(Theme.GOLD);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(75);
        table.getColumnModel().getColumn(4).setPreferredWidth(85);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
        table.getColumnModel().getColumn(8).setPreferredWidth(110);
        table.getColumnModel().getColumn(9).setPreferredWidth(110);

        // Custom Renderers
        table.getColumnModel().getColumn(5).setCellRenderer(new StockBarRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new UrgencyBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Theme.cardBg());
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        refreshTableData();
    }

    public void filterByZone(String zoneCode) {
        if ("FOOD".equals(zoneCode)) zoneFilterCombo.setSelectedIndex(1);
        else if ("ELEC".equals(zoneCode)) zoneFilterCombo.setSelectedIndex(2);
        else if ("FASH".equals(zoneCode)) zoneFilterCombo.setSelectedIndex(3);
        else if ("GROC".equals(zoneCode)) zoneFilterCombo.setSelectedIndex(4);
        else zoneFilterCombo.setSelectedIndex(0);

        this.activeZoneFilter = zoneCode != null ? zoneCode : "ALL";
        refreshTableData();
    }

    @Override
    public void onInventoryUpdated() {
        refreshTableData();
    }

    private void refreshTableData() {
        tableModel.setRowCount(0);
        String search = searchField.getText().trim();

        List<Product> products;
        if (!search.isEmpty()) {
            products = mediator.getInventoryManager().searchProducts(search);
        } else {
            products = new ArrayList<Product>(mediator.getInventoryManager().getAlphabeticalCatalog().values());
        }

        for (Product p : products) {
            // Apply zone filter
            if (!"ALL".equals(activeZoneFilter)) {
                ShopZone z = mediator.getInventoryManager().getZone(activeZoneFilter);
                if (z == null || !z.hasProduct(p.getId())) {
                    continue;
                }
            }

            int daysLeft = mediator.getInventoryManager().getDemandPredictor().estimateDaysUntilStockout(p);
            String daysStr = daysLeft >= 999 ? "No Sales" : (daysLeft + " days");
            UrgencyLevel urgency = mediator.getInventoryManager().getDemandPredictor().determineUrgency(daysLeft);
            String typeStr = (p instanceof PerishableProduct) ? "Perishable" : "Non-Perish";

            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategory(),
                String.format("$%.2f", p.getPrice()),
                p.getStock(),
                p.getStock(), // Visual stock bar
                String.format("%.1f", p.averageDailySales()),
                daysStr,
                urgency,
                typeStr
            });
        }
    }

    // =========================================================================
    // DIALOGS & ACTIONS (Checked Exceptions -> Friendly Dialogs)
    // =========================================================================

    private void openRecordSaleDialog() {
        int selectedRow = table.getSelectedRow();
        String defaultId = (selectedRow >= 0) ? (String) table.getValueAt(selectedRow, 0) : "";

        JTextField idField = new JTextField(defaultId, 12);
        JTextField qtyField = new JTextField("1", 8);

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.add(new JLabel("Product ID (e.g. F101):"));
        panel.add(idField);
        panel.add(new JLabel("Quantity to Sell:"));
        panel.add(qtyField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Record Sale Transaction",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String pid = idField.getText().trim().toUpperCase();
            String qtyStr = qtyField.getText().trim();
            try {
                int qty = Integer.parseInt(qtyStr);
                SaleTransaction tx = mediator.getInventoryManager().recordSale(pid, qty);
                JOptionPane.showMessageDialog(this,
                        "Sale Successful!\n" + tx.toString(),
                        "Transaction Complete", JOptionPane.INFORMATION_MESSAGE);
                mediator.notifyInventoryUpdated();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Quantity must be a valid positive whole number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (OutOfStockException oos) {
                JOptionPane.showMessageDialog(this,
                        "OUT OF STOCK ALERT:\n" + oos.getMessage() + "\n\nPlease check Reorder Queue to replenish stock.",
                        "Stockout Rejection", JOptionPane.WARNING_MESSAGE);
            } catch (ProductNotFoundException pnfe) {
                JOptionPane.showMessageDialog(this, pnfe.getMessage(), "Product Not Found", JOptionPane.ERROR_MESSAGE);
            } catch (InvalidQuantityException iqe) {
                JOptionPane.showMessageDialog(this, iqe.getMessage(), "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openRestockDialog() {
        int selectedRow = table.getSelectedRow();
        String defaultId = (selectedRow >= 0) ? (String) table.getValueAt(selectedRow, 0) : "";

        JTextField idField = new JTextField(defaultId, 12);
        JTextField qtyField = new JTextField("25", 8);

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.add(new JLabel("Product ID to Restock:"));
        panel.add(idField);
        panel.add(new JLabel("Units to Add:"));
        panel.add(qtyField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Restock Inventory",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String pid = idField.getText().trim().toUpperCase();
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                mediator.getInventoryManager().restockProduct(pid, qty);
                Product p = mediator.getInventoryManager().getProductById(pid);

                // Dispatch delivery courier truck on map
                String zoneCode = "FOOD";
                for (ShopZone z : mediator.getInventoryManager().getZones().values()) {
                    if (z.hasProduct(pid)) {
                        zoneCode = z.getZoneCode();
                        break;
                    }
                }
                mediator.dispatchDeliveryTruck(zoneCode);

                JOptionPane.showMessageDialog(this,
                        "Successfully restocked " + qty + " units of '" + p.getName() + "'.\nCurrent stock: " + p.getStock(),
                        "Restock Complete", JOptionPane.INFORMATION_MESSAGE);
                mediator.notifyInventoryUpdated();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric restock quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (ProductNotFoundException pnfe) {
                JOptionPane.showMessageDialog(this, pnfe.getMessage(), "Product Not Found", JOptionPane.ERROR_MESSAGE);
            } catch (InvalidQuantityException iqe) {
                JOptionPane.showMessageDialog(this, iqe.getMessage(), "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleUndoSale() {
        try {
            SaleTransaction reverted = mediator.getInventoryManager().undoLastSale();
            JOptionPane.showMessageDialog(this,
                    "Successfully Reverted Transaction:\n" + reverted.toString(),
                    "Undo Complete", JOptionPane.INFORMATION_MESSAGE);
            mediator.notifyInventoryUpdated();
        } catch (IllegalStateException ise) {
            JOptionPane.showMessageDialog(this, ise.getMessage(), "Undo Stack Empty", JOptionPane.INFORMATION_MESSAGE);
        } catch (ProductNotFoundException pnfe) {
            JOptionPane.showMessageDialog(this, pnfe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddProductDialog() {
        JTextField idField = new JTextField(10);
        JTextField nameField = new JTextField(16);
        JTextField priceField = new JTextField("19.99", 8);
        JTextField stockField = new JTextField("50", 8);
        JComboBox<String> zoneCombo = new JComboBox<String>(new String[]{"FOOD (Food Court)", "ELEC (Electronics)", "FASH (Fashion)", "GROC (Grocery)"});

        final JRadioButton radioPerishable = new JRadioButton("Perishable", true);
        final JRadioButton radioNonPerishable = new JRadioButton("Non-Perishable");
        ButtonGroup group = new ButtonGroup();
        group.add(radioPerishable);
        group.add(radioNonPerishable);

        final JLabel extraLabel = new JLabel("Shelf Life (Days):");
        final JTextField extraField = new JTextField("7", 8);

        radioPerishable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                extraLabel.setText("Shelf Life (Days):");
                extraField.setText("7");
            }
        });

        radioNonPerishable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                extraLabel.setText("Warranty (Months):");
                extraField.setText("12");
            }
        });

        JPanel panel = new JPanel(new GridLayout(7, 2, 8, 8));
        panel.add(new JLabel("Target Zone:"));
        panel.add(zoneCombo);
        panel.add(new JLabel("Product ID:"));
        panel.add(idField);
        panel.add(new JLabel("Product Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Retail Price ($):"));
        panel.add(priceField);
        panel.add(new JLabel("Initial Stock:"));
        panel.add(stockField);
        panel.add(radioPerishable);
        panel.add(radioNonPerishable);
        panel.add(extraLabel);
        panel.add(extraField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add New Product SKU", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String pid = idField.getText().trim().toUpperCase();
            String name = nameField.getText().trim();
            if (pid.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Product ID and Name cannot be blank.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double price = Double.parseDouble(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());
                int extraVal = Integer.parseInt(extraField.getText().trim());

                String[] zoneCodes = {"FOOD", "ELEC", "FASH", "GROC"};
                String zCode = zoneCodes[zoneCombo.getSelectedIndex()];
                String category = mediator.getInventoryManager().getZone(zCode).getCategory();

                Product newProduct;
                if (radioPerishable.isSelected()) {
                    newProduct = new PerishableProduct(pid, name, price, stock, category, extraVal);
                } else {
                    newProduct = new NonPerishableProduct(pid, name, price, stock, category, extraVal);
                }

                mediator.getInventoryManager().addProduct(zCode, newProduct);
                JOptionPane.showMessageDialog(this, "Product successfully registered to " + zCode + "!", "Product Added", JOptionPane.INFORMATION_MESSAGE);
                mediator.notifyInventoryUpdated();

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Price, Stock, and Parameter must be valid numbers.", "Numeric Format Error", JOptionPane.ERROR_MESSAGE);
            } catch (ProductNotFoundException pnfe) {
                JOptionPane.showMessageDialog(this, pnfe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleRemoveProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pid = (String) table.getValueAt(selectedRow, 0);
        String name = (String) table.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently remove:\n[" + pid + "] " + name + "?",
                "Confirm SKU Removal", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                mediator.getInventoryManager().removeProduct(pid);
                JOptionPane.showMessageDialog(this, "Product [" + pid + "] removed successfully.", "Removed", JOptionPane.INFORMATION_MESSAGE);
                mediator.notifyInventoryUpdated();
            } catch (ProductNotFoundException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // CUSTOM TABLE CELL RENDERERS
    // =========================================================================

    /**
     * Renders an animated-style stock bar inside the JTable cell.
     */
    private static class StockBarRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            final int stock = (value instanceof Integer) ? (Integer) value : 0;
            final int maxCapacity = 100;
            final int pct = Math.min(100, (int) Math.round(((double) stock / maxCapacity) * 100));

            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int h = getHeight() - 10;
                    int y = 5;
                    int w = getWidth() - 16;
                    int x = 8;

                    // Bar Background
                    g2.setColor(Theme.isDarkMode() ? new Color(25, 36, 56) : new Color(220, 228, 238));
                    g2.fillRoundRect(x, y, w, h, 8, 8);

                    // Bar Fill
                    Color fillColor = pct <= 25 ? Theme.RED_ALERT : (pct <= 50 ? Theme.YELLOW_WARN : Theme.GREEN_SAFE);
                    g2.setColor(fillColor);
                    int fillW = (int) (w * (pct / 100.0));
                    g2.fillRoundRect(x, y, Math.max(4, fillW), h, 8, 8);

                    // Label text
                    g2.setColor(Color.WHITE);
                    g2.setFont(Theme.FONT_SMALL);
                    String txt = stock + " (" + pct + "%)";
                    int tw = g2.getFontMetrics().stringWidth(txt);
                    g2.drawString(txt, x + (w - tw) / 2, y + (h + g2.getFontMetrics().getAscent()) / 2 - 2);

                    g2.dispose();
                }
            };
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return panel;
        }
    }

    /**
     * Renders a colored pill badge for Urgency Level (CRITICAL / WARNING / SAFE).
     */
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

                    Color bgCol;
                    Color fgCol;
                    if (level == UrgencyLevel.CRITICAL) {
                        bgCol = new Color(255, 59, 48, 40);
                        fgCol = Theme.RED_ALERT;
                    } else if (level == UrgencyLevel.WARNING) {
                        bgCol = new Color(255, 179, 0, 40);
                        fgCol = Theme.YELLOW_WARN;
                    } else {
                        bgCol = new Color(52, 199, 89, 40);
                        fgCol = Theme.GREEN_SAFE;
                    }

                    int arc = 10;
                    g2.setColor(bgCol);
                    g2.fillRoundRect(6, 4, getWidth() - 12, getHeight() - 8, arc, arc);
                    g2.setColor(fgCol);
                    g2.drawRoundRect(6, 4, getWidth() - 12, getHeight() - 8, arc, arc);

                    g2.setFont(Theme.FONT_BODY_BOLD);
                    String txt = level.getLabel();
                    int tw = g2.getFontMetrics().stringWidth(txt);
                    int tx = (getWidth() - tw) / 2;
                    int ty = (getHeight() + g2.getFontMetrics().getAscent()) / 2 - 2;
                    g2.drawString(txt, tx, ty);

                    g2.dispose();
                }
            };
            pillPanel.setOpaque(true);
            pillPanel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return pillPanel;
        }
    }
}
