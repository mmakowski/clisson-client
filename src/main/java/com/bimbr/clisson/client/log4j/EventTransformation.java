package com.bimbr.clisson.client.log4j;

import org.apache.log4j.spi.LoggingEvent;

import com.bimbr.clisson.protocol.Event;

/**
 * Event transformations build Clisson {@link Event}s from log4j {@link LoggingEvent}s.
 * 
 * @author mmakowski
 * @since 1.0.0
 */
public interface EventTransformation {
    /**
     * Transforms log4j {@link LoggingEvent} to Clisson {@link Event}.
     * @param source the log4j logging event
     * @return a Clisson event
     * @throws IgnoreEventException if the {@code source} event should be ignored and not sent to the server
     * @since 1.0.0
     */
    Event perform(LoggingEvent source) throws IgnoreEventException;

    /**
     * Exception thrown by transformation if the input event should be ignored and not sent to the server.
     * @author mmakowski
     * @since 1.0.0
     */
    public final class IgnoreEventException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
