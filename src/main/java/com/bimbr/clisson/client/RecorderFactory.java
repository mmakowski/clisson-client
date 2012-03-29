package com.bimbr.clisson.client;

import java.util.HashMap;
import java.util.Map;

import com.bimbr.util.Clock;


/**
 * A factory that constructs {@link Recorder}s based on the config specified in a properties file pointed to by 
 * {@code clisson.config} system property. If the property is not specified it attempts to load the config from
 * the {@code classpath://clisson.properties} file.  
 * <p>
 * The path to the config file must be prefixed with protocol, {@code classpath://} or {@code file://} that determines
 * how the file is searched for. Examples of paths:
 * <ul>
 * <li>{@code classpath://conf/clisson-test.properties}</li>
 * <li>{@code file://c:/myapp/conf/clisson.properties}</li>
 * </ul>
 * <p>
 * The config file must contain the following properties:
 * <ul>
 * <li>{@code clisson.server.host} - the host name of Clisson server</li>
 * <li>{@code clisson.server.port} - the port on which Clisson server listens</li>
 * </ul>
 * <p>
 * The factory guarantees to create only a single instance of recorder for each {@code sourceId}.
 * 
 * @author mmakowski
 * @since 1.0.0
 */
public final class RecorderFactory {
    private static final Map<String, Recorder> recorders = new HashMap<String, Recorder>();
    
    /**
     * Constructs a {@link Recorder} using the config specified in properties file.
     * @param sourceId the id of the source of events
     * @return a {@link Recorder} for specified source
     * @since 1.0.0
     */
    public static Recorder getRecorder(final String sourceId) {
        final Config config = Config.fromPropertiesFile();
        return getRecorder(sourceId, config);
    }
    
    /**
     * 
     * @param sourceId
     * @param config
     * @return a {@link Recorder} for specified source with properties taken
     * @since 1.0.0
     */
    public static Recorder getRecorder(final String sourceId, final Config config) {
        if (!recorders.containsKey(sourceId)) {
            recorders.put(sourceId, recorder(sourceId, config));
        }
        return  recorders.get(sourceId);
    }

    private static Recorder recorder(String sourceId, Config config) {
        final HttpInvoker invoker = new HttpInvoker(config.getHost(), config.getPort());
        return new AsyncHttpRecorder(sourceId, invoker, 1000, new Clock());        
    }
}
