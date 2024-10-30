package Client;

public class MessageHandler {
    private String receiver;
    private String sender;
    private String parts[];

    private String status;

    private String data;
    public MessageHandler(String message) {
        parts = message.split(",");
        sender = parts[0];
        receiver = parts[1];
        status = parts[2];
        data = parts[3];
    }

    public String getStatus() {
        return status;
    }

    public String getData() {
        return data;
    }
    public String getReceiver() {return sender;}

}
