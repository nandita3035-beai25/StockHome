package com.mall.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Model class recording an individual sale transaction event.
 * Instances of this class are stored in a LIFO Deque (Stack)
 * to support the "Undo Last Sale" feature.
 * 
 * Demonstrates:
 * - OOP: Encapsulation, immutable transaction state
 * - Static counter for unique transaction sequence IDs
 */
public class SaleTransaction {
    private static long transactionSequence = 1000L;

    private final long transactionId;
    private final String productId;
    private final String productName;
    private final int quantity;
    private final double unitPrice;
    private final double totalAmount;
    private final long timestamp;

    public SaleTransaction(String productId, String productName, int quantity, double unitPrice) {
        this.transactionId = ++transactionSequence;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = quantity * unitPrice;
        this.timestamp = System.currentTimeMillis();
    }

    public long getTransactionId() {
        return transactionId;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }

    @Override
    public String toString() {
        return String.format("Tx #%d: Sold %d x '%s' (₹%.2f ea) = ₹%.2f at %s",
                transactionId, quantity, productName, unitPrice, totalAmount, getFormattedTimestamp());
    }
}
