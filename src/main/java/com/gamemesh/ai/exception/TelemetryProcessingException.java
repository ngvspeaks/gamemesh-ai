package com.gamemesh.ai.exception;

/**
 * Exception thrown when asynchronous telemetry processing fails.
 * Triggers Spring AMQP retry policies and dead-letter routing.
 */
public class TelemetryProcessingException extends RuntimeException {

    public TelemetryProcessingException(String message) {
        super(message);
    }

    public TelemetryProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

