package Sub_Server;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ReceivingEvents extends Thread {
    Socket socket = null;
    Robot robot = null;
    boolean continueLoop = true;

    public ReceivingEvents(Socket socket, Robot robot) {
        this.socket = socket;
        this.robot = robot;
        start();
    }

    public void run() {
        Scanner scanner = null;
        try {
            scanner = new Scanner(socket.getInputStream());
            while (continueLoop) {
                int command = scanner.nextInt();
                switch (command) {
                    case -1:
                        System.out.println("Press");
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); // Primary mouse button press
                        break;
                    case -2:
                        System.out.println("Release");
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); // Primary mouse button release
                        break;
                    case -3:
                        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK); // Secondary mouse button press
                        break;
                    case -4:
                        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK); // Secondary mouse button release
                        break;
                    case -5:
                        int pressKey = scanner.nextInt();
                        System.out.println("Press " + pressKey);
                        robot.keyPress(pressKey);
                        break;
                    case -6:
                        int releaseKey = scanner.nextInt();
                        System.out.println("Release " + releaseKey);
                        robot.keyRelease(releaseKey);
                        break;
                    case -7:
                        int x = scanner.nextInt();
                        int y = scanner.nextInt();
                        robot.mouseMove(x, y);
                        break;
                    case -8:
                        // Handle mouse drag
                        int xDrag = scanner.nextInt();
                        int yDrag = scanner.nextInt();
                        robot.mouseMove(xDrag, yDrag);
                        break;
                    case -9:
                        int yDelta = scanner.nextInt();
                        robot.mouseWheel(-yDelta);
                        break;
                    default:
                        // Handle unrecognized command
                        System.err.println("Unrecognized command: " + command);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}