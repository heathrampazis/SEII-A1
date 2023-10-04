import java.io.*;
import java.net.*;

//
//  ERROR HANDLER
//  Description : Responsible to handling errors associated with the client socket
//
public class ErrorHandler extends AggregationServer {

    // Lamport clock for managing time stamps in the Error Handler
    public static LamportClock lamportClock = new LamportClock(0);
    
    // Socket associated with the client connection
    private final Socket socket;

    // Initialise the error handler with the client socket
    public ErrorHandler(Socket socket) {
        this.socket = socket;
    }

    //
    // RUN METHOD
    //
    public void run() {
        try {
            // Create a response packet with a 400 Bad Request message
            Packet packet = new Packet("400 - Bad Request", lamportClock.currentTimeStamp);
            
            // Create a new output stream for sending the error message back to the client
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            
            // Send the error message back to the client
            outputStream.writeObject(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}