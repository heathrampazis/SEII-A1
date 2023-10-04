import java.io.*;
import java.net.*;
import org.w3c.dom.*;

//
//  PUT HANDLER
//  Description : Handles PUT requests from the Content Server
//
public class PUTHandler extends AggregationServer {

    // Lamport clock for managing time steps
    public static LamportClock lamportClock = new LamportClock(0);
    
    // Flag to check if a new feed entry is being added
    public boolean isNewFeed = false;

    // Variables associated with PUT Handler
    private BufferedReader bufferedReader = null;
    private Packet packet = null; 
    private ObjectOutputStream outputStream = null;
    private ObjectInputStream inputStream = null;
    private final Socket socket;

    // Constructor for the PUT Handler
    public PUTHandler(Socket socket) {
        this.socket = socket;
    }

    // Handle incoming PUT request from the client
    private void handleRequest() throws IOException, ClassNotFoundException, InterruptedException {
        
        // Initialise input stream to read incoming packet from client
        inputStream = new ObjectInputStream(socket.getInputStream());
        packet = (Packet) inputStream.readObject();

// NEVER GETS TO HERE //

        // Increment the Lamport Clock timestamp based on the request and display it
        lamportClock.increment(packet.timeStamp);
        System.out.println("Current timestamp: " + lamportClock.currentTimeStamp);

        // Handle XML string recieved from the client
        if (packet.xmlString != null) {
            handleXMLString(packet.xmlString);
        } else {
            handleNullXMLString();
        }
    }

    // Handle the case where the XML string contains content
    private void handleXMLString(String string) throws IOException, InterruptedException {

        // Create a new XML Factory
        XMLFactory xmlFactory = new XMLFactory();
        
        // Parse the XML string and extract the id attribute
        Node node = xmlFactory.stringParser(string).getElementsByTagName("feed").item(0);
        Element element = (Element) node;
        String ID = element.getAttribute("id");
        String tag = "id = \"" + ID + "\"";

        // Flag to check if feed is new
        boolean isNewFeed = false;

        // Handle the case where the feed entry is new
        if (feed.stream().noneMatch(entry -> entry.contains(tag))) {
            feed.add(string);
            isNewFeed = true;
        }

        // Handle the case where the feed entry is not new
        if (!isNewFeed) {
            if (feed.size() > 20) {
                feed.removeFirst();
            }
            feed.add(string);
            storeInFile("oldFeed.txt", feed);
        }

        // Create the response to be sent
        handleResponse(isNewFeed, tag);
    }

    // Handle the case where the XML string contains no content
    private void handleNullXMLString() throws IOException {
        
        // Increment the Lamport Clock and create a "204 - No Content" response
        lamportClock.currentTimeStamp++;
        Packet responsePacket = new Packet("204 - No Content", lamportClock.currentTimeStamp);

        // Send the response to the client
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(responsePacket);
    }

    // Handle the response to be sent back to the client
    private void handleResponse(boolean isNewFeed, String tag) throws IOException, InterruptedException {

        outputStream = new ObjectOutputStream(socket.getOutputStream());
        lamportClock.currentTimeStamp++;
        
        // Create the response packet
        Packet responsePacket = new Packet("200 - Success", lamportClock.currentTimeStamp);

        // Update the response string if feed already exists
        if (!isNewFeed) responsePacket.xmlString = "201 - HTTP Created";

        // Send the response packet to client
        outputStream.writeObject(responsePacket);

        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        bufferedReader = new BufferedReader(inputStreamReader);

        // Remove the feed entry with the matching 'id'
        while (true) {
            Thread.sleep(12000);
            if (bufferedReader.readLine() == null) {
                feed.removeIf(entry -> entry.contains(tag));
                storeInFile("oldFeed.txt", feed);
                break;
            }
        }
    }

    //
    // RUN METHOD
    //
    public void run() {
        try {
            System.out.println("HELLo");
            handleRequest();
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}