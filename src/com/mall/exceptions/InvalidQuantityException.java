package com.mall.exceptions;

/**
 * Custom Checked Exception thrown when quantity values are non-positive
 * or violate business boundary conditions.
 * 
 * College Concept: Custom Checked Exception, parameter validation.
 */
public class InvalidQuantityException extends Exception {
    private final int invalidQuantity;

    public InvalidQuantityException(int invalidQuantity) {
        super("Invalid quantity supplied: " + invalidQuantity + ". Quantity must be strictly positive (greater than 0).");
        this.invalidQuantity = invalidQuantity;
    }

    public InvalidQuantityException(String customMessage, int invalidQuantity) {
        super(customMessage + " (Value received: " + invalidQuantity + ")");
        this.invalidQuantity = invalidQuantity;
    }

    public int getInvalidQuantity() {
        return invalidQuantity;
    }
}
