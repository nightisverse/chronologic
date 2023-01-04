package com.chronologic.util;

import com.chronologic.core.ChronoLogicRunner;

public class ErrorHandler {

    public static void showErrorMessage(String errorMessage) {
        ChronoLogicRunner.getMainController().showErrorMessage(errorMessage);
    }

    public static void showErrorMessageFromProperty(String propertyKey) {
        showErrorMessage(AppProperties.getAppProperty(propertyKey));
    }

}
