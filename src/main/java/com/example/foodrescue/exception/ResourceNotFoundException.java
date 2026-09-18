package com.example.foodrescue.exception;

// Thrown when a fridge or food item id does not exist - mapped to HTTP 404
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String what, Long id) {
        return new ResourceNotFoundException(what + " " + id + " was not found.");
    }
}
