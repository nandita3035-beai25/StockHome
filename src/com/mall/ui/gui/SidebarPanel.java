package com.mall.ui.gui;

import com.mall.model.ShopZone;
import com.mall.model.UrgencyLevel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Modern Left Sidebar Navigation Panel for Mall of Tomorrow.
 */
public class SidebarPanel extends JPanel implements InventoryListener {

    private final GuiMediator mediator;
    private final Map<String, JButton> navButtons;
    private String activeScreen = "MALL_MAP";
    private JLabel healthStatusLabel;

    public SidebarPanel(final GuiMediator mediator) {
        this.mediator = mediator;
        this.navButtons = new HashMap<String, JButton>();
        mediator.addInventoryListener(this);

        setPreferredSize(new Dimension(220, 700));
        setLayout(new BorderLayout());
        setOpaque(false);

        // Header
        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(22, 16, 22, 16));

        JLabel logoLabel = new JLabel("MALL OF TOMORROW");
        logoLabel.setFont(Theme.FONT_TITLE);
        logoLabel.setForeground(Theme.GOLD);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        brandPanel.add(logoLabel, BorderLayout.NORTH);

        JLabel subLabel = new JLabel("AI Demand Predictor");
        subLabel.setFont(Theme.FONT_SMALL);
        subLabel.setForeground(Theme.TEAL);
        subLabel.setHorizontalAlignment(SwingConstants.CENTER);
        brandPanel.add(subLabel, BorderLayout.SOUTH);

        add(brandPanel, BorderLayout.NORTH);

        // Navigation Buttons Center List
        JPanel navList = new JPanel(new GridLayout(6, 1, 0, 8));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(10, 14, 10, 14));

        navList.add(createNavButton("MALL_MAP", "[MALL MAP]", "Top-Down Floorplan"));
        navList.add(createNavButton("DASHBOARD", "[DASHBOARD]", "Analytics & Charts"));
        navList.add(createNavButton("PRODUCTS", "[PRODUCTS]", "Catalog & Sales"));
        navList.add(createNavButton("REORDERS", "[REORDERS]", "PriorityQueue Urgency"));
        navList.add(createNavButton("REPORTS", "[REPORTS]", "Velocity & 2D Matrix"));
        navList.add(createNavButton("SIMULATION", "[LIVE SIM]", "Real-Time Traffic"));

        add(navList, BorderLayout.CENTER);

        // Footer Health Badge
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(14, 14, 20, 14));

        healthStatusLabel = new JLabel("Mall Health: SAFE", SwingConstants.CENTER);
        healthStatusLabel.setFont(Theme.FONT_BODY_BOLD);
        healthStatusLabel.setForeground(Theme.GREEN_SAFE);
        healthStatusLabel.setBorder(new EmptyBorder(8, 10, 8, 10));

        footer.add(healthStatusLabel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        Theme.addThemeChangeListener(new Runnable() {
            @Override
            public void run() {
                repaint();
            }
        });

        updateHealthBadge();
    }

    private JButton createNavButton(final String screenId, final String title, final String subtitle) {
        final JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isActive = screenId.equals(activeScreen);
                if (isActive) {
                    g2.setColor(Theme.isDarkMode() ? new Color(20, 35, 60) : new Color(210, 225, 245));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                    // Active gold indicator bar on left
                    g2.setColor(Theme.GOLD);
                    g2.fillRoundRect(2, 6, 5, getHeight() - 12, 4, 4);
                } else if (getModel().isRollover()) {
                    g2.setColor(Theme.isDarkMode() ? new Color(18, 26, 44) : new Color(235, 240, 248));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setLayout(new BorderLayout());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(Theme.FONT_BODY_BOLD);
        lblTitle.setForeground(Theme.text());

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(Theme.FONT_SMALL);
        lblSub.setForeground(Theme.textMuted());

        btn.add(lblTitle, BorderLayout.NORTH);
        btn.add(lblSub, BorderLayout.SOUTH);

        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setActiveScreen(screenId);
                mediator.navigateTo(screenId, null);
            }
        });

        navButtons.put(screenId, btn);
        return btn;
    }

    public void setActiveScreen(String screenId) {
        this.activeScreen = screenId;
        repaint();
    }

    @Override
    public void onInventoryUpdated() {
        updateHealthBadge();
    }

    private void updateHealthBadge() {
        UrgencyLevel worst = UrgencyLevel.SAFE;
        for (ShopZone zone : mediator.getInventoryManager().getZones().values()) {
            UrgencyLevel zHealth = zone.getZoneStockHealth();
            if (zHealth == UrgencyLevel.CRITICAL) {
                worst = UrgencyLevel.CRITICAL;
                break;
            } else if (zHealth == UrgencyLevel.WARNING) {
                worst = UrgencyLevel.WARNING;
            }
        }

        if (worst == UrgencyLevel.CRITICAL) {
            healthStatusLabel.setText("Health: CRITICAL [!]");
            healthStatusLabel.setForeground(Theme.RED_ALERT);
        } else if (worst == UrgencyLevel.WARNING) {
            healthStatusLabel.setText("Health: WARNING [*]");
            healthStatusLabel.setForeground(Theme.YELLOW_WARN);
        } else {
            healthStatusLabel.setText("Health: OPTIMAL [OK]");
            healthStatusLabel.setForeground(Theme.GREEN_SAFE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Theme.sidebarBg());
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Right subtle separator border
        g2.setColor(Theme.cardBorder());
        g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
