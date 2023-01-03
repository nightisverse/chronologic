package com.chronologic.core;

import com.chronologic.util.AppProperties;

public class MediaFileProcessingException extends RuntimeException {

    public MediaFileProcessingException(String propertyKey) {
        super(AppProperties.getAppProperty(propertyKey));
    }

}
