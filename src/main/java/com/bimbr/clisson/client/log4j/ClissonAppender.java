package com.bimbr.clisson.client.log4j;

import java.lang.reflect.Constructor;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.bimbr.clisson.client.Recorder;
import com.bimbr.clisson.client.RecorderFactory;
import com.bimbr.clisson.client.log4j.EventTransformation.IgnoreEventException;

/**
 * A log4j appender that transforms logging events to Clisson events and sends them to Clisson server.
 * When {@code ClissonAppender}, in addition to the settings described in {@link com.bimbr.clisson.client.RecorderFactory},
 * the config file must contain:  
 * <ul>
 * <li>{@code clisson.log4j.eventTransformation} - the full name of the class that implements {@link EventTransformation} 
 * to be used for transforming log4j events to Clisson events</li>
 * </ul>
 * 
 * @author mmakowski
 * @since 1.0.0
 */
public final class ClissonAppender extends AppenderSkeleton {
    private final EventTransformation transformation;
    private final Recorder record;

    /**
     * Constructs 
     * @since 1.0.0
     */
    public ClissonAppender() {
        final Config config = Config.fromPropertiesFile();
        this.transformation = newInstanceOf(config.getTransformationClass());
        this.record         = RecorderFactory.getRecorder(config);
    }

    private EventTransformation newInstanceOf(final Class<EventTransformation> transformationClass) { 
        try {
            final Constructor<EventTransformation> constructor = transformationClass.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("error creating an instance of " + transformationClass, e);
        }
    }

    /**
     * For unit testing.
     */
    ClissonAppender(final EventTransformation transformation,
                    final Recorder            recorder) {
        this.transformation = transformation;
        this.record         = recorder;
    }
    
    /**
     * Doesn't do anything
     * @see org.apache.log4j.Appender#close()
     */
    public void close() {
        // no need to do anything
    }

    /**
     * @see org.apache.log4j.Appender#requiresLayout()
     */
    public boolean requiresLayout() {
        return false;
    }

    protected void append(final LoggingEvent log4jEvent) {
        try {
            record.event(transformation.perform(log4jEvent));
        } catch (IgnoreEventException e) {
            // log4jEvent should be ignored
        }
    }
}
