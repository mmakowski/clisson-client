package com.bimbr.clisson.client;

import static java.lang.Thread.currentThread;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the configuration of the recorder and provides a way to read the configuration from
 * a properties file.
 * 
 * @author mmakowski
 * @since 1.0.0
 */
public class Config {
    private static final Logger log = LoggerFactory.getLogger(Config.class);
    
    private static final String CLASSPATH_PREFIX = "classpath://";
    private static final String FILE_PREFIX = "file://";
    private static final String CONFIG_PROPERTY = "clisson.config";
    private static final String DEFAULT_CONFIG_PATH = CLASSPATH_PREFIX + "clisson.properties";
    /** this config will be used if the file supplied by the user or the default config file are not found */
    private static final String FALLBACK_CONFIG_PATH = CLASSPATH_PREFIX + "__clisson-fallback.properties";
    
    protected static final String COMPONENT_ID   = "clisson.componentId";
    protected static final String RECORD_ENABLED = "clisson.record.enabled";
    protected static final String SERVER_HOST    = "clisson.server.host";
    protected static final String SERVER_PORT    = "clisson.server.port";
    
    private final String host;
    private final int port;
    private final String componentId;
    private final boolean isRecordingEnabled;
    
    public static Config fromPropertiesFile() {
        final Properties properties = validatedProperties(new PropertyValidator());
        return new Config(Boolean.valueOf(properties.getProperty(RECORD_ENABLED, "true")),
                          properties.getProperty(SERVER_HOST), 
                          Integer.valueOf(properties.getProperty(SERVER_PORT)),
                          properties.getProperty(COMPONENT_ID));
    }
    
    protected static Properties validatedProperties(PropertyValidator propertyValidator) {
        final String path = propertiesPath();
        final Properties properties = propertiesFromFile(path);
        propertyValidator.validate(properties, path);
        return properties;
    }
 
    private static String propertiesPath() {
        return System.getProperty(CONFIG_PROPERTY, DEFAULT_CONFIG_PATH);
    }
    
    private static Properties propertiesFromFile(String propertiesPath) {
        InputStream stream = null;
        try {
            stream = inputStreamFrom(propertiesPath);
            final Properties properties = new Properties();
            properties.load(stream);
            return properties;
        } catch (FileNotFoundException e) {
            final Properties defaultProperties = propertiesFromFile(FALLBACK_CONFIG_PATH);
            log.warn("config file " + propertiesPath + " not found; will use default properties: " + defaultProperties);
            return defaultProperties;
        } catch (IOException e) {
            throw new IllegalStateException("error when loading Clisson client properties from " + propertiesPath, e);
        } finally {
            closeQuietly(stream);
        }
    }

    private static InputStream inputStreamFrom(String propertiesPath) throws FileNotFoundException {
        final InputStream stream;
        if (propertiesPath.startsWith(CLASSPATH_PREFIX)) stream = inputStreamFromClasspathFile(propertiesPath.substring(CLASSPATH_PREFIX.length()));
        else if (propertiesPath.startsWith(FILE_PREFIX)) stream = inputStreamFromFilesystemFile(propertiesPath.substring(FILE_PREFIX.length()));
        else throw new IllegalStateException("properties file path must start with either " + CLASSPATH_PREFIX + " or " + FILE_PREFIX + "; path supplied: " + propertiesPath);
        return stream;
    }

    private static InputStream inputStreamFromFilesystemFile(String propertiesPath) throws FileNotFoundException {
        return new FileInputStream(propertiesPath);
    }

    private static InputStream inputStreamFromClasspathFile(String propertiesPath) throws FileNotFoundException {
        final InputStream stream = currentThread().getContextClassLoader().getResourceAsStream(propertiesPath);
        if (stream == null) throw new FileNotFoundException(propertiesPath);
        return stream;
    }

    private static void closeQuietly(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // quietly!
            }
        }
    }

    /**
     * Exception thrown when the config is incorrect
     * @author mmakowski
     * @since 1.0.0
     */
    public static final class ConfigException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ConfigException(String path, String message) {
            super("invalid config in " + path + ": " + message);
        }
    }
    
    protected Config(final boolean isRecordingEnabled, 
                     final String  host, 
                     final int     port,
                     final String  componentId) {
        this.isRecordingEnabled = isRecordingEnabled;
        this.host               = host;
        this.port               = port;
        this.componentId        = componentId;
    }

    /**
     * @return Clisson server host name
     */
    public String getHost() {
        return host;
    }

    /**
     * @return Clisson server port
     */
    public int getPort() {
        return port;
    }
    
    /**
     * @return the component identifier used to mark recorded events
     */
    public String getComponentId() {
        return componentId;
    }
    
    /**
     * @return {@code true} if event recording is enabled, {@code false} otherwise  
     */
    public boolean isRecordingEnabled() {
        return isRecordingEnabled;
    }
    
    protected static class PropertyValidator {
        public void validate(Properties properties, String configPath) {
            validateNotEmptyString(properties, configPath, SERVER_HOST, "host name");
            final String portStr = properties.getProperty(SERVER_PORT);
            if (portStr == null || Integer.valueOf(portStr) <= 0) throw new ConfigException(configPath, SERVER_PORT + " must be set to a positive integer");
            validateNotEmptyString(properties, configPath, COMPONENT_ID, "component identifier");
        }

        private void validateNotEmptyString(Properties properties,
                String configPath, String propertyKey, String propertyDescription) {
            final String host = properties.getProperty(propertyKey);
            if (isEmpty(host)) throw new ConfigException(configPath, propertyKey + " must be set to a non-empty " + propertyDescription);
        }

        private static boolean isEmpty(final String str) {
            return str == null || str.length() == 0;
        }
    }
}
