import java.io.Serializable;
import java.util.LinkedList;

public class XMLPacket implements Serializable {
    public LinkedList<String> xmlContent;
    public int timeStamp;
    
    public XMLPacket (LinkedList<String> xmlContent, int timeStamp) {
        this.xmlContent = xmlContent;
        this.timeStamp = timeStamp;
    }
}
