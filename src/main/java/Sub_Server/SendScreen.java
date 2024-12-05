package Sub_Server;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SendScreen {

    private Robot robot;
    private Socket socket;
    public SendScreen(Robot robot, Socket socket) {
        this.robot = robot;
        this.socket = socket;
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            while (true) {
                BufferedImage screenCapture = robot.createScreenCapture(screenRect);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(screenCapture,"jpg",byteArrayOutputStream);
                byte [] imageBytes = byteArrayOutputStream.toByteArray();
                //send image length as metadata
                int imageLength = imageBytes.length;
                OutputStream out = socket.getOutputStream();
                out.write(ByteBuffer.allocate(4).putInt(imageLength).array());
                out.write(imageBytes);
                out.flush();
                Thread.sleep(10);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
