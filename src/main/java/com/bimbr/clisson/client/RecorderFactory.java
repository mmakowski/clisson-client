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
 * The following properties are supported:
 * <table>
 * <thead>
 * <tr><td>property</td><td>required</td><td>default value</td><td>description</td></tr>
 * </thead>
 * <tr><td>{@code clisson.record.enabled}</td><td>no</td><td>{@code true}</td><td>whether sending of events to the server is enabled</td></tr>
 * <tr><td>{@code clisson.server.host}</td><td>yes</td><td></td><td>the host name of Clisson server</td></tr>
 * <tr><td>{@code clisson.server.port}</td><td>yes</td><td></td><td>the port on which Clisson server listens</td></tr>
 * </table>
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
        synchronized (recorders) {
            if (!recorders.containsKey(sourceId)) {
                recorders.put(sourceId, recorder(sourceId, config));
            }
            return recorders.get(sourceId);
        }
    }

    private static Recorder recorder(String sourceId, Config config) {
        final SimpleHttpInvoker invoker = new SimpleHttpInvoker(config.getHost(), config.getPort());
        return new AsyncHttpRecorder(config.isRecordingEnabled(), sourceId, invoker, 1000, new Clock());        
    }
    
    /**
     * for resetting cached Recorders in tests
     */
    static void reset() {
        recorders.clear();
    }
}
