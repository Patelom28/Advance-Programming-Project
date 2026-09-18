package com.example.foodrescue.exception;

// Thrown when a request would break one of the domain rules - mapped to HTTP 409
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
