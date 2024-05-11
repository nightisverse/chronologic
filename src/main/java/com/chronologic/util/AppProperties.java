package com.chronologic.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppProperties extends Properties {

    private static final AppProperties appProperties;

    static {
        appProperties = new AppProperties();

        try (InputStream inputStream = AppProperties.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (inputStream == null) {
                throw new RuntimeException();
            }

            appProperties.load(inputStream);

        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex.getMessage());
        }
    }

    private AppProperties() {
    }

    public static String getAppProperty(String key) {
        return appProperties.getProperty(key);
    }

}

