package Client;


import Sub_Server.Sub_server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class RemoteDesktop  {

    @FXML
    private TextField localIp;

    @FXML
    private TextField localPwd;

    private Sub_server sub_server = null;
    @FXML
    private TextField remoteIp;

    @FXML
    private PasswordField remotePwd;

    private Socket socketClient = null;

    private DataOutputStream out;

    private DataInputStream in;

    private String message ="";

    private int activated = 0;

    private static int count = 0;

    public void setSocketClient(Socket socketClient, DataOutputStream out, DataInputStream in, Sub_server sub_server) {
        this.sub_server = sub_server;
        this.socketClient = socketClient;
        this.in = in;
        this.out = out;
        System.out.println("hello1");
        if (activated == 0) {
            Init();
            activated = 1;
        }
    }

    public void setValue(String pwd)
    {
        try {
            // Lấy địa chỉ IP của máy
            String ipAddress = InetAddress.getLocalHost().getHostAddress();

            // Đặt giá trị của tfYourID bằng địa chỉ IP
            localIp.setText(ipAddress);

            //Password default
            localPwd.setText(pwd);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void Init() {
        System.out.println("hello2");


        // Tạo một luồng để nhận tin nhắn
        Thread receiverThread = new Thread(() -> {
            try {
                while (true) {
                    System.out.println("vao day");
                    message = in.readUTF();
                    System.out.println("hello3");
                    System.out.println(message);
                    //Class xử lý tin nhắn
                    MessageHandler msg = new MessageHandler(message);
                    String data = msg.getData();
                    if (!data.equals("True") && !data.equals("False") && !message.isEmpty()) {
                        System.out.println(localPwd.getText());
                        data = data.equals(localPwd.getText()) ? "True" : "False";
                        message = msg.getReceiver() + "," + msg.getStatus() + "," + data;
                        out.writeUTF(message);
                        out.flush();
                    }
                    //True thì tạo luồng khác để remote
                    else if(data.equals("True")) {
                        Platform.runLater(() -> {
                            Stage remoteStage = new Stage();
                            FXMLLoader remoteLoader = new FXMLLoader(getClass().getResource("RemoteWindow.fxml"));
                            Pane remoteRoot = null;
                            try {
                                remoteRoot = remoteLoader.load();
                            } catch (IOException e) {
                                throw new RuntimeException();
                            }

                            Scene remoteScene = new Scene(remoteRoot);

                            remoteStage.setScene(remoteScene);

                            remoteStage.show();

                            RemoteWindow remoteController = remoteLoader.getController();
                            remoteController.getIp(remoteIp.getText(), remoteScene, remoteStage);
                            remoteController.start();
                        });
                        message = "";
                        out.writeUTF(message);
                        out.flush();
                    }
                    else {
                        message = "";
                        out.writeUTF(message);
                        out.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        receiverThread.start();
    }
    public void connectBtn(MouseEvent mouseEvent) {
        try {
            out.writeUTF(remoteIp.getText() + ",abc,"+ remotePwd.getText());
            out.flush();
            System.out.println("press button");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
