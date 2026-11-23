package com.mall.ui.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Unified UI Theme and Styling Engine for "Mall of Tomorrow" Swing Frontend.
 * 
 * Supports:
 * - High-contrast modern Deep Navy mall palette with Gold accents and Neon Teal highlights
 * - Light Mode / Dark Mode dynamic toggle with listener callbacks
 * - Standardized typography hierarchy and reusable rounded-card factory methods
 */
public final class Theme {

    private static boolean darkMode = true;
    private static final List<Runnable> themeChangeListeners = new ArrayList<Runnable>();

    // Fonts
    public static final Font FONT_LARGE_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    // Accent Colors (Constant across themes)
    public static final Color GOLD = new Color(229, 184, 66);
    public static final Color TEAL = new Color(0, 229, 255);
    public static final Color GREEN_SAFE = new Color(52, 199, 89);
    public static final Color YELLOW_WARN = new Color(255, 179, 0);
    public static final Color RED_ALERT = new Color(255, 59, 48);

    private Theme() {
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean isDark) {
        if (darkMode != isDark) {
            darkMode = isDark;
            for (Runnable listener : themeChangeListeners) {
                try {
                    listener.run();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static void toggleTheme() {
        setDarkMode(!darkMode);
    }

    public static void addThemeChangeListener(Runnable listener) {
        if (listener != null) {
            themeChangeListeners.add(listener);
        }
    }

    // Dynamic Color Getters
    public static Color bg() {
        return darkMode ? new Color(11, 17, 30) : new Color(244, 246, 249);
    }

    public static Color sidebarBg() {
        return darkMode ? new Color(8, 12, 22) : new Color(230, 235, 242);
    }

    public static Color headerBg() {
        return darkMode ? new Color(15, 23, 42) : new Color(255, 255, 255);
    }

    public static Color cardBg() {
        return darkMode ? new Color(22, 32, 50) : new Color(255, 255, 255);
    }

    public static Color cardBgHover() {
        return darkMode ? new Color(29, 42, 65) : new Color(240, 243, 248);
    }

    public static Color cardBorder() {
        return darkMode ? new Color(42, 59, 83) : new Color(218, 226, 236);
    }

    public static Color text() {
        return darkMode ? new Color(240, 244, 248) : new Color(26, 32, 44);
    }

    public static Color textMuted() {
        return darkMode ? new Color(138, 153, 173) : new Color(90, 107, 130);
    }

    public static Color inputBg() {
        return darkMode ? new Color(15, 22, 36) : new Color(250, 252, 255);
    }

    /**
     * Creates a rounded card JPanel with subtle border and anti-aliased edges.
     */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 14;
                g2.setColor(cardBg());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.setColor(cardBorder());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        return card;
    }

    /**
     * Creates a styled modern push button.
     */
    public static JButton createButton(String text, final Color baseColor) {
        final JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, javax.swing.JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = baseColor;
                if (btn.getModel().isPressed()) {
                    fill = baseColor.darker();
                } else if (btn.getModel().isRollover()) {
                    fill = baseColor.brighter();
                }
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                g2.dispose();
                super.paint(g, c);
            }
        });

        return btn;
    }
}
