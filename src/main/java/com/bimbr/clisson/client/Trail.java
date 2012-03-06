package com.bimbr.clisson.client;

/**
 * Trail provides methods for recording message events. These methods should never throw exceptions.
 * 
 * @author mmakowski
 * @since 1.0.0
 */
public interface Trail {
    /**
     * Adds a {@link com.bimbr.clisson.protocol.CheckpointEvent} to the trail.
     * @param messageId the id of the message that arrived at a checkpoint
     * @param description description of the checkpoint
     */
    void checkpoint(String messageId, String description);
}
