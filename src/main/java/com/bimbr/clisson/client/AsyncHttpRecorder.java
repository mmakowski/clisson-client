package com.bimbr.clisson.client;

import static com.bimbr.clisson.util.Arguments.nonEmpty;
import static com.bimbr.clisson.util.Arguments.nonNull;

import java.util.Set;
import java.util.TreeSet;

import com.bimbr.clisson.protocol.Event;
import com.bimbr.clisson.protocol.Json;
import com.bimbr.util.Clock;

/**
 * A {@link Recorder} that asynchronously sends the events to the server over HTTP. Allows the calling code to continue
 * execution immediately without the need to wait for Clisson server to respond.
 * 
 * @author mmakowski
 * @since 1.0.0
 */
final class AsyncHttpRecorder implements Recorder {
    private final String      sourceId;
    private final HttpInvoker invoker;
    private final Clock       clock;

    /**
     * @param sourceId the id of the component that is the source of events
     * @param invoker the {@link HttpInvoker} used to communicate with the server
     * @param clock the {@link Clock} used to generate event timestamp
     */
    AsyncHttpRecorder(final String      sourceId,
                      final HttpInvoker invoker,
                      final Clock       clock) {
        this.sourceId = nonEmpty(sourceId, "sourceId");
        this.invoker = nonNull(invoker, "invoker");
        this.clock = nonNull(clock, "clock");
    }
    
    /**
     * @see Recorder#checkpoint(String, String)
     */
    public void checkpoint(final String messageId, final String description) {
        final Set<String> messageIds = new TreeSet<String>();
        messageIds.add(messageId);
        event(messageIds, messageIds, description);
    }

    /**
     * @see Recorder#event(Set, Set, String)
     */
    public void event(final Set<String> inputMessageIds, final Set<String> outputMessageIds, final String description) {
        final Event event = new Event(sourceId, clock.getTime(), inputMessageIds, outputMessageIds, description);
        invoker.post("/event", Json.jsonFor(event));
    }
}
