import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;

public class CustomFile implements Serializable {
    private String fileName;
    private String fileType;
    private long fileSize;
    private String modifiedDate;
    private String ipAddress;
    private int portNumber;

    public CustomFile(String fileName, String fileType, long fileSize,String modifiedDate, String ipAddress, int portNumber) {
        this.setFileName(fileName);
        this.setFileType(fileType);
        this.setModifiedDate(modifiedDate);
        this.fileSize = fileSize;
        this.setIpAddress(ipAddress);
        this.setPortNumber(portNumber);
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj )
            return true;
        if(obj instanceof CustomFile) {
            CustomFile f = (CustomFile) obj;
            if(this.fileName.equals(f.fileName) && this.fileType.equals(f.fileType) && this.fileSize == f.fileSize
                    && this.modifiedDate.equals(f.modifiedDate) && this.ipAddress.equals(f.ipAddress)
                    && this.portNumber == f.portNumber)
                    return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String res = "<" + fileType + ", " + fileSize + ", " + modifiedDate + ", " + ipAddress + ", " + portNumber + ">";
        return res;
    }
}
