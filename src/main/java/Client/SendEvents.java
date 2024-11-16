package Client;

import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SendEvents {
    private Scene scene;
    private Socket socketClient = null;
    private BorderPane cPane = null;
    private PrintWriter writer = null;
    private ImageView imageView;
    String width = "";
    String height = "";
    double w;
    double h;

    public SendEvents(Socket s, BorderPane p, String width, String height, Scene scene, ImageView imageView) {
        this.socketClient = s;
        this.cPane = p;
        this.width = width;
        this.height = height;
        this.imageView = imageView;
        this.w = Double.parseDouble(width);
        this.h = Double.parseDouble(height);
        this.scene = scene;
        try {
            writer = new PrintWriter(socketClient.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        scene.setOnKeyPressed(event -> {
            KeyCode keyCode = event.getCode();
            int asciiCode = keyCode.getCode();

            // Gửi mã phím tới phía server
            writer.println(Commands.PRESS_KEY.getAbbrev());
            writer.println(asciiCode);
            writer.flush();
        });

        scene.setOnKeyReleased(event -> {
            KeyCode keyCode = event.getCode();
            int asciiCode = keyCode.getCode();

            // Gửi mã phím tới phía server
            writer.println(Commands.RELEASE_KEY.getAbbrev());
            writer.println(asciiCode);
            writer.flush();
        });
//
        cPane.setOnMousePressed(event -> {
            // Handle mouse press events
            MouseButton button = event.getButton();
            int xButton = 0; // Initialize to an undefined value

            if (button == MouseButton.PRIMARY) {
                xButton = Commands.PRESS_MOUSE_PRIMARY.getAbbrev();
            } else if (button == MouseButton.SECONDARY) {
                xButton = Commands.PRESS_MOUSE_SECONDARY.getAbbrev();
            }

            writer.println(xButton);
            writer.flush();
        });
//
        cPane.setOnMouseReleased(mouseEvent -> {
            // Handle mouse release events
            MouseButton button = mouseEvent.getButton();
            int xButton = 0; // Initialize to an undefined value

            if (button == MouseButton.PRIMARY) {
                xButton = Commands.RELEASE_MOUSE_PRIMARY.getAbbrev();
            } else if (button == MouseButton.SECONDARY) {
                xButton = Commands.RELEASE_MOUSE_SECONDARY.getAbbrev();
            }
            writer.println(xButton);
            writer.flush();
        });
//
//

        cPane.setOnMouseDragged(event -> {
            // Handle mouse drag events
            double xScale =  w / cPane.getWidth();
            double yScale =  h / cPane.getHeight();

            writer.println(Commands.DRAG_MOUSE.getAbbrev());
            writer.println((int) (event.getX() * xScale));
            writer.println((int) (event.getY() * yScale));
            writer.flush();
        });
//
        cPane.setOnMouseMoved(event -> {
            // Handle mouse move events
            double xScale = w / cPane.getWidth();
            double yScale = h / cPane.getHeight();

            writer.println(Commands.MOVE_MOUSE.getAbbrev());
            writer.println((int) (event.getX() * xScale));
            writer.println((int) (event.getY() * yScale));
            writer.flush();
        });

        cPane.setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY();
            writer.println(Commands.SCROLL_MOUSE.getAbbrev());
            writer.println((int)deltaY);
            writer.flush();
        });
    }
}