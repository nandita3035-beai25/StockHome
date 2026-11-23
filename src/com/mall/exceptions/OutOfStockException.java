package com.mall.exceptions;

/**
 * Custom Checked Exception thrown when a sale cannot be fulfilled
 * because requested quantity exceeds current available inventory stock.
 * 
 * College Concept: Custom Checked Exception (extends java.lang.Exception),
 * constructor chaining using super(message).
 */
public class OutOfStockException extends Exception {
    private final String productId;
    private final int requestedQty;
    private final int availableStock;

    public OutOfStockException(String productId, int requestedQty, int availableStock) {
        super("Stockout alert! Product [" + productId + "] requested: " 
                + requestedQty + ", but only " + availableStock + " in stock.");
        this.productId = productId;
        this.requestedQty = requestedQty;
        this.availableStock = availableStock;
    }

    public String getProductId() {
        return productId;
    }

    public int getRequestedQty() {
        return requestedQty;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
