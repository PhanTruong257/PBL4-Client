package Client;




import Sub_Server .Sub_server;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;


public class StartController implements Initializable {
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private Sub_server sub_server = null;
    private RemoteDesktop controller = null ;

    private Node content;
    private FXMLLoader loader;
    private Node remoteContent;

    private FXMLLoader remoteLoader = new FXMLLoader(getClass().getResource("RemoteDesktop.fxml"));

    public StackPane contentArea;

    public void  setClientSocket(Socket clientSocket, DataInputStream in, DataOutputStream out, Sub_server sub_server) {
        this.sub_server = sub_server;
        this.clientSocket = clientSocket;
        this.in = in;
        this.out = out;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            remoteContent = remoteLoader.load();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void exit(MouseEvent mouseEvent) {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }


    public void RemotePage(MouseEvent mouseEvent) {
        loader = remoteLoader;
        content = remoteContent;
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);

        if (controller == null) controller = loader.getController();

        String pwd = randomNumber();
        controller.setValue(pwd);
        controller.setSocketClient(clientSocket, out, in, sub_server);
    }








    public String randomNumber() {
        Random random = new Random();
        int randomNumber = 100000 + random.nextInt(900000); // Generates a number between 100000 and 999999


        return String.valueOf(randomNumber);
    }




    public void HomePage(MouseEvent mouseEvent) {


        loader = new FXMLLoader(getClass().getResource("StartWindow.fxml"));
        content = null;
        try {
            content = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        contentArea.getChildren().setAll(content);


    }
}

