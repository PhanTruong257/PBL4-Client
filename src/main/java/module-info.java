module client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires webcam.capture;
    requires javafx.swing;

    opens Client to javafx.fxml;
    exports Client;
}