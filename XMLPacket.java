import java.io.Serializable;
import java.util.LinkedList;

public class XMLPacket implements Serializable {
    public int timeStamp;
    public LinkedList<String> xmlContent;

    public XMLPacket (LinkedList<String> xmlContentIn, int timeStampIn) {
        this.timeStamp = timeStampIn;
        this.xmlContent = xmlContentIn;
    }
}
