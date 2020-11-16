package com.romarjozeka.app.ws.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 2827572788348591230L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
