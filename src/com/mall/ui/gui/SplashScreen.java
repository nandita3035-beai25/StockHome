package com.mall.ui.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JWindow;
import javax.swing.Timer;

/**
 * Animated Grand Opening Splash Screen for "Mall of Tomorrow".
 * Features rising security shutter animation and neon sign illumination.
 */
public class SplashScreen extends JWindow {

    private int progress = 0; // 0 to 100%
    private Timer animationTimer;
    private final Runnable onComplete;

    public SplashScreen(Runnable onComplete) {
        this.onComplete = onComplete;
        setSize(540, 360);

        // Center on screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);

        // Animation timer: 50 steps at 35ms each ~= 1.75 seconds
        animationTimer = new Timer(35, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progress += 2;
                repaint();
                if (progress >= 100) {
                    animationTimer.stop();
                    dispose();
                    if (SplashScreen.this.onComplete != null) {
                        SplashScreen.this.onComplete.run();
                    }
                }
            }
        });
    }

    public void showSplash() {
        setVisible(true);
        animationTimer.start();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Dark navy mall background
        g2.setColor(new Color(11, 17, 30));
        g2.fillRect(0, 0, w, h);

        // Gold border
        g2.setColor(Theme.GOLD);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(2, 2, w - 5, h - 5, 20, 20);

        // Neon Glow effect for title based on progress
        float alpha = Math.min(1.0f, progress / 70.0f);
        g2.setColor(new Color(0, 229, 255, (int) (alpha * 255)));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
        String title = "MALL OF TOMORROW";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (w - titleWidth) / 2, 85);

        // Subtitle
        g2.setColor(new Color(229, 184, 66, (int) (alpha * 255)));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        String subtitle = "SMART INVENTORY DEMAND PREDICTOR";
        int subWidth = g2.getFontMetrics().stringWidth(subtitle);
        g2.drawString(subtitle, (w - subWidth) / 2, 115);

        // Rising Storefront Shutter Simulation Area
        int shutterX = 40;
        int shutterY = 145;
        int shutterW = w - 80;
        int shutterH = 140;

        // Background behind shutter (illuminated store interior)
        g2.setColor(new Color(20, 32, 54));
        g2.fillRect(shutterX, shutterY, shutterW, shutterH);

        // Glowing storefront interior lights
        g2.setColor(new Color(52, 199, 89, 180));
        g2.fillOval(shutterX + 40, shutterY + 30, 60, 40);
        g2.setColor(new Color(0, 229, 255, 180));
        g2.fillOval(shutterX + shutterW - 100, shutterY + 30, 60, 40);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String storeMsg = ">>> GRAND OPENING & INVENTORY ONLINE <<<";
        int msgWidth = g2.getFontMetrics().stringWidth(storeMsg);
        g2.drawString(storeMsg, shutterX + (shutterW - msgWidth) / 2, shutterY + 75);

        // Shutter slats that roll upwards
        int rolledHeight = (int) ((100 - progress) / 100.0 * shutterH);
        if (rolledHeight > 0) {
            g2.setColor(new Color(50, 65, 88));
            g2.fillRect(shutterX, shutterY, shutterW, rolledHeight);

            // Draw horizontal slat ridges
            g2.setColor(new Color(30, 42, 60));
            g2.setStroke(new BasicStroke(2));
            for (int y = shutterY; y < shutterY + rolledHeight; y += 12) {
                g2.drawLine(shutterX, y, shutterX + shutterW, y);
            }

            // Shutter bottom bar
            g2.setColor(new Color(229, 184, 66));
            g2.fillRect(shutterX, shutterY + rolledHeight - 6, shutterW, 6);
        }

        // Shutter frame
        g2.setColor(new Color(75, 95, 125));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(shutterX, shutterY, shutterW, shutterH);

        // Bottom Progress Bar
        int barY = h - 45;
        int barW = w - 80;
        g2.setColor(new Color(25, 36, 56));
        g2.fillRoundRect(40, barY, barW, 8, 6, 6);

        g2.setColor(Theme.TEAL);
        int fillW = (int) ((progress / 100.0) * barW);
        g2.fillRoundRect(40, barY, fillW, 8, 6, 6);

        // Status text
        g2.setColor(new Color(160, 180, 205));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        String status = "Raising shutters... Initializing AI Demand Matrix " + progress + "%";
        g2.drawString(status, 42, barY - 8);
    }
}
