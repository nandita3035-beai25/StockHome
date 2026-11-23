package com.mall.ui.gui;

import com.mall.model.ShopZone;
import com.mall.model.UrgencyLevel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Screen 1: Top-Down 2D Mall Floorplan Visualizer.
 * 
 * Features:
 * - Custom Graphics2D rendering of 4 store zones, central atrium, fountains, and pathways
 * - Dynamic store glow reflecting health (Green/Amber/Red) with pulsing red alerts
 * - Walking animated shopper figures along pathways
 * - Delivery truck restock courier animation sliding to shops
 * - Interactive hover tooltips and click-to-filter navigation
 */
public class MallMapPanel extends JPanel implements InventoryListener, GuiMediator.DeliveryTruckListener {

    private static class Shopper {
        float x, y;
        float targetX, targetY;
        float speed;
        Color color;
        String destinationZone;
        boolean inStore;
        int dwellTicks;

        Shopper(float x, float y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.speed = 1.2f + (float) Math.random() * 1.5f;
            this.inStore = false;
            this.dwellTicks = 0;
        }
    }

    private static class DeliveryTruck {
        float x, y;
        float startX, startY;
        float targetX, targetY;
        boolean active;
        String zoneCode;
        float progress; // 0.0 to 1.0

        DeliveryTruck() {
            this.active = false;
        }
    }

    private final GuiMediator mediator;
    private final Timer animationTimer;
    private float pulseAlpha = 0.5f;
    private boolean pulseRising = true;

    // Shoppers simulation
    private final List<Shopper> shoppers = new ArrayList<Shopper>();
    private final DeliveryTruck deliveryTruck = new DeliveryTruck();
    private final Random random = new Random();

    // Storefront bounding boxes (calculated in paint)
    private final Rectangle rectFood = new Rectangle();
    private final Rectangle rectElec = new Rectangle();
    private final Rectangle rectFash = new Rectangle();
    private final Rectangle rectGroc = new Rectangle();

    private String hoveredZone = null;
    private Point mousePoint = new Point();

