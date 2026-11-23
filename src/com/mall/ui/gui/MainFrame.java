package com.mall.ui.gui;

import com.mall.service.InventoryManager;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Main Application Window for "Mall of Tomorrow" Graphical Interface.
 */
public class MainFrame extends JFrame implements GuiMediator.Navigator {

    private final GuiMediator mediator;
    private final SidebarPanel sidebarPanel;
    private final TickerPanel tickerPanel;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;

    // Screen Panels
    private MallMapPanel mallMapPanel;
    private DashboardPanel dashboardPanel;
    private ProductsPanel productsPanel;
    private ReorderPanel reorderPanel;
    private ReportsPanel reportsPanel;
    private SimulationPanel simulationPanel;

    // Header Components
    private JLabel clockLabel;
    private JButton themeToggleBtn;

    public MainFrame(InventoryManager inventoryManager) {
        super("Mall of Tomorrow - Smart Inventory Demand Predictor");
        this.mediator = new GuiMediator(inventoryManager);
        this.mediator.setNavigator(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1240, 820);
        setLocationRelativeTo(null);

        // Main layout container
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Theme.bg());
        setContentPane(rootPanel);

        // 1. Top Header
        rootPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Left Sidebar
        sidebarPanel = new SidebarPanel(mediator);
        rootPanel.add(sidebarPanel, BorderLayout.WEST);

        // 3. Center Screen Area (CardLayout)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        // Initialize Screens
        mallMapPanel = new MallMapPanel(mediator);
        dashboardPanel = new DashboardPanel(mediator);
        productsPanel = new ProductsPanel(mediator);
        reorderPanel = new ReorderPanel(mediator);
        reportsPanel = new ReportsPanel(mediator);
        simulationPanel = new SimulationPanel(mediator);

        contentPanel.add(mallMapPanel, "MALL_MAP");
        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(productsPanel, "PRODUCTS");
        contentPanel.add(reorderPanel, "REORDERS");
        contentPanel.add(reportsPanel, "REPORTS");
        contentPanel.add(simulationPanel, "SIMULATION");

        rootPanel.add(contentPanel, BorderLayout.CENTER);

        // 4. Bottom Ticker Bar
        tickerPanel = new TickerPanel(mediator);
        rootPanel.add(tickerPanel, BorderLayout.SOUTH);

        // Window listener to terminate simulation worker thread cleanly
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediator.stopSimulation();
            }
        });

        // Theme change listener
        Theme.addThemeChangeListener(new Runnable() {
            @Override
            public void run() {
                getContentPane().setBackground(Theme.bg());
                updateThemeButtonText();
                repaint();
            }
        });

        // Start on Mall Map
        navigateTo("MALL_MAP", null);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.headerBg());
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Bottom gold accent line
                g2.setColor(Theme.cardBorder());
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setPreferredSize(new Dimension(1100, 56));
        header.setBorder(new EmptyBorder(8, 20, 8, 20));

        // Left Branding
        JPanel leftBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        leftBrand.setOpaque(false);
        JLabel title = new JLabel("MALL OF TOMORROW");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.GOLD);

        JLabel tag = new JLabel("|   INTELLIGENT SUPPLY-CHAIN MAIN CONTROL");
        tag.setFont(Theme.FONT_BODY);
        tag.setForeground(Theme.textMuted());

        leftBrand.add(title);
        leftBrand.add(tag);
        header.add(leftBrand, BorderLayout.WEST);

        // Right Controls: Digital Clock & Theme Toggle
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 4));
        rightControls.setOpaque(false);

        clockLabel = new JLabel();
        clockLabel.setFont(Theme.FONT_MONO);
        clockLabel.setForeground(Theme.TEAL);
        updateClock();

        // Timer for clock updates every 1000ms (EDT)
        Timer clockTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateClock();
            }
        });
        clockTimer.start();

        themeToggleBtn = Theme.createButton("Light Mode", new Color(60, 80, 110));
        updateThemeButtonText();
        themeToggleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Theme.toggleTheme();
            }
        });

        rightControls.add(clockLabel);
        rightControls.add(themeToggleBtn);
        header.add(rightControls, BorderLayout.EAST);

        return header;
    }

    private void updateClock() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        clockLabel.setText("[MALL TIME: " + sdf.format(new Date()) + "]");
    }

    private void updateThemeButtonText() {
        themeToggleBtn.setText(Theme.isDarkMode() ? "Light Mode" : "Dark Mode");
    }

    @Override
    public void navigateTo(String screenName, Object parameter) {
        cardLayout.show(contentPanel, screenName);
        sidebarPanel.setActiveScreen(screenName);

        // Handle parameter injection (e.g. filtering products by zone code)
        if ("PRODUCTS".equals(screenName) && parameter instanceof String) {
            productsPanel.filterByZone((String) parameter);
        }

        mediator.notifyInventoryUpdated();
    }
}
