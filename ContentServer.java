import java.net.Socket;
import java.net.ConnectException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Scanner;

//
//  CONTENT SERVER
//  Description : Makes HTTP PUT requests to upload new versions of feeds to the Aggregation Server.
//
class ContentServer {

    // Lamport clock for managing time stamps
    public static LamportClock lamportClock = new LamportClock(0);
    
    // Variables for server obtained through user input
    public static String hostName = "";
    public static String contentServerID = "";
    public static String inputFile = "";
    private static int port = 0;

    // Processes the packet recieved from the server
    private static void processResponse(Packet responsePacket, ObjectInputStream inputStream, PrintWriter outputWriter) throws IOException, InterruptedException {
        String response = responsePacket.xmlString;
        lamportClock.increment(responsePacket.timeStamp);

        System.out.println("Response: " + response + "\r\nTimestamp" + lamportClock.currentTimeStamp);

            if (response.equalsIgnoreCase("201 - HTTP Created") || response.equalsIgnoreCase("200 - Success")) {
                Scanner scanner = new Scanner(System.in);
                String line = null;

                // Handle a response with a HTTP 200 or 201 status code
                while (true) {
                    if (scanner.hasNextLine()) {
                        line = scanner.nextLine();
                    }
                    if (line != null) {
                        if (!line.equalsIgnoreCase("exit")) {
                            outputWriter.println("1");
                            Thread.sleep(2000);
                        } else {
                            break;
                        }
                    }
                }
                scanner.close();
        } else if (response.equalsIgnoreCase("204 - No Content")) {

            // Handle a response with no content
            System.out.println("Request not stored.");
        }

        inputStream.close();
    }

    // handles connection exceptions by retrying after a delay
    private static void handleConnectionException(ConnectException e) throws InterruptedException {
        System.out.println("Connection error, retrying...");
        Thread.sleep(10000);
    }

    // Handles general exceptions and prints error information
    private static void handleException(Exception e) {
        System.err.println("Server exception: " + e.toString());
        e.printStackTrace();
    }

    // Closes the server socket if it's not null
    private static void closeServer(Socket socket) throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    public static void main(String args[]) throws IOException, ClassNotFoundException, InterruptedException {

        // Command line arguments to obtain server information
        contentServerID = args[0];
        inputFile = args[1];
        String[] components = args[2].split(":");
        hostName = components[0];
        port = Integer.parseInt(components[1]);

        // Variable to determine if a connection was successful
        boolean connected = false;

        // Loop to keep running until a connection is successful 
        while (!connected) {
            Socket socket = null;

            // Start the Content Server operation
            try {
                // connect to server
                socket = new Socket(hostName, port);
                // send data, recieve and process responses 
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);
                XMLFactory xmlFactory = new XMLFactory();
    
                // Assemble the XML feed from the plain text file
                String xmlOutput = xmlFactory.buildXML(inputFile, contentServerID);
    
                System.out.println("Starting Content Server, timestamp: " + lamportClock.currentTimeStamp);
                int contentLength = (xmlOutput != null) ? xmlOutput.length() : 0;
    
                // Send HTTP PUT request with necessary headers
                outputWriter.println("PUT /atom.xml HTTP/1.1");
                outputWriter.println("User-Agent: " + contentServerID);
                outputWriter.println("Content Type: application/atom+xml");
                outputWriter.println("Content Length: " + contentLength);
                lamportClock.currentTimeStamp++;
    
                // Sends packet to the server
                Packet packet = new Packet(xmlOutput, lamportClock.currentTimeStamp);
                outputStream.writeObject(packet);
    
                // Reads packet from the server
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                Packet responsePacket = (Packet) inputStream.readObject();
                
                // process response from server
                processResponse(responsePacket, inputStream, outputWriter);

                // connection recieved
                connected = true;
            } catch (ConnectException e) {
                // handle connection exceptions and operate the content server again
                handleConnectionException(e);
            } catch (Exception e) {
                handleException(e);
            } finally {
                closeServer(socket);
            }
        }
        
    }
}