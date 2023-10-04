import java.io.Serializable;

//
//  PACKET
//  Description : 
//
public class Packet implements Serializable {
    
    // Variables associated with the Packet
    public int timeStamp;
    public String xmlString;

    // Constructor for the Packet
    public Packet (String xmlString, int timeStamp) {
        this.xmlString = xmlString;
        this.timeStamp = timeStamp;
    }
}
