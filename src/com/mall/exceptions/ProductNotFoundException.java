package com.mall.exceptions;

/**
 * Custom Checked Exception thrown when an inquiry or operation specifies
 * a product ID that does not exist in any mall zone.
 * 
 * College Concept: Custom Exception class, error reporting with identifiers.
 */
public class ProductNotFoundException extends Exception {
    private final String attemptedId;

    public ProductNotFoundException(String attemptedId) {
        super("Product with ID '" + attemptedId + "' was not found in any Mall of Tomorrow zone.");
        this.attemptedId = attemptedId;
    }

    public String getAttemptedId() {
        return attemptedId;
    }
}
