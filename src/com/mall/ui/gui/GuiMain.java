package com.mall.ui.gui;

import com.mall.service.InventoryManager;
import com.mall.util.DataSeeder;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Dedicated Graphical Launcher for "Mall of Tomorrow".
 * Initializes inventory model, seeds realistic retail data,
 * and launches the animated Splash Screen leading into the MainFrame.
 */
public class GuiMain {

    public static void main(String[] args) {
        // Set system look and feel for native dialog fonts and widgets
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Initialize backend (Zero Swing dependencies in backend layers)
        final InventoryManager manager = new InventoryManager();
        DataSeeder.seedMallData(manager);

        // Launch on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Show 1.75s animated splash screen with rolling shutter
                SplashScreen splash = new SplashScreen(new Runnable() {
                    @Override
                    public void run() {
                        MainFrame mainFrame = new MainFrame(manager);
                        mainFrame.setVisible(true);
                    }
                });
                splash.showSplash();
            }
        });
    }
}
