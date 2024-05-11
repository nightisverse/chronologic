module com.chronologic {
    requires java.desktop;
    requires javafx.fxml;
    requires javafx.controls;
    requires im4java;
    requires org.apache.commons.io;

    exports com.chronologic.ui;
    exports com.chronologic.core;

    opens com.chronologic.ui to javafx.fxml;
}