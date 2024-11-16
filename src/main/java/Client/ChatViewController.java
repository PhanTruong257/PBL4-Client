package Client;


import Sub_Server.Sub_ClientHandlerChat;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
public class ChatViewController implements Initializable {

    @FXML
    private Button btnConnect;
    @FXML
    private Button button_send;

    @FXML
    private Label labelPartnerID;

    @FXML
    private Label labelYourID;
    @FXML
    private ScrollPane sp_main;

    @FXML
    private TextField tfPartnerID;
    @FXML
    private TextField tfYourID;
    @FXML
    private TextField tf_message;

    @FXML
    private VBox vbox_messages;

    private Socket socket= null;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private Thread senderThread;
    private Thread receiverThread;
    private static boolean subClientHandlerChatCreated = false;

    @FXML
    private Button btnCloseConnect;

    public static void addLabelSend(String msgFromServer, VBox vBox){

       //Time
        LocalDateTime currentDateSendTime = LocalDateTime.now();
        DateTimeFormatter formatter= DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedDateTime= currentDateSendTime.format(formatter);
        HBox hBox2= new HBox();
        hBox2.setAlignment(Pos.CENTER_RIGHT);
        hBox2.setPadding(new Insets(-5,5,-5,10));

        Text text2= new Text("("+ formattedDateTime +")");
        TextFlow textFlow2 = new TextFlow(text2);
        textFlow2.setPadding(new Insets(5,5,5,5));
        hBox2.getChildren().add(textFlow2);

        //Text
        HBox hBox= new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPadding(new Insets(0,10,5,10));

        Text text= new Text(msgFromServer);
        TextFlow textFlow= new TextFlow(text);
        textFlow.setStyle("-fx-color:rgb(239,242,255);" + "-fx-background-color: rgb(15,125,242);" + "-fx-background-radius: 10px;");
        textFlow.setPadding(new Insets(5,10,5,10));
        hBox.getChildren().add(textFlow);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vBox.getChildren().add(hBox2);
                vBox.getChildren().add(hBox);
            }
        });

    }
    public static void addLabelReceive(String msgFromClient, VBox vBox){
        LocalDateTime currentDateTime= LocalDateTime.now();
        DateTimeFormatter formatter= DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedDateTime= currentDateTime.format(formatter);
        HBox hBox2= new HBox();
        hBox2.setAlignment(Pos.CENTER_LEFT);
        hBox2.setPadding(new Insets(-5,5,-5,10));
        Text text2 = new Text("( " + formattedDateTime + ")");
        TextFlow textFlow2= new TextFlow(text2);
        hBox2.getChildren().add(textFlow2);

        HBox hBox= new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox2.setPadding(new Insets(0,5,5,10));
        Text text= new Text(msgFromClient);
        TextFlow textFlow= new TextFlow(text);
        textFlow.setStyle("-fx-color:rgb(239,242,255);" + "-fx-background-color: rgb(15,125,242);" + "-fx-background-radius: 10px;");
        textFlow.setPadding(new Insets(5,10,5,10));
        hBox.getChildren().add(textFlow);

        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                vBox.getChildren().add(hBox2);
                vBox.getChildren().add(hBox);
            }
        });

    }
    public void setValue(){
        try{
            String ipAddress= InetAddress.getLocalHost().getHostAddress();
            System.out.println("Địa chỉ máy là:"+ ipAddress);
            tfYourID.setText(ipAddress);
        }catch(UnknownHostException e){
            e.printStackTrace();
        }
    }
    public void clearChatView(){
        Platform.runLater(()->{
            vbox_messages.getChildren().clear();
        });
    }

    @FXML
    void onClickConnect(MouseEvent event) throws IOException {
        //Địa chỉ IP của partner
        String partnerID = tfPartnerID.getText();
        try {
            socket = new Socket(partnerID, 9999);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            if (senderThread != null && senderThread.isAlive()) {
                senderThread.interrupt();
            }
            if (receiverThread != null && receiverThread.isAlive()) {
                senderThread.interrupt();
            }
            senderThread = new Thread(() -> {
                button_send.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        try{
                            String message;
                            message = tf_message.getText();
                            dataOutputStream.writeUTF(message);
                            //Thêm message lên VBox
                            addLabelSend(message,vbox_messages);
                            tf_message.setText("");
                            dataOutputStream.flush();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                });
                tf_message.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent)  {
                        try{
                            String message;
                            message = tf_message.getText();
                            dataOutputStream.writeUTF(message);
                            addLabelSend(message,vbox_messages);
                            tf_message.setText("");
                            dataOutputStream.flush();
                        }catch(IOException e){
                            e.printStackTrace();
                        }

                    }
                });
            });
            receiverThread = new Thread(()->{
               try {
                    String message;
                     while (true) {
                         message = dataInputStream.readUTF();
                         if(message.equals("Connect is closed by partner")){
                             dataOutputStream.close();
                             dataInputStream.close();
                             clearChatView();
                             showErrorAlert("Alert","Connect is closed by partner!");

                         }else{
                             addLabelReceive(message,vbox_messages);
                             System.out.println(message);

                         }
                     }
               }catch(IOException e){
                   e.printStackTrace();
               }
            });

         senderThread.start();
         receiverThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onClickCloseConnect(MouseEvent event) throws IOException{
        dataOutputStream.writeUTF("Connect is closed by partner");
        dataOutputStream.flush();
        clearChatView();
        dataOutputStream.close();
        dataInputStream.close();
        socket.close();
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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new Sub_ClientHandlerChat(this,vbox_messages,button_send,tf_message,btnConnect).start();
        System.out.println("Khởi tạo");

        if(!subClientHandlerChatCreated){
            System.out.println("Khởi tạo");
            new Sub_ClientHandlerChat(this, vbox_messages, button_send, tf_message,btnCloseConnect).start();
            subClientHandlerChatCreated = true;
        }

    }
}