module com.chronologic {
    requires java.sql;
    requires java.desktop;
    requires javafx.fxml;
    requires javafx.controls;
    requires im4java;
    requires org.slf4j;
    requires metadata.extractor;
    requires org.apache.tika.core;
    requires org.apache.commons.io;

    exports com.chronologic.ui;
    exports com.chronologic.core;

    opens com.chronologic.ui to javafx.fxml;
}