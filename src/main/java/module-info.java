module pbl4.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens pbl4.client to javafx.fxml;
    exports pbl4.client;
}