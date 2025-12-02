package com.artem.brewery.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final String DEFAULT_ENV_FILE = "env.properties";
    private static final String ENV_FILE_PREFIX = "env-";
    private static final String ENV_FILE_SUFFIX = ".properties";
    private static final String SYSTEM_PROPERTY_ENV = "env";

    private final Properties properties;

    public ConfigLoader() {
        this.properties = loadProperties();
    }

    private Properties loadProperties() {
        String environment = System.getProperty(SYSTEM_PROPERTY_ENV);
        String fileName = getConfigFileName(environment);

        Properties props = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                System.err.println("Unable to find " + fileName + ", using default: " + DEFAULT_ENV_FILE);
                return loadDefaultProperties();
            }
            props.load(input);
            System.out.println("Loaded configuration from: " + fileName);
        } catch (IOException e) {
            System.err.println("Error loading configuration file: " + fileName);
            e.printStackTrace();
            return loadDefaultProperties();
        }

        return props;
    }

    private Properties loadDefaultProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(DEFAULT_ENV_FILE)) {
            if (input != null) {
                props.load(input);
                System.out.println("Loaded default configuration from: " + DEFAULT_ENV_FILE);
            } else {
                throw new RuntimeException("Default configuration file not found: " + DEFAULT_ENV_FILE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default configuration", e);
        }
        return props;
    }

    private String getConfigFileName(String environment) {
        if (environment == null || environment.trim().isEmpty()) {
            return DEFAULT_ENV_FILE;
        }
        return ENV_FILE_PREFIX + environment.trim().toLowerCase() + ENV_FILE_SUFFIX;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}

