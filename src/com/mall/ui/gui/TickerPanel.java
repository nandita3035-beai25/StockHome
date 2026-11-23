package com.mall.ui.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Bottom Scrolling Ticker Bar displaying real-time critical stock alerts
 * and recent inventory audit transactions.
 */
public class TickerPanel extends JPanel implements InventoryListener {

    private final GuiMediator mediator;
    private final Timer tickerTimer;
    private int scrollOffset = 0;
    private String announcementText = "All mall systems operating within nominal parameters.";
    private final JLabel activityLabel;

    public TickerPanel(GuiMediator mediator) {
        this.mediator = mediator;
        mediator.addInventoryListener(this);

        setPreferredSize(new Dimension(1100, 38));
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(4, 12, 4, 12));

        // Alert badge on left
        JLabel badge = new JLabel("LIVE TICKER ");
        badge.setFont(Theme.FONT_BODY_BOLD);
        badge.setForeground(Theme.GOLD);
        badge.setBorder(new EmptyBorder(0, 4, 0, 10));
        add(badge, BorderLayout.WEST);

        // Activity log on right
        activityLabel = new JLabel("System Ready");
        activityLabel.setFont(Theme.FONT_SMALL);
        activityLabel.setForeground(Theme.TEAL);
        activityLabel.setBorder(new EmptyBorder(0, 10, 0, 4));
        add(activityLabel, BorderLayout.EAST);

        // Center scrolling ticker panel
        JPanel centerMarquee = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setFont(Theme.FONT_MONO);
                g2.setColor(Theme.isDarkMode() ? new Color(245, 200, 80) : new Color(180, 120, 20));

                int y = (getHeight() + g2.getFontMetrics().getAscent()) / 2 - 2;
                g2.drawString(announcementText, getWidth() - scrollOffset, y);

                g2.dispose();
            }
        };
        centerMarquee.setOpaque(false);
        add(centerMarquee, BorderLayout.CENTER);

        // Timer for ticker scroll (every 40ms)
        tickerTimer = new Timer(40, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scrollOffset += 2;
                int textWidth = getFontMetrics(Theme.FONT_MONO).stringWidth(announcementText);
                if (scrollOffset > getWidth() + textWidth) {
                    scrollOffset = 0;
                }
                repaint();
            }
        });
        tickerTimer.start();

        updateAlertMessages();
    }

    @Override
    public void onInventoryUpdated() {
        updateAlertMessages();
        repaint();
    }

    private void updateAlertMessages() {
        Set<String> criticalAlerts = mediator.getInventoryManager().getActiveCriticalAlerts();
        if (criticalAlerts.isEmpty()) {
            this.announcementText = "*** MALL ANNOUNCEMENT: ALL ZONES HEALTHY *** No critical stockout warnings active.";
        } else {
            StringBuilder sb = new StringBuilder("*** CRITICAL STOCK ALERTS: ");
            for (String a : criticalAlerts) {
                sb.append(" [!] ").append(a).append("    ");
            }
            sb.append("***");
            this.announcementText = sb.toString();
        }

        List<String> logs = mediator.getInventoryManager().getRecentActivityLog();
        if (logs != null && !logs.isEmpty()) {
            String latest = logs.get(0);
            if (latest.length() > 45) {
                latest = latest.substring(0, 42) + "...";
            }
            activityLabel.setText("Last Event: " + latest);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Theme.headerBg());
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Top separator line
        g2.setColor(Theme.cardBorder());
        g2.drawLine(0, 0, getWidth(), 0);
        g2.dispose();
        super.paintComponent(g);
    }
}
