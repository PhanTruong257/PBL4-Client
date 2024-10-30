package Client;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class RemoteWindow extends Thread{
    private Scene scene;

    @FXML
    private BorderPane borderPane;
    @FXML
    private ImageView imageView;
    private String ip;

    private String width;

    private String height;

    private Socket socket;
    private Stage remoteStage;

    public void getIp(String ip, Scene scene, Stage remoteStage) {
        this.ip = ip;
        this.scene = scene;
        this.remoteStage = remoteStage;
        System.out.println(ip);
    }


    @Override
    public void run() {

        remoteStage.setOnCloseRequest(e -> {
            try {
                socket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        try {
            socket = new Socket(ip, 2508);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("1");
            out.flush();
            DataInputStream initSize = new DataInputStream(socket.getInputStream());
            width = initSize.readUTF();
            height = initSize.readUTF();
            remoteStage.setWidth(Double.parseDouble(width));
            remoteStage.setHeight(Double.parseDouble(height));
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }

        InputStream in = null;
        try {
            in = socket.getInputStream();
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            e.printStackTrace();
        }



        new ReceivingScreen(in, imageView, borderPane);
        new SendEvents(socket, borderPane, width, height, scene, imageView);
    }


}
