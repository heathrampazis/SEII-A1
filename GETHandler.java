import java.io.*;
import java.net.*;
import java.util.*;

//
//  GET HANDLER
//  Description : Handles GET requests from the GET Client
//
public class GETHandler implements Runnable {

    // Lamport clock for managing time steps
    public static LamportClock lamportClock = new LamportClock(0);
    
    // Linked List to store feed entries
    public static LinkedList<String> feed = new LinkedList<>();
    
    // Socket associated with the client's connection
    private final Socket socket;

    // Initialise a GET handler with a client's socket
    public GETHandler(Socket socket) {
        this.socket = socket;
    }

    // Handle incoming client GET request
    private void handleRequest() throws IOException {
        System.out.println("GET request received, timestamp: " + lamportClock.currentTimeStamp);
        try (
            ObjectInputStream inputStream =  new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        ) {     
            // Read incoming packet from GET Client    
            Packet packet = (Packet) inputStream.readObject();
            
            // Increment the current lamport clock based on the time stamp of the packet
            lamportClock.increment(packet.timeStamp);

            // Send the response packet with feed content and current timestamp
            outputStream.writeObject(new XMLPacket(feed, lamportClock.currentTimeStamp));
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Handles exception error messages
    private void handleException(Exception e) {
        System.err.println("Server exception: " + e.toString());
        e.printStackTrace();
    }

    // Closes the client socket once finished
    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    //  RUN METHOD
    //
    public void run() {
        try {
            handleRequest();
        }
        catch (Exception e) {
            handleException(e);
        }
        finally {
            closeSocket();
        }
    }

}