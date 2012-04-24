package com.bimbr.clisson.client;

import static java.lang.Thread.currentThread;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Represents the configuration of the recorder and provides a way to read the configuration from
 * a properties file.
 * 
 * @author mmakowski
 * @since 1.0.0
 */
public class Config {
    private static final String CLASSPATH_PREFIX = "classpath://";
    private static final String FILE_PREFIX = "file://";
    private static final String CONFIG_PROPERTY = "clisson.config";
    private static final String DEFAULT_CONFIG_PATH = CLASSPATH_PREFIX + "clisson.properties";
    
    protected static final String RECORD_ENABLED = "clisson.record.enabled";
    protected static final String SERVER_HOST    = "clisson.server.host";
    protected static final String SERVER_PORT    = "clisson.server.port";
    
    private final String host;
    private final int port;
    private final boolean isRecordingEnabled;
    
    public static Config fromPropertiesFile() {
        final Properties properties = validatedProperties(new PropertyValidator());
        return new Config(Boolean.valueOf(properties.getProperty(RECORD_ENABLED, "true")),
                          properties.getProperty(SERVER_HOST), 
                          Integer.valueOf(properties.getProperty(SERVER_PORT)));
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
            throw new IllegalStateException("config file " + propertiesPath + " not found");
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
                     final int     port) {
        this.isRecordingEnabled = isRecordingEnabled;
        this.host               = host;
        this.port               = port;
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
     * @return {@code true} if event recording is enabled, {@code false} otherwise  
     */
    public boolean isRecordingEnabled() {
        return isRecordingEnabled;
    }
    
    protected static class PropertyValidator {
        public void validate(Properties properties, String configPath) {
            final String host = properties.getProperty(SERVER_HOST);
            if (host == null || host.length() == 0) throw new ConfigException(configPath, SERVER_HOST + " must be set to a non-empty host name");
            final String portStr = properties.getProperty(SERVER_PORT);
            if (portStr == null || Integer.valueOf(portStr) <= 0) throw new ConfigException(configPath, SERVER_PORT + " must be set to a positive integer");
        }
    }
}
