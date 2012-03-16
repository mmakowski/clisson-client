package com.bimbr.clisson.client;

import java.util.Set;

import com.bimbr.clisson.protocol.Event;

/**
 * Recorder provides methods for recording message events. These methods should never throw exceptions.
 * 
 * @author mmakowski
 * @since 1.0.0
 */
public interface Recorder {
    /**
     * Records a {@link com.bimbr.clisson.protocol.Event} that has a single input and the same single output message.
     * @param messageId the id of the message that arrived at a checkpoint
     * @param description description of the checkpoint
     * @since 1.0.0
     */
    void checkpoint(String messageId, String description);
    
    /**
     * Records an arbitrary {@link com.bimbr.clisson.protocol.Event} that has zero or more input messages and zero or more output messages.
     * The total number of input and output messages must not be zero.
     * @param inputMessageIds the ids of input messages
     * @param outputMessageIds the ids of output messages
     * @param description description of the event
     * @since 1.0.0
     */
    void event(Set<String> inputMessageIds, Set<String> outputMessageIds, String description);

    /**
     * Records an arbitrary, preconstructed {@link com.bimbr.clisson.protocol.Event}
     * @param event the event to record
     * @since 1.0.0
     */
    void event(Event event);
}
