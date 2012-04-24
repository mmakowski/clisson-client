package com.bimbr.clisson.client.log4j;

import java.util.Properties;

/**
 * An extension of {@link com.bimbr.clisson.client.Config} that, in addition to the Clisson server settings also
 * requires log4j-specific settings.
 * 
 * @author mmakowski
 * @since 1.0.0
 */
class Config extends com.bimbr.clisson.client.Config {
    private static final String LOG4J_EVENTTRANSFORMATION = "clisson.log4j.eventTransformation";
    
    private final Class<EventTransformation> transformationClass;
    
    /**
     * @return config constructed from the default or user-specified properties file
     * @see com.bimbr.clisson.client.Config#fromPropertiesFile()
     */
    public static Config fromPropertiesFile() {
        final Properties properties = validatedProperties(new PropertyValidator());
        return new Config(Boolean.valueOf(properties.getProperty(RECORD_ENABLED, "true")),
                          properties.getProperty(SERVER_HOST), 
                          Integer.valueOf(properties.getProperty(SERVER_PORT)),
                          properties.getProperty(LOG4J_EVENTTRANSFORMATION));
    }

    @SuppressWarnings("unchecked") // loading class dynamically
    private Config(final boolean isRecordingEnabled, final String host, final int port, final String className) {
        super(isRecordingEnabled, host, port);
        try {
            transformationClass = (Class<EventTransformation>) Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("className is set to non-existent class " + className, e);
        }
    }
    
    private static final class PropertyValidator extends com.bimbr.clisson.client.Config.PropertyValidator {
        @Override
        public void validate(Properties properties, String configPath) {
            super.validate(properties, configPath);
            final String className = properties.getProperty(LOG4J_EVENTTRANSFORMATION);
            try {
                final Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(className);
                if (!EventTransformation.class.isAssignableFrom(cls)) 
                    throw new ConfigException(configPath, LOG4J_EVENTTRANSFORMATION + " is set to " + className + " which does not implement " + EventTransformation.class);
            } catch (ClassNotFoundException e) {
                throw new ConfigException(configPath, LOG4J_EVENTTRANSFORMATION + " is set to " + className + " which could not be found");
            }
        }
    }
    
    /**
     * @return the class of transformation to be used to map log4j events to Clisson events
     * @since 1.0.0
     */
    public Class<EventTransformation> getTransformationClass() {
        return transformationClass;
    }   
}
