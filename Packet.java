import java.io.Serializable;

public class Packet implements Serializable {
    public int timeStamp;
    public String xmlString;

    public Packet (String xmlStringIn, int timeStampIn) {
        this.timeStamp = timeStampIn;
        this.xmlString = xmlStringIn;
    }
}
