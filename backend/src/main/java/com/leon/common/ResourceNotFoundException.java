package com.leon.common;

/** Thrown when a slug or id does not resolve. Mapped to 404 by the handler. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String type, String identifier) {
        super(type + " '" + identifier + "' was not found");
    }
}
