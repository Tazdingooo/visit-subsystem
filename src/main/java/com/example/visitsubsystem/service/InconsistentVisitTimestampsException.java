package com.example.visitsubsystem.service;

/**
 * Signals persisted visit timestamps that violate the expected ordering
 * (e.g. a check-out time earlier than the check-in time), instead of being
 * silently coerced into a plausible-looking duration.
 */
public class InconsistentVisitTimestampsException extends RuntimeException {

    public InconsistentVisitTimestampsException(String message) {
        super(message);
    }
}
