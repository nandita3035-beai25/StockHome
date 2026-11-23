package com.mall.ui.gui;

import com.mall.service.InventoryManager;
import com.mall.service.SimulationService;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Central state coordinator and Mediator between GUI panels,
 * background simulation worker thread, and InventoryManager backend.
 */
public class GuiMediator {

    public interface Navigator {
        void navigateTo(String screenName, Object parameter);
    }

    public interface DeliveryTruckListener {
        void onTruckDispatched(String targetZoneCode);
    }

    private final InventoryManager inventoryManager;
    private SimulationService simulationService;
    private Thread simulationThread;
    private final List<InventoryListener> listeners;
    private Navigator navigator;
    private DeliveryTruckListener deliveryTruckListener;

    public GuiMediator(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        this.listeners = new ArrayList<InventoryListener>();
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public synchronized void addInventoryListener(InventoryListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeInventoryListener(InventoryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Broadcasts an inventory update event to all registered listeners.
     * Guaranteed to execute safely on the Event Dispatch Thread (EDT).
     */
    public void notifyInventoryUpdated() {
        if (SwingUtilities.isEventDispatchThread()) {
            fireListeners();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireListeners();
                }
            });
        }
    }

    private synchronized void fireListeners() {
        for (InventoryListener listener : listeners) {
            try {
                listener.onInventoryUpdated();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public void navigateTo(String screenName, Object parameter) {
        if (navigator != null) {
            navigator.navigateTo(screenName, parameter);
        }
    }

    public void setDeliveryTruckListener(DeliveryTruckListener listener) {
        this.deliveryTruckListener = listener;
    }

    public void dispatchDeliveryTruck(String targetZoneCode) {
        if (deliveryTruckListener != null) {
            deliveryTruckListener.onTruckDispatched(targetZoneCode);
        }
    }

    // =========================================================================
    // SIMULATION SERVICE LIFECYCLE (Multithreading on EDT-safe boundary)
    // =========================================================================

    public synchronized boolean isSimulationActive() {
        return simulationService != null && simulationService.isRunning();
    }

    public synchronized SimulationService getSimulationService() {
        return simulationService;
    }

    public synchronized void startSimulation() {
        if (isSimulationActive()) {
            return;
        }

        this.simulationService = new SimulationService(inventoryManager);
        this.simulationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                simulationService.run();
            }
        }, "MallGuiSimThread");

        simulationThread.setDaemon(true);
        simulationThread.start();
        notifyInventoryUpdated();
    }

    public synchronized void stopSimulation() {
        if (simulationService != null) {
            simulationService.stopSimulation();
        }
        if (simulationThread != null) {
            simulationThread.interrupt();
            try {
                simulationThread.join(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            simulationThread = null;
        }
        notifyInventoryUpdated();
    }
}
