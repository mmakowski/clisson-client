package com.bimbr.clisson.client;

import static com.bimbr.clisson.util.Arguments.nonEmpty;
import static com.bimbr.clisson.util.Arguments.nonNull;
import static com.bimbr.clisson.util.Arguments.positive;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bimbr.clisson.protocol.Event;
import com.bimbr.clisson.protocol.Json;
import com.bimbr.util.Clock;

/**
 * A {@link Recorder} that asynchronously sends the events to the server over HTTP. Allows the calling code to continue
 * execution immediately without the need to wait for Clisson server to respond. This implementation uses an internal
 * buffer to store the messages to be submitted. When the buffer fills up subsequent calls to submission methods will
 * succeed, but will log errors reporting that the submission had to be dropped.
 * 
 * @author mmakowski
 * @since 1.0.0
 */
final class AsyncHttpRecorder implements Recorder {
    private static final int DEFAULT_LOGGER_GAG_PERIOD_MS = 5 * 60 * 1000;
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(AsyncHttpRecorder.class);
    
    private final boolean     enabled;
    private final String      sourceId;
    private final HttpInvoker invoker;
    private final Clock       clock;
    private final BlockingQueue<HttpInvocation> invocationBuffer;
    private final Logger      logger;
    private final int         loggerGagPeriodMs;
    
    private volatile long     lastLogMessageTime;
    
    /**
     * @param enabled if set to {@code false}, invocations of the constructed recorder will not have any effect
     * @param sourceId the id of the component that is the source of events
     * @param invoker the {@link HttpInvoker} used to communicate with the server
     * @paran bufferSize the size of internal buffer
     * @param clock the {@link Clock} used to generate event timestamp
     */
    AsyncHttpRecorder(final boolean     enabled,
                      final String      sourceId,
                      final HttpInvoker invoker,
                      final int         bufferSize,
                      final Clock       clock) {
        this(enabled, sourceId, invoker, bufferSize, clock, DEFAULT_LOGGER, DEFAULT_LOGGER_GAG_PERIOD_MS);
    }
    
    /**
     * @param enabled if set to {@code false}, invocations of the constructed recorder will not have any effect
     * @param sourceId the id of the component that is the source of events
     * @param invoker the {@link HttpInvoker} used to communicate with the server
     * @paran bufferSize the size of internal buffer
     * @param clock the {@link Clock} used to generate event timestamp
     * @param logger the logger to use
     * @param loggerGagPeriodMs the highest frequency (in 1/milliseconds) at which a single type of message will be logged  
     */
    AsyncHttpRecorder(final boolean     enabled,
                      final String      sourceId,
                      final HttpInvoker invoker,
                      final int         bufferSize,
                      final Clock       clock,
                      final Logger      logger,
                      final int         loggerGagPeriodMs) {
        this.enabled = enabled;
        this.sourceId = nonEmpty(sourceId, "sourceId");
        this.invoker = nonNull(invoker, "invoker");
        this.invocationBuffer = new ArrayBlockingQueue<HttpInvocation>(positive(bufferSize, "bufferSize"));
        this.clock = nonNull(clock, "clock");
        this.logger = nonNull(logger, "logger");
        this.loggerGagPeriodMs = loggerGagPeriodMs;
        
        startHttpInvocationThread();
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
        event(new Event(sourceId, clock.getTime(), inputMessageIds, outputMessageIds, description));
    }
    
    /**
     * @see Recorder#event(Event)
     */
    public void event(final Event event) {
        if (enabled) {
            final boolean enqueued = invocationBuffer.offer(new EventSubmission(event));
            if (!enqueued && isAllowedToLog(lastLogMessageTime)) {
                logger.warn("buffer capacity of " + invocationBuffer.size() + " has been reached, unable to enqueue new invocations. Events will be missing!");
                lastLogMessageTime = System.currentTimeMillis();
            }
        }
    }

    private boolean isAllowedToLog(long lastLogMessageTime) {
        return System.currentTimeMillis() - lastLogMessageTime > loggerGagPeriodMs;
    }
    
    private void startHttpInvocationThread() {
        logger.debug("staring HTTP invoker thread...");
        final Thread invocationThread = new Thread(new BufferProcessor(), "clisson-http-invoker");
        invocationThread.setDaemon(true);
        invocationThread.start();
        logger.debug("HTTP invoker thread started");
    }
    
    private static interface HttpInvocation {
        void invoke(HttpInvoker invoker);
    }
    
    private static final class EventSubmission implements HttpInvocation {
        private final Event event;
        
        public EventSubmission(final Event event) {
            this.event = event;
        }
        
        public void invoke(HttpInvoker invoker) {
            invoker.post("/event", Json.jsonFor(event));
        }
    }
    
    private final class BufferProcessor implements Runnable {
        private volatile long lastLogMessageTime;
        
        public void run() {
            while (true) {
                try {
                    invocationBuffer.take().invoke(invoker);
                } catch (Exception e) {
                    if (isAllowedToLog(lastLogMessageTime)) {
                        logger.warn("error while invoking Clisson server over HTTP", e);
                        lastLogMessageTime = System.currentTimeMillis();
                    }
                }
            }
        }
    }
}
