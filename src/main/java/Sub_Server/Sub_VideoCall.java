package Sub_Server;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class Sub_VideoCall extends Thread {
    @FXML
    public ImageView imgMe;
    @FXML
    public ImageView imgPa;
    @FXML
    private Button btnConnect;

    Socket videoSocketSend;
    Socket videoSocketReceive;
    Socket audioSocketSend;
    Socket audioSocketReceive;
    private Webcam webcam;
    private TargetDataLine microphone;
    private SourceDataLine speakers;  // Để phát âm thanh


    private DataOutputStream videoOutputStream;
    private DataOutputStream audioOutputStream;
    private DataInputStream videoInputStream;
    private InputStream audioInputStream;


    private ServerSocket serverVideoSocketSend;
    private ServerSocket serverVideoSocketReceive;
    private ServerSocket serverAudioSocketSend;
    private ServerSocket serverAudioSocketReceive;


    public Sub_VideoCall(ImageView imgMe, ImageView imgPa, Button btnConnect) {
        this.imgMe = imgMe;
        this.imgPa = imgPa;
        this.btnConnect = btnConnect;
    }

    @Override
    public void run() {
        VideoCall();
    }

    public void VideoCall() {
        System.out.println("server VideoCall");
        try {
            serverVideoSocketSend = new ServerSocket(3435);
            serverVideoSocketReceive = new ServerSocket(3436);
            serverAudioSocketSend = new ServerSocket(3437);
            serverAudioSocketReceive = new ServerSocket(3438);

            while (true) {

                videoSocketSend = serverVideoSocketReceive.accept();
                videoSocketReceive = serverVideoSocketSend.accept();
                audioSocketSend = serverAudioSocketReceive.accept();
                audioSocketReceive = serverAudioSocketSend.accept();
                webcam = Webcam.getDefault();

                if (webcam != null) {
                    if (webcam.isOpen()) {
                        webcam.close(); // Đóng webcam trước khi cấu hình
                    }
                    webcam.setViewSize(new Dimension(640, 480)); // Đặt độ phân giải
                    webcam.open(); // Mở lại webcam
                }
                videoInputStream = new DataInputStream(videoSocketReceive.getInputStream());

                videoOutputStream = new DataOutputStream(videoSocketSend.getOutputStream());
                audioOutputStream = new DataOutputStream(audioSocketSend.getOutputStream());
                audioInputStream = new DataInputStream(audioSocketReceive.getInputStream());

                startVideoStream();
                startAudioStream();
                receiveAudioFromClient();
                receiveVideoFromClient();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void startVideoStream() throws IOException {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendVideoFrame();  // Gửi video frame mỗi 20ms
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 20); // 20ms giữa các frame
    }

    private void sendVideoFrame() throws IOException {
        try {
            // Lấy hình ảnh từ webcam
            BufferedImage bi = webcam.getImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baos);
            byte[] bytes = baos.toByteArray();

            // Hiển thị video cục bộ
            Platform.runLater(() -> imgMe.setImage(SwingFXUtils.toFXImage(bi, null)));

            // Gửi dữ liệu qua socket video
            if (videoOutputStream != null) {
                videoOutputStream.writeInt(bytes.length);
                videoOutputStream.write(bytes);
                videoOutputStream.flush();
            } else {
                System.out.println("videoOutputStream is null, unable to send frame.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void receiveVideoFromClient() {
        new Thread(() -> {
            while (true) {
                try {
                    if (videoSocketReceive.isConnected()) {
                        System.out.println("Attempting to receive video frame from client...");

                        // Đọc độ dài của frame
                        int length = videoInputStream.readInt();

                        // Kiểm tra nếu length hợp lệ (phải > 0)
                        if (length <= 0) {
                            System.out.println("Invalid video frame length: " + length);
                            continue;  // Tiếp tục vòng lặp nếu length không hợp lệ
                        }

                        byte[] bytes = new byte[length];
                        videoInputStream.readFully(bytes, 0, length);

                        // Chuyển byte[] thành BufferedImage và cập nhật giao diện
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                        BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
                        Image image = SwingFXUtils.toFXImage(bufferedImage, null);

                        // Cập nhật hình ảnh đối tác trên giao diện
                        Platform.runLater(() -> imgPa.setImage(image));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        // Chờ 2 giây trước khi thử lại khi gặp lỗi
                        Thread.sleep(2000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    System.out.println("Reattempting to receive video...");
                }
            }
        }).start();
    }


    private void startAudioStream() {
        new Thread(() -> {
            try {
                setupMicrophone();
                byte[] audioBuffer = new byte[4096];
                while (audioSocketSend.isConnected() && !audioSocketSend.isClosed()) {
                    try {
                        int bytesRead = microphone.read(audioBuffer, 0, audioBuffer.length);
                        if (bytesRead > 0) {
                            audioOutputStream.write(audioBuffer, 0, bytesRead);
                            audioOutputStream.flush();

                            // Kiểm tra phản hồi từ server (nếu cần yêu cầu gửi lại)
                            if (audioInputStream.available() > 0) {
                                byte[] response = new byte[16];
                                int responseLength = audioInputStream.read(response);
                                String command = new String(response, 0, responseLength).trim();

                                if ("RETRANSMIT_AUDIO".equals(command)) {
                                    System.out.println("Retransmission requested by server...");
                                    audioOutputStream.write(audioBuffer, 0, bytesRead); // Gửi lại dữ liệu
                                    audioOutputStream.flush();
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Error sending audio. Retrying...");
                        e.printStackTrace();
                    }
                }
            } catch (LineUnavailableException e) {
                System.out.println("Microphone setup failed.");
                e.printStackTrace();
            }
        }).start();
    }

    public void receiveAudioFromClient() {
        new Thread(() -> {
            try {
                setupSpeakers();
                byte[] audioBuffer = new byte[4096];
                while (audioSocketReceive.isConnected() && !audioSocketReceive.isClosed()) {
                    try {
                        int bytesRead = audioInputStream.read(audioBuffer);
                        if (bytesRead == -1) break;

                        // Điều chỉnh bytesRead để chia hết cho frameSize
                        int frameSize = 4; // 16-bit stereo
                        bytesRead -= bytesRead % frameSize;

                        if (bytesRead > 0) {
                            speakers.write(audioBuffer, 0, bytesRead); // Phát âm thanh
                        } else {
                            System.out.println("Invalid audio data received. Requesting retransmission...");
                            audioOutputStream.write("RETRANSMIT_AUDIO".getBytes());
                            audioOutputStream.flush();
                        }
                    } catch (IOException e) {
                        System.out.println("Error receiving audio. Retrying...");
                        e.printStackTrace();
                    }
                }
            } catch (LineUnavailableException e) {
                System.out.println("Speaker setup failed.");
                e.printStackTrace();
            }
        }).start();
    }

    private void setupSpeakers() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(44100.0f, 16, 2, true, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Speakers not supported.");
        }
        speakers = (SourceDataLine) AudioSystem.getLine(info);
        speakers.open(format);
        speakers.start();
    }


    private void setupMicrophone() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(44100.0f, 16, 2, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Microphone not supported.");
        }
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();
    }


}
