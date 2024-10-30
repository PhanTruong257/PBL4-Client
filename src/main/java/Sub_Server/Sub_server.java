package Sub_Server;


import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Sub_server extends Thread{
    private Robot robot = null;

    private ServerSocket serverSocket   ;
    private final int port = 2508;
    public Sub_server(){
        System.out.println("Sub_server is running...");
        start();
    }
    @Override
    public void run(){
        try {
            GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gDev = gEnv.getDefaultScreenDevice();
            this.robot = new Robot();
            this.serverSocket = new ServerSocket(port);
            Sub_ClientHandler remoteServer = null;
            System.out.println("the day");
            while(true){
                if (remoteServer == null || !remoteServer.isAlive()){
                    System.out.println("vao day khong");
                    Socket clientSocket = serverSocket.accept() ;

                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    String option = in.readUTF()    ;
                    remoteServer = new Sub_ClientHandler(clientSocket,robot,option);

                    remoteServer.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
