package Sub_Server;

public class FileData {
    private String fileName;
    private byte[] data;
    public FileData(String fileName,byte[] data){
        this.fileName= fileName;
        this.data= data;
    }
    public String getFileName(){
        return fileName;
    }
    public byte[] getData(){
        return data;
    }
}
