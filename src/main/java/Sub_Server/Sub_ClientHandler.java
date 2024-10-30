package Sub_Server;



import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Sub_ClientHandler extends Thread       {
    private Socket clientSocket;
    private Robot robot;
    private String option = "";

    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    private String clientWidthCm = Double.toString(dim.getWidth());
    private String clientHeightCm = Double.toString(dim.getHeight());


    public Sub_ClientHandler(Socket clientSocket, Robot robot,String option){
        this.clientSocket = clientSocket;
        this.robot = robot;
        this.option = option;
    }
    @Override
    public void run() {
        getControl(option);
    }


    public void getControl(String control){
        switch ((control)){
            case "1":
                Remote_Desktop();
        }
    }
    public void Remote_Desktop(){
        DataOutputStream out = null;
        try {

            out = new DataOutputStream(clientSocket.getOutputStream());
            out.writeUTF(clientWidthCm);
            out.flush();
            out.writeUTF(clientHeightCm);
            out.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            Thread thread = new Thread(() -> {
                new SendScreen(robot,clientSocket);
            });
            thread.start();
            new ReceivingEvents(clientSocket,robot);


        } catch (Exception e) {
            try {
                System.out.println("Đóng kết nối");
                clientSocket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            e.printStackTrace();
        }
    }
}
