package com.example.visitsubsystem.service;

public class InvalidVisitTransitionException extends RuntimeException {

    public InvalidVisitTransitionException(String message) {
        super(message);
    }
}
