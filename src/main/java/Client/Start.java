package Client;

import Sub_Server.Sub_server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Start extends Application {
    @Override
    public void start(Stage stage) throws IOException {


        String ip ="192.168.2.85" ;
        int port = 2507;
        int checkWait =   10;

        boolean connect = false;
        Socket client = null;
        DataInputStream in = null;
        DataOutputStream out = null;

        for(int i = 1 ; i <= checkWait; i++){
            try{
                client = new Socket(ip,port);
                System.out.println("Connected...");
                in = new DataInputStream(client.getInputStream());
                out = new DataOutputStream(client.getOutputStream());
                connect = true;
                break; // after connected, exit loop
            } catch (Exception e) {
                System.out.println(i+ " Connect failed");
                try{
                    Thread.sleep(1000); //after 1 second trying to connect
                }catch (InterruptedException ee){
                    ee.printStackTrace();
                }
            }
        }

        if(connect){
            out.writeUTF( InetAddress.getLocalHost().getHostAddress() );
            out.flush();

            FXMLLoader fxmlLoader = new FXMLLoader(Start.class.getResource("StartWindow.fxml"));
            Pane pane = fxmlLoader.load();
            Sub_server sub_server = new Sub_server();
            StartController controller = fxmlLoader.getController();
            controller.setClientSocket(client, in, out, sub_server);
            Scene scene = new Scene(pane);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
            stage.show();
        }


    }

    public static void main(String[] args) {
        launch();
    }
}