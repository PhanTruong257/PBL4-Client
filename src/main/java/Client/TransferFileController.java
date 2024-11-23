package Client;

import Sub_Server.*;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.*;

import java.io.*;
import java.net.*;
import java.util.ResourceBundle;


public class TransferFileController implements Initializable {
    @FXML
    private Button btnFastDownload;
    @FXML
    private Button btnConnect;
    @FXML
    private Button btnCloseConnect;
    @FXML
    private Button btnOpenFile;
    @FXML
    private Button btnOpenFolder;



    @FXML
    private Label labelPartnerFile;
    @FXML
    private Label labelYourFile;
    @FXML
    private Label labelYourID;


    @FXML
    private TextArea taYourFile;
    @FXML
    private TextArea taYourPartner;


    @FXML
    private TextField tfYourID;
    @FXML
    private TextField tfPartnerID;

    @FXML
    private VBox vBoxDownload;
    @FXML
    private VBox vBoxSend;




    private ServerSocket serverSocket;
    private Socket socket;
    private OutputStream outputStream;


    private Thread senderThread;
    private Thread receiverThread;


    private Thread sendFolder;

    private static boolean subClientHandlerTransferFileCreated = false;

    public void setValue()
    {
        try {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Địa chỉ: "+ipAddress);
            tfYourID.setText(ipAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    private void clearViewTransfer() {
        Platform.runLater(() -> {
            vBoxSend.getChildren().clear();
            vBoxDownload.getChildren().clear();
        });
    }
    @FXML
    void handleClickConnect(MouseEvent event) {
        String partnerID = tfPartnerID.getText();
        if(partnerID != null)
        {
            try {
                socket = new Socket(partnerID,9090);
                outputStream = new DataOutputStream(socket.getOutputStream());


                if (senderThread != null && senderThread.isAlive()) {
                    senderThread.interrupt();
                }
                if (receiverThread != null && receiverThread.isAlive()) {
                    receiverThread.interrupt();
                }
                if (senderThread != null && senderThread.isAlive()) {
                    senderThread.interrupt();
                }
                senderThread = new Thread(() -> {
                    btnOpenFile.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            new SendFile(socket,btnOpenFile,taYourFile,vBoxSend);
                        }
                    });
                });

                receiverThread = new Thread(() -> {
                    new ReceiveFile(socket,taYourPartner,btnFastDownload,vBoxDownload,vBoxSend);
                });

                receiverThread.start();
                senderThread.start();


                sendFolder = new Thread(() ->{
                    btnOpenFolder.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            new SendFolder(socket,btnOpenFolder,taYourFile,vBoxSend);
                        }
                    });
                });


                sendFolder.start();
                clearViewTransfer();

            }
            catch (IOException e)
            {
                e.printStackTrace();
                showErrorAlert("Error", "Địa chỉ IP không chính xác. Vui lòng nhập lại!", e);
            }


        }
    }

    private void showErrorAlert(String title, String header, Exception exception) {
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
    @FXML
    void handleClickCloseConnect(MouseEvent event) throws IOException {
        if (socket != null && !socket.isClosed()) {

            clearViewTransfer();
            outputStream.close();
            socket.close();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new Sub_ClientHandlerFile(this,tfPartnerID,taYourPartner,btnFastDownload,btnOpenFile,taYourFile,btnOpenFolder,vBoxDownload,vBoxSend,btnCloseConnect).start();
        if (!subClientHandlerTransferFileCreated) {
            new Sub_ClientHandlerFile(this,tfPartnerID,taYourPartner,btnFastDownload,btnOpenFile,taYourFile,btnOpenFolder,vBoxDownload,vBoxSend,btnCloseConnect).start();
            subClientHandlerTransferFileCreated = true;
        }
    }
}



