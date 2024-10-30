package Client;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ReceivingScreen extends Thread {
    private InputStream in;
    private ImageView imageView;
    private BorderPane cPane;

    public ReceivingScreen(InputStream in, ImageView imageView, BorderPane cPane) {
        this.in = in;
        this.imageView = imageView;
        this.cPane = cPane;
        start();
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[4]; // Buffer to read the image length
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                if (bytesRead != 4) {
                    System.err.println("Invalid header length");
                    continue;
                }

                int imageLength = ByteBuffer.wrap(buffer).getInt();
                byte[] imageData = new byte[imageLength];
                int totalBytesRead = 0;

                while (totalBytesRead < imageLength) {
                    bytesRead = in.read(imageData, totalBytesRead, imageLength - totalBytesRead);
                    if (bytesRead == -1) {
                        break;
                    }
                    totalBytesRead += bytesRead;
                }

                if (totalBytesRead != imageLength) {
                    System.err.println("Incomplete image data");
                    continue;
                }

                Platform.runLater(() -> {;
                    Image image = new Image(new ByteArrayInputStream(imageData),
                            imageView.getFitWidth(), imageView.getFitHeight(), true, true);
                    imageView.setImage(image);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}