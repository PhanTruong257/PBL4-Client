package Sub_Server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.scene.text.Text;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SendFile {


    private Socket socket;
    private DataOutputStream outputStream;

    @FXML
    private TextArea taYourFile;
    @FXML
    private Button btnOpenFile;
    @FXML
    private VBox vBoxSend;

    public SendFile(Socket socket, Button btnOpenFile, TextArea taYourFile, VBox vBoxSend){
        try {
            this.socket = socket;
            this.outputStream= new DataOutputStream(socket.getOutputStream());
            this.btnOpenFile = btnOpenFile;
            this.taYourFile = taYourFile;
            this.vBoxSend = vBoxSend;

            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(btnOpenFile.getScene().getWindow());

            if (selectedFile != null) {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                byte [] buffer = new byte[4096];
                int bytesRead;

                //Gửi tên file
                String nameFile= selectedFile.getName();
                byte[] nameBytes= nameFile.getBytes("UTF-8");
                outputStream.writeInt(nameBytes.length);//gửi độ dài
                outputStream.write(nameBytes);//gửi tên file
                outputStream.writeUTF("endofname");//đánh dấu kết thúc phần tên file
                outputStream.writeLong(selectedFile.length());//gửi kích thước của file

                //Đọc và gửi nội dụng file
                while((bytesRead = fileInputStream.read(buffer))!= -1){
                    outputStream.write(buffer,0,bytesRead); //gửi từng khối dữ liệu
                    outputStream.flush();
                }
                System.out.println("Send file successfully");
                fileInputStream.close(); //đóng file

                //Cập nhật
                Platform.runLater(()->{
                    addLabelSend(selectedFile.getName(),vBoxSend);
                });




            }
        }catch(Exception e){
            e.printStackTrace();
        }



    }
    public static void addLabelSend(String fileName, VBox vBox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.BASELINE_LEFT);
        hBox.setPadding(new Insets(0, 5, 5, 10));
        Text text = new Text(fileName);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle(
                "-fx-color: rgb(239,242,255);" + // Màu chữ
                        "-fx-background-color: rgb(240,240,240);" + // Màu nền xám nhẹ
                        "-fx-border-color: rgb(210,210,210);" + // Viền màu xám nhạt
                        "-fx-border-width: 1px;" + // Độ dày viền
                        "-fx-border-radius: 8px;" + // Bo góc viền
                        "-fx-background-radius: 8px;" + // Bo góc nền
                        "-fx-cursor: pointer;" // Thay đổi con trỏ chuột khi hover
        );
        textFlow.setPadding(new Insets(2, 5, 2, 5));
        hBox.getChildren().add(textFlow);
        Platform.runLater(() -> {
            vBox.getChildren().add(hBox);

        });
    }
}
