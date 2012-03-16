package com.bimbr.clisson.client.log4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.bimbr.clisson.client.Recorder;
import com.bimbr.clisson.client.RecorderFactory;

/**
 * A log4j appender that transforms logging events to Clisson events and sends them to Clisson server.
 * TODO: describe config
 * 
 * @author mmakowski
 * @since 1.0.0
 */
public final class ClissonAppender extends AppenderSkeleton {
    private final EventTransformation transformation;
    private final Recorder record;
    
    public ClissonAppender() {
        final Config config = Config.fromPropertiesFile();
        try {
            this.transformation = newInstanceOf(config.getTransformationClass());
        } catch (Exception e) {
            throw new IllegalStateException("error creating an instance of " + config.getTransformationClass(), e);
        }
        this.record         = RecorderFactory.getRecorder("log4j", config);
    }

    private EventTransformation newInstanceOf(final Class<EventTransformation> transformationClass) 
            throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, 
                   IllegalAccessException, InvocationTargetException {
        final Constructor<EventTransformation> constructor = transformationClass.getConstructor();
        return constructor.newInstance();
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
        record.event(transformation.perform(log4jEvent));
    }
}
