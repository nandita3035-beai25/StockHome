package com.mall.ui.gui;

/**
 * Observer interface for GUI components that need to refresh their state
 * whenever an inventory transaction (sale, restock, reorder, undo, add, remove,
 * multiplier change, or simulation tick) occurs.
 */
public interface InventoryListener {
    /**
     * Fired when any underlying inventory or prediction data changes.
     * Guaranteed to be executed on the Swing Event Dispatch Thread (EDT).
     */
    void onInventoryUpdated();
}
