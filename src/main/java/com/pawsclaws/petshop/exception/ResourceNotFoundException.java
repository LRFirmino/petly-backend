package com.pawsclaws.petshop.exception;

/**
 * Thrown when a resource does not exist *within the current tenant*.
 * Returning 404 (instead of 403) avoids leaking whether the resource exists
 * in another petshop.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
