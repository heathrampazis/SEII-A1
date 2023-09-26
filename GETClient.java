import java.io.IOException;
import java.io.PrintWriter;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.LinkedList;

public class GETClient {

    // Lamport clock for managing time stamps
    public static LamportClock lamportClock = new LamportClock(0);

    // Send GET request to the Aggregation Server
    private static void sendRequest(Socket socket) throws IOException {
        
        // Send the GET Request to get the XML response
        PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);
        outputWriter.println("GET /atom.xml HTTP/1.1");

        // Display the current timestamp and increment it
        System.out.println("Timestamp at: " + lamportClock.currentTimeStamp);
        lamportClock.currentTimeStamp++;
    }

    // Handles the Aggregation Server's response to the GET request
    private static void handleResponse(Socket socket) throws IOException, ClassNotFoundException{
        
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        XMLPacket responsePacket = (XMLPacket) inputStream.readObject();
        LinkedList<String> xmlResponse = responsePacket.xmlContent;
        
        // increment the time stamp based on the response packet
        lamportClock.increment(responsePacket.timeStamp);

        // output the XML response to the GET Client
        outputResponse(xmlResponse);
    }

    // Output the recieved XML response to the client
    private static void outputResponse(LinkedList<String> xmlResponse) throws IOException {
        XMLFactory xmlFactory = new XMLFactory();
        for (int i = 0; i < xmlResponse.size(); i++) {
            xmlFactory.printXML(i, xmlResponse.get(i));
        }
    }

    // Handle exceptions by printing error messages
    private static void handleException(Exception e) throws IOException {
        System.err.println("Client exception: " + e.toString());
        e.printStackTrace();
    }

    // Close the GET client's resources
    private static void closeClient(ObjectInputStream inputStream, PrintWriter outputWriter, Socket socket) throws IOException {
        if (inputStream != null) inputStream.close();
        if (outputWriter != null) outputWriter.close();
        if (socket != null) socket.close();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, NullPointerException {
        PrintWriter outputWriter = null;
        ObjectInputStream inputStream = null;
        
        // Command line arguments to obtain server information
        String[] components = args[0].split(":");
        String hostName = components[0];
        int port = Integer.parseInt(components[1]);

        // Create a new socket to connect to the server
        Socket socket = new Socket(hostName, port);
        
        // Print server connection information
        System.out.println("Connected at: " + socket.getInetAddress().getHostAddress() + " (address) " + socket.getInetAddress().getHostName() + " (host name)");
        
        try {
            // Send the GET Request and handle the server's response
            sendRequest(socket);
            handleResponse(socket);
        }
        catch( Exception e ) {
            handleException(e);
        }
        finally {
            closeClient(inputStream, outputWriter, socket);
        }
    }
}
