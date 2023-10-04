import java.io.Serializable;
import java.util.LinkedList;

//
//  XML PACKET
//  Description : 
//
public class XMLPacket implements Serializable {
    // Variables associated with the XML Packet
    public LinkedList<String> xmlContent;
    public int timeStamp;
    
    // Constructor for the XML Packet
    public XMLPacket (LinkedList<String> xmlContent, int timeStamp) {
        this.xmlContent = xmlContent;
        this.timeStamp = timeStamp;
    }
}
