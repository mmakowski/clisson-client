package com.bimbr.clisson.client;

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
 * <tr><td>{@code clisson.componentId}</td><td>yes</td><td></td><td>the identifier of component for the purpose of event identification</td></tr>
 * <tr><td>{@code clisson.record.enabled}</td><td>no</td><td>{@code true}</td><td>whether sending of events to the server is enabled</td></tr>
 * <tr><td>{@code clisson.server.host}</td><td>yes</td><td></td><td>the host name of Clisson server</td></tr>
 * <tr><td>{@code clisson.server.port}</td><td>yes</td><td></td><td>the port on which Clisson server listens</td></tr>
 * </table>
 * <p>
 * The factory guarantees to create only a single instance of {@code Recorder} for the entire application.
 * 
 * @author mmakowski
 * @since 1.0.0
 */
public final class RecorderFactory {
    private static Recorder recorder;
    
    /**
     * Constructs a {@link Recorder} using the config specified in properties file.
     * @return a {@link Recorder} built based on the config loaded from properties file
     * @since 1.0.0
     */
    public static synchronized Recorder getRecorder() {
        if (recorder == null) {
            final Config config = Config.fromPropertiesFile();
            return getRecorder(config);
        } else {
            return recorder;
        }
    }
    
    /**
     * @param config the config to use when constructing the recorder
     * @return a {@link Recorder} with properties taken from {@code config}
     * @since 1.0.0
     */
    public static synchronized Recorder getRecorder(final Config config) {
        if (recorder == null) {
            recorder = recorder(config);
        }
        return recorder;
    }

    private static Recorder recorder(Config config) {
        final SimpleHttpInvoker invoker = new SimpleHttpInvoker(config.getHost(), config.getPort());
        return new AsyncHttpRecorder(config.isRecordingEnabled(), 
                                     config.getComponentId(), 
                                     invoker, 
                                     1000, 
                                     new Clock());        
    }
    
    /**
     * for resetting cached Recorders in tests
     */
    static synchronized void reset() {
        recorder = null;
    }
}
