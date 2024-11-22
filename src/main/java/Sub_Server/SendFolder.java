package Sub_Server;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SendFolder {
    @FXML
    private Button btnOpenFolder;
    @FXML
    private TextArea taYourFile;
    @FXML
    private VBox vBoxSend;

    private Socket socket;
    private DataOutputStream outputStream;

    public SendFolder(Socket socket, Button btnOpenFolder, TextArea taYourFile, VBox vBoxSend) {
        try {
            this.socket = socket;
            this.btnOpenFolder = btnOpenFolder;
            this.taYourFile = taYourFile;
            this.vBoxSend = vBoxSend;
            this.outputStream = new DataOutputStream(socket.getOutputStream());


            DirectoryChooser directoryChooser = new DirectoryChooser();//Hiển thị hộp thoại để chọn folder
            directoryChooser.setTitle("Chọn thư mục muốn gửi");

            File selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null) {
                System.out.println("Thư mục đã được chọn: " + selectedDirectory.getAbsolutePath());

                String sourceFolder = selectedDirectory.getAbsolutePath();

                String zipFileName = selectedDirectory.getName() + ".zip";

                File zipFile = zipFolder(sourceFolder, zipFileName); //tạo một file zip

                if (zipFile != null) {
                    System.out.println("Thư mục đã được nén thành tệp ZIP: " + zipFile.getAbsolutePath());
                    String nameFolder = selectedDirectory.getName() + ".zip";
                    byte[] nameBytes = nameFolder.getBytes("UTF-8");
                    outputStream.writeInt(nameBytes.length);
                    outputStream.write(nameBytes);
                    outputStream.writeUTF("endofname");
                    outputStream.writeLong(zipFile.length());
                    try (FileInputStream zipInputStream = new FileInputStream(zipFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            outputStream.flush();
                        }
                    }
                    System.out.println("Dữ liệu đã được gửi thành công.");
                    Platform.runLater(() -> {
//                        taYourFile.appendText(selectedDirectory.getName() + "\n");
                        addLabelSend(selectedDirectory.getName(),vBoxSend);
                    });
                } else {
                    System.err.println("Không thể nén thư mục thành tệp ZIP.");
                }

            } else {
                System.out.println("Không có thư mục nào được chọn.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   public static File zipFolder(String sourceFolder, String zipFileName) {
        try {
            // Tạo tệp ZIP đầu ra
            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            // Gọi đệ quy để nén tất cả các tệp và thư mục trong thư mục nguồn
            addFolderToZip(sourceFolder, sourceFolder, zos);

            // Đóng tệp ZIP
            zos.close();
            fos.close();

            return new File(zipFileName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static void addFolderToZip(String folderPath, String sourceFolder, ZipOutputStream zos) throws IOException {
        File folder = new File(folderPath); // Tạo đối tượng File đại diện cho thư mục cần nén

        for (String fileName : folder.list()) {
            String filePath = folderPath + File.separator + fileName;

            if (new File(filePath).isDirectory()) {
                // Nếu là thư mục, gọi đệ quy để xử lý thư mục con
                addFolderToZip(filePath, sourceFolder, zos);
            } else {
                // Nếu là tệp-> Tạo đường dẫn tương đối của tệp so với thư mục nguồn
                String relativePath = new File(filePath).getPath().substring(new File(sourceFolder).getPath().length() + 1);
                ZipEntry ze = new ZipEntry(relativePath); // Tạo một mục nhập ZIP với đường dẫn tương đối
                zos.putNextEntry(ze); // Thêm mục nhập vào tệp ZIP

                // Ghi dữ liệu của tệp vào tệp ZIP
                FileInputStream fis = new FileInputStream(filePath); // Mở tệp để đọc
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                fis.close(); // Đóng luồng đọc
                zos.closeEntry(); // Đóng mục nhập ZIP
            }
        }

    }
    public static void addLabelSend(String fileName, VBox vBox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.BASELINE_LEFT);
        hBox.setPadding(new Insets(0, 5, 5, 10)); // Thiết lập khoảng cách bên trong

        Text text = new Text(fileName);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-color:rgb(239,242,255);" + "-fx-background-color: rgb(255,255,255);" + "-fx-border-color: black;" + " -fx-border-width: 1px;" + "-fx-border-radius: 5px;" + "-fx-cursor:pointer;");
        textFlow.setPadding(new Insets(2, 5, 2, 5)); // Thêm khoảng cách bên trong TextFlow
        hBox.getChildren().add(textFlow);

        Platform.runLater(() -> {
            vBox.getChildren().add(hBox);
        });
    }



}

