module com.chronologic {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires metadata.extractor;
    requires org.apache.commons.io;
    requires org.apache.tika.core;
    requires org.slf4j;
    requires java.sql;

    exports com.chronologic.core;
    exports com.chronologic.ui;
    opens com.chronologic.ui to javafx.fxml;
}