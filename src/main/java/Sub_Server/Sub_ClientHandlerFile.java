package Sub_Server;




import Client.TransferFileController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Sub_ClientHandlerFile extends Thread {

    @FXML
    private TextArea taYourPartner;
    @FXML
    private Button btnFastDownload;
    @FXML
    private Button btnOpenFile;
    @FXML
    private TextArea taYourFile;

    @FXML
    private Button btnOpenFolder;
    @FXML
    private VBox vBoxDownload;
    @FXML
    private VBox vBoxSend;
    @FXML
    private Button btnCloseConnect;


    private Socket clientSocket;
    private TransferFileController transferFileController;
    private ServerSocket server;


    public Sub_ClientHandlerFile(TransferFileController transferFileController, TextArea taYourPartner,Button btnFastDownload,Button btnOpenFile,TextArea taYourFile,Button btnOpenFolder,VBox vBoxDownload,VBox vBoxSend,Button btnCloseConnect) {
        this.transferFileController = transferFileController;
        this.taYourPartner = taYourPartner;
        this.btnFastDownload = btnFastDownload;
        this.btnOpenFile = btnOpenFile;
        this.taYourFile = taYourFile;
        this.btnOpenFolder = btnOpenFolder;
        this.vBoxDownload = vBoxDownload;
        this.vBoxSend = vBoxSend;
        this.btnCloseConnect = btnCloseConnect;
    }
    @Override
    public void run() {
        File_Transfer();
    }

    public void File_Transfer() {
        try {
            server = new ServerSocket(9090);
            System.out.println("Tạo server truyền file thành công!");
            while(true)
            {
                clientSocket = server.accept();
                System.out.println("Truyền File được rồi");
                showErrorAlert("Bạn có thông báo mới","Đã có người kết nối đến bạn");
                Thread senderThread = new Thread(() -> {
                    btnOpenFile.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            new SendFile(clientSocket,btnOpenFile,taYourFile,vBoxSend);
                        }
                    });
                    btnCloseConnect.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            try {
                                clearViewTransfer();
                                clientSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                });

                Thread receiverThread = new Thread(() -> {
                    new ReceiveFile(clientSocket,taYourPartner,btnFastDownload,vBoxDownload,vBoxSend);
                });

                senderThread.start();
                receiverThread.start();


                Thread sendFolder = new Thread(() ->{
                    btnOpenFolder.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            new SendFolder(clientSocket,btnOpenFolder,taYourFile,vBoxSend);
                        }
                    });
                });


                sendFolder.start();


            }


        } catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println(e);
        }
    }
    private void clearViewTransfer() {
        Platform.runLater(() -> {
            vBoxSend.getChildren().clear();
            vBoxDownload.getChildren().clear();
        });
    }
    private void showErrorAlert(String title, String header) {
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
    }

}