    public MallMapPanel(final GuiMediator mediator) {
        this.mediator = mediator;
        mediator.addInventoryListener(this);
        mediator.setDeliveryTruckListener(this);
        setOpaque(false);

        // Initialize 8 shoppers wandering the mall
        Color[] shopperColors = {
            new Color(255, 107, 107), new Color(78, 205, 196),
            new Color(255, 230, 109), new Color(170, 111, 248),
            new Color(255, 159, 243), new Color(84, 160, 255)
        };
        for (int i = 0; i < 8; i++) {
            shoppers.add(new Shopper(400 + (i * 25), 350 + (i * 15), shopperColors[i % shopperColors.length]));
        }

        // Animation timer: 30 FPS (~33ms)
        animationTimer = new Timer(33, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAnimations();
                repaint();
            }
        });
        animationTimer.start();

        // Mouse listeners for hover and click interactions
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoint = e.getPoint();
                String prev = hoveredZone;
                hoveredZone = detectZoneAt(mousePoint);
                if (hoveredZone != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
                if ((prev == null && hoveredZone != null) || (prev != null && !prev.equals(hoveredZone))) {
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String clickedZone = detectZoneAt(e.getPoint());
                if (clickedZone != null) {
                    mediator.navigateTo("PRODUCTS", clickedZone);
                }
            }
        });
    }

    private String detectZoneAt(Point p) {
        if (rectFood.contains(p)) return "FOOD";
        if (rectElec.contains(p)) return "ELEC";
        if (rectFash.contains(p)) return "FASH";
        if (rectGroc.contains(p)) return "GROC";
        return null;
    }

    @Override
    public void onInventoryUpdated() {
        repaint();
    }

    @Override
    public void onTruckDispatched(String targetZoneCode) {
        deliveryTruck.active = true;
        deliveryTruck.progress = 0.0f;
        deliveryTruck.zoneCode = targetZoneCode;
        deliveryTruck.startX = getWidth() / 2f;
        deliveryTruck.startY = getHeight() - 40f;

        // Target center of shop
        Rectangle targetRect = getZoneRect(targetZoneCode);
        deliveryTruck.targetX = targetRect.x + targetRect.width / 2f;
        deliveryTruck.targetY = targetRect.y + targetRect.height / 2f;
        deliveryTruck.x = deliveryTruck.startX;
        deliveryTruck.y = deliveryTruck.startY;
        repaint();
    }

    private Rectangle getZoneRect(String code) {
        if ("FOOD".equals(code)) return rectFood;
        if ("ELEC".equals(code)) return rectElec;
        if ("FASH".equals(code)) return rectFash;
        if ("GROC".equals(code)) return rectGroc;
        return rectFood;
    }

    private void updateAnimations() {
        // 1. Red Alert Pulsing Glow
        if (pulseRising) {
            pulseAlpha += 0.04f;
            if (pulseAlpha >= 0.85f) {
                pulseAlpha = 0.85f;
                pulseRising = false;
            }
        } else {
            pulseAlpha -= 0.04f;
            if (pulseAlpha <= 0.25f) {
                pulseAlpha = 0.25f;
                pulseRising = true;
            }
        }

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // 2. Shopper Movement Logic
        for (Shopper s : shoppers) {
            if (s.inStore) {
                s.dwellTicks++;
                if (s.dwellTicks > 50 + random.nextInt(40)) {
                    s.inStore = false;
                    s.dwellTicks = 0;
                    // Return to central atrium
                    s.targetX = (w / 2f) + (random.nextInt(120) - 60);
                    s.targetY = (h / 2f) + (random.nextInt(120) - 60);
                }
            } else {
                float dx = s.targetX - s.x;
                float dy = s.targetY - s.y;
                float dist = (float) Math.hypot(dx, dy);

                if (dist < 8f || s.targetX == 0) {
                    // Pick next destination (store or walkway)
                    if (random.nextInt(100) < 45) {
                        // Visit a store
                        String[] zones = {"FOOD", "ELEC", "FASH", "GROC"};
                        String z = zones[random.nextInt(zones.length)];
                        Rectangle r = getZoneRect(z);
                        if (r.width > 0) {
                            s.targetX = r.x + 30 + random.nextInt(Math.max(1, r.width - 60));
                            s.targetY = r.y + 30 + random.nextInt(Math.max(1, r.height - 60));
                            s.destinationZone = z;
                            s.inStore = true;
                        }
                    } else {
                        // Walk atrium
                        s.targetX = (w / 2f) + (random.nextInt(180) - 90);
                        s.targetY = (h / 2f) + (random.nextInt(200) - 100);
                    }
                } else {
                    s.x += (dx / dist) * s.speed;
                    s.y += (dy / dist) * s.speed;
                }
            }
        }

        // 3. Delivery Truck Animation
        if (deliveryTruck.active) {
            deliveryTruck.progress += 0.025f;
            deliveryTruck.x = deliveryTruck.startX + (deliveryTruck.targetX - deliveryTruck.startX) * deliveryTruck.progress;
            deliveryTruck.y = deliveryTruck.startY + (deliveryTruck.targetY - deliveryTruck.startY) * deliveryTruck.progress;

            if (deliveryTruck.progress >= 1.0f) {
                deliveryTruck.active = false;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Floor Tile Grid
        g2.setColor(Theme.isDarkMode() ? new Color(13, 20, 34) : new Color(236, 240, 246));
        g2.fillRect(0, 0, w, h);

        // Draw marble tile grid
        g2.setColor(Theme.isDarkMode() ? new Color(22, 32, 50, 100) : new Color(218, 225, 235, 120));
        int tileSize = 40;
        for (int x = 0; x < w; x += tileSize) {
            g2.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += tileSize) {
            g2.drawLine(0, y, w, y);
        }

        // 2. Calculate Store Dimensions (Responsive 2x2 Layout with central plaza)
        int marginX = 40;
        int marginY = 30;
        int storeW = (w - marginX * 2 - 140) / 2;
        int storeH = (h - marginY * 2 - 120) / 2;

        rectFood.setBounds(marginX, marginY, storeW, storeH);
        rectElec.setBounds(w - marginX - storeW, marginY, storeW, storeH);
        rectFash.setBounds(marginX, h - marginY - storeH - 30, storeW, storeH);
        rectGroc.setBounds(w - marginX - storeW, h - marginY - storeH - 30, storeW, storeH);

        // 3. Central Promenade & Fountains
        int atriumX = marginX + storeW + 20;
        int atriumY = marginY + 20;
        int atriumW = w - 2 * (marginX + storeW + 20);
        int atriumH = h - 2 * marginY - 40;

        // Walkway background
        g2.setColor(Theme.isDarkMode() ? new Color(18, 28, 46) : new Color(248, 250, 253));
        g2.fillRoundRect(atriumX, atriumY, atriumW, atriumH, 30, 30);
        g2.setColor(Theme.cardBorder());
        g2.drawRoundRect(atriumX, atriumY, atriumW, atriumH, 30, 30);

        // Central Illuminated Fountain
        int fountainX = w / 2;
        int fountainY = h / 2 - 15;
        g2.setColor(new Color(0, 229, 255, 40));
        g2.fillOval(fountainX - 55, fountainY - 55, 110, 110);
        g2.setColor(new Color(0, 229, 255, 100));
        g2.fillOval(fountainX - 35, fountainY - 35, 70, 70);
        g2.setColor(Theme.TEAL);
        g2.drawOval(fountainX - 35, fountainY - 35, 70, 70);
        g2.setFont(Theme.FONT_SMALL);
        g2.drawString("ATRIUM FOUNTAIN", fountainX - 48, fountainY + 4);

        // 4. Render 4 Storefronts
        Map<String, ShopZone> zones = mediator.getInventoryManager().getZones();
        renderStore(g2, rectFood, "ZONE A", "GOURMET FOOD COURT", "FOOD", zones.get("FOOD"), new Color(255, 140, 0));
        renderStore(g2, rectElec, "ZONE B", "TECHNOVA ELECTRONICS", "ELEC", zones.get("ELEC"), new Color(0, 180, 255));
        renderStore(g2, rectFash, "ZONE C", "VOGUE & VELVET FASHION", "FASH", zones.get("FASH"), new Color(220, 80, 220));
        renderStore(g2, rectGroc, "ZONE D", "APEX SUPER GROCERY", "GROC", zones.get("GROC"), new Color(40, 190, 80));

        // 5. Render Animated Shoppers
        for (Shopper s : shoppers) {
            g2.setColor(s.color);
            g2.fillOval((int) s.x - 5, (int) s.y - 5, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawOval((int) s.x - 5, (int) s.y - 5, 10, 10);
        }

        // 6. Render Delivery Truck Courier (if active)
        if (deliveryTruck.active) {
            int tx = (int) deliveryTruck.x;
            int ty = (int) deliveryTruck.y;

            // Truck body
            g2.setColor(Theme.GOLD);
            g2.fillRoundRect(tx - 16, ty - 10, 32, 20, 6, 6);
            g2.setColor(Color.BLACK);
            g2.fillRect(tx + 8, ty - 8, 6, 16); // Windshield
            // Wheels
            g2.fillOval(tx - 12, ty + 9, 8, 8);
            g2.fillOval(tx + 6, ty + 9, 8, 8);

            // Banner
            g2.setColor(Theme.GREEN_SAFE);
            g2.setFont(Theme.FONT_BODY_BOLD);
            g2.drawString("RESTOCK COURIER -> " + deliveryTruck.zoneCode, tx - 45, ty - 16);
        }

        // 7. Render Hover Tooltip
        if (hoveredZone != null && mousePoint != null) {
            renderTooltip(g2, hoveredZone, mousePoint);
        }

        // 8. Bottom Map Legend
        g2.setColor(Theme.cardBg());
        g2.fillRoundRect(marginX, h - 35, w - 2 * marginX, 26, 10, 10);
        g2.setColor(Theme.cardBorder());
        g2.drawRoundRect(marginX, h - 35, w - 2 * marginX, 26, 10, 10);

        g2.setFont(Theme.FONT_SMALL);
        g2.setColor(Theme.text());
        g2.drawString("MALL HEALTH MAP: ", marginX + 16, h - 18);

        g2.setColor(Theme.GREEN_SAFE);
        g2.drawString("O  SAFE (>5 Days)", marginX + 150, h - 18);
        g2.setColor(Theme.YELLOW_WARN);
        g2.drawString("O  WARNING (<=5 Days)", marginX + 280, h - 18);
        g2.setColor(Theme.RED_ALERT);
        g2.drawString("O  CRITICAL (<=2 Days / Pulsing)", marginX + 440, h - 18);

        g2.setColor(Theme.TEAL);
        g2.drawString("(TIP: Click any store to view its inventory)", w - marginX - 250, h - 18);

        g2.dispose();
    }

    private void renderStore(Graphics2D g2, Rectangle r, String tag, String title, 
                            String code, ShopZone zone, Color accentColor) {
        boolean isHovered = code.equals(hoveredZone);
        UrgencyLevel health = zone != null ? zone.getZoneStockHealth() : UrgencyLevel.SAFE;

        // Health Glow & Pulse
        Color glowColor;
        if (health == UrgencyLevel.CRITICAL) {
            glowColor = new Color(255, 59, 48, (int) (pulseAlpha * 255));
        } else if (health == UrgencyLevel.WARNING) {
            glowColor = new Color(255, 179, 0, 160);
        } else {
            glowColor = new Color(52, 199, 89, 140);
        }

        // Draw Outer Glow Halo
        g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 30));
        g2.fillRoundRect(r.x - 8, r.y - 8, r.width + 16, r.height + 16, 24, 24);

        // Store interior background
        g2.setColor(isHovered ? Theme.cardBgHover() : Theme.cardBg());
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 16, 16);

        // Store border with health indicator
        g2.setColor(glowColor);
        g2.setStroke(new BasicStroke(isHovered ? 3.5f : 2.5f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 16, 16);

        // Top Store Awning Strip
        g2.setColor(accentColor);
        g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, 30, 14, 14);
        g2.fillRect(r.x + 2, r.y + 16, r.width - 4, 16);

        // Awning scalloped stripes
        g2.setColor(new Color(0, 0, 0, 40));
        for (int i = 0; i < r.width - 20; i += 24) {
            g2.fillRect(r.x + 10 + i, r.y + 2, 12, 30);
        }

        // Store Title
        g2.setColor(Color.WHITE);
        g2.setFont(Theme.FONT_HEADER);
        g2.drawString(tag + ": " + title, r.x + 14, r.y + 22);

        // Store Body Info
        int totalItems = zone != null ? zone.getProductCount() : 0;
        int totalStock = zone != null ? zone.getTotalStockCount() : 0;

        g2.setColor(Theme.text());
        g2.setFont(Theme.FONT_BODY);
        g2.drawString("Inventory Items: " + totalItems + " SKUs", r.x + 16, r.y + 65);
        g2.drawString("Total Units In-Stock: " + totalStock, r.x + 16, r.y + 88);

        // Status Badge Pill
        int badgeX = r.x + 16;
        int badgeY = r.y + 108;
        g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 35));
        g2.fillRoundRect(badgeX, badgeY, 140, 24, 10, 10);
        g2.setColor(glowColor);
        g2.drawRoundRect(badgeX, badgeY, 140, 24, 10, 10);

        g2.setFont(Theme.FONT_BODY_BOLD);
        g2.drawString("STATUS: " + health.getLabel(), badgeX + 12, badgeY + 17);
    }

    private void renderTooltip(Graphics2D g2, String zoneCode, Point pt) {
        ShopZone zone = mediator.getInventoryManager().getZone(zoneCode);
        if (zone == null) return;

        String title = zone.getName() + " (" + zone.getCategory() + ")";
        String line1 = "Total Products: " + zone.getProductCount() + " SKUs";
        String line2 = "Units on Hand: " + zone.getTotalStockCount() + " units";
        String line3 = "Health State: " + zone.getZoneStockHealth().getLabel();
        String line4 = "-> Click to inspect items";

        int boxW = 210;
        int boxH = 95;
        int bx = Math.min(pt.x + 15, getWidth() - boxW - 10);
        int by = Math.min(pt.y + 15, getHeight() - boxH - 10);

        g2.setColor(new Color(15, 23, 42, 235));
        g2.fillRoundRect(bx, by, boxW, boxH, 12, 12);
        g2.setColor(Theme.GOLD);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, boxW, boxH, 12, 12);

        g2.setColor(Theme.GOLD);
        g2.setFont(Theme.FONT_BODY_BOLD);
        g2.drawString(title, bx + 10, by + 18);

        g2.setColor(Color.WHITE);
        g2.setFont(Theme.FONT_SMALL);
        g2.drawString(line1, bx + 10, by + 36);
        g2.drawString(line2, bx + 10, by + 52);

        Color hCol = zone.getZoneStockHealth() == UrgencyLevel.CRITICAL ? Theme.RED_ALERT :
                     zone.getZoneStockHealth() == UrgencyLevel.WARNING ? Theme.YELLOW_WARN : Theme.GREEN_SAFE;
        g2.setColor(hCol);
        g2.drawString(line3, bx + 10, by + 68);

        g2.setColor(Theme.TEAL);
        g2.drawString(line4, bx + 10, by + 84);
    }
}
