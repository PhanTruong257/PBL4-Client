package Sub_Server;

import Client.ChatViewController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Sub_ClientHandlerChat extends Thread {

    private Socket clientSocket;
    private ServerSocket server;

    private ChatViewController chatViewController;

    @FXML
    private VBox vbox_message;
    @FXML
    private TextField tf_message;
    @FXML
    private Button button_send;
    @FXML
    private Button btnCloseConnect;
    @FXML
    private TextField tfPartnerID;


    public Sub_ClientHandlerChat(ChatViewController chatViewController, VBox vbox_message, Button button_send, TextField tf_message, Button btnCloseConnect, TextField tfPartnerID) {

        this.chatViewController = chatViewController;

        this.vbox_message = vbox_message;
        this.button_send = button_send;
        this.tf_message = tf_message;
        this.btnCloseConnect = btnCloseConnect;
        this.tfPartnerID = tfPartnerID;
    }

    @Override
    public void run() {
        Chatting();
    }

    public void Chatting() {
        try {
            server = new ServerSocket(9999);
            while (true) {
                clientSocket = server.accept();
                System.out.println("accept");


                //Lấy IP của client
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                //Thông báo có client kết nối đến
                showErrorAlert("Thông báo", clientAddress + " đã kết nối đến");
                Platform.runLater(() -> {
                    tfPartnerID.setText(clientAddress);
                });

                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());

                //Luồng gửi tin nhắn
                Thread senderThread = new Thread(() -> {
                    button_send.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            try {
                                String message;
                                message = tf_message.getText();
                                out.writeUTF(message);
                                chatViewController.addLabelSend(message, vbox_message);
                                tf_message.setText("");
                                out.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    tf_message.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            try {
                                String message;
                                message = tf_message.getText();
                                out.writeUTF(message);
                                chatViewController.addLabelSend(message, vbox_message);
                                tf_message.setText("");
                                out.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    btnCloseConnect.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            try {
                                String message = "Connect is closed by partner";
                                out.writeUTF(message);
                                out.flush();
                                clearChatView();
                                out.close();
                                in.close();
                                clientSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                });

                //Luồng Nhận tin nhắn
                Thread receiverThread = new Thread(() -> {
                    try {
                        String message;
                        while (true) {
                            message = in.readUTF();
                            if (message.equals("Connect is closed by partner")) {
                                in.close();
                                out.close();
                                clientSocket.close();
                                showErrorAlert("Alert", "Connect is closed by partner!");
                                clearChatView();
                            } else {
                                System.out.println(message);
                                this.chatViewController.addLabelReceive(message, vbox_message);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                senderThread.start();
                receiverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearChatView() {
        Platform.runLater(() -> {
            vbox_message.getChildren().clear();
        });
    }

    private void showErrorAlert(String title, String header) {
        Platform.runLater(() -> {
            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            Label label = new Label(header);
            label.setWrapText(true);
            StackPane root = new StackPane();
            root.getChildren().add(label);
            Scene scene = new Scene(root, 300, 100);
            dialogStage.setTitle(title);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        });
    }
}
