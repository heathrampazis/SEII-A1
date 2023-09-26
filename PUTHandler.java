import java.io.*;
import java.net.*;
import org.w3c.dom.*;

public class PUTHandler extends AggregationServer {

    // Lamport clock for managing time steps
    public static LamportClock lamportClock = new LamportClock(0);
    
    private BufferedReader reader = null; // Buffered Reader for reading input from the client
    private Packet packet = null; // Packet to hold data from the client
    private ObjectOutputStream outputStream = null; // Output stream for sending data to the client
    private ObjectInputStream inputStream = null; // Input stream for reading data from the client
    private final Socket socket; // Socket associated with client conneciton
    public boolean isNewFeed = false; // Flag to check if a new feed entry is being added

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
        if (packet.xmlString == null) {
            handleNullString();
        } else {
            handleNonNullString(packet.xmlString);
        }
    }

    // Handle the case where the XML string contains no content
    private void handleNullString() throws IOException {
        // Increment the Lamport Clock and create a "204 - No Content" response
        lamportClock.currentTimeStamp++;
        Packet responsePacket = new Packet("204 - No Content", lamportClock.currentTimeStamp);

        // Send the response to the client
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(responsePacket);
    }

    // Handle the case where the XML string contains content
    private void handleNonNullString(String string) throws IOException, InterruptedException {
        XMLFactory xmlFactory = new XMLFactory();
        
        // Parse the XML string and extract the id attribute
        Node node = xmlFactory.stringParser(string).getElementsByTagName("feed").item(0);
        Element element = (Element) node;
        String ID = element.getAttribute("id");
        String tag = "id = \"" + ID + "\"";

        boolean isNewFeed = false;

// REFACTORED THIS
        if (feed.stream().noneMatch(entry -> entry.contains(tag))) {
            feed.add(string);
            isNewFeed = true;
        }

// REFACTORED THIS
        if (!isNewFeed) {
            if (feed.size() > 20) {
                feed.removeFirst();
            }
            feed.add(string);
            storeInFile("oldFeed.txt", feed);
        }
        handleResponse(isNewFeed, tag);
    }

    // Handle the response to be sent back to the client
    private void handleResponse(boolean newCheck, String tag) throws IOException, InterruptedException {
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        lamportClock.currentTimeStamp++;
        
        Packet responsePacket = new Packet("200 - Success", lamportClock.currentTimeStamp);

        if (!newCheck) responsePacket.xmlString = "201 - HTTP Created";

        outputStream.writeObject(responsePacket);

        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        reader = new BufferedReader(inputStreamReader);

        while (true) {
            Thread.sleep(12000);
    
// REFACTORED THIS
            if (reader.readLine() == null) {
                // Remove the feed entry with the matching 'id'
                feed.removeIf(entry -> entry.contains(tag));
                storeInFile("oldFeed.txt", feed);
                break;
            }
        }
    }

    // The main run method for the PUT handler thread
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