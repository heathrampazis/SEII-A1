import java.io.Serializable;

public class Packet implements Serializable {
    public int timeStamp;
    public String xmlString;

    public Packet (String xmlString, int timeStamp) {
        this.xmlString = xmlString;
        this.timeStamp = timeStamp;
    }
}
