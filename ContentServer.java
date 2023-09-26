import java.net.Socket;
import java.net.ConnectException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Scanner;

class ContentServer {

    // Lamport clock for managing time stamps
    public static LamportClock lamportClock = new LamportClock(0);
    
    // Variables for server
    public static String hostName = "";
    public static String ID = "";
    public static String inputFile = "";
    private static int port = 0;

    // Operate the content server, handlign connections an doperations
    public static void operateServer(Scanner input, String hostName, int port) throws IOException, ClassNotFoundException, InterruptedException {
        Socket socket = null;
        try {
            // connect to server
            socket = new Socket(hostName, port);
            // send data, recieve and process responses 
            performOperation(input, socket);
        } catch (ConnectException e) {
            // handle connection exceptions and operate the content server again
            handleConnectionException(e);
            operateServer(input, hostName, port);
        } catch (Exception e) {
            handleException(e);
        } finally {
            closeServer(socket);
        }
    }

    // Method that performs the main server operations
    private static void performOperation(Scanner input, Socket socket) throws IOException, ClassNotFoundException, InterruptedException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);

        XMLFactory xmlFactory = new XMLFactory();
        String xmlOutput = xmlFactory.buildXML(inputFile, ID);

        System.out.println("Starting Content Server, timestamp: " + lamportClock.currentTimeStamp);
        int contentLength = (xmlOutput != null) ? xmlOutput.length() : 0;

        // Send HTTP PUT request with necessary headers
        outputWriter.println("PUT /atom.xml HTTP/1.1\r\nUser-Agent: " + ID + "\r\nContent Type: application/atom+xml\r\nContent Length: " + contentLength);
        lamportClock.currentTimeStamp++;

        // Sends packet to the server
        Packet packet = new Packet(xmlOutput, lamportClock.currentTimeStamp);
        outputStream.writeObject(packet);

        // Reads packet from the server
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        Packet responsePacket = (Packet) inputStream.readObject();

        // Process the response from the server
        processResponse(responsePacket, inputStream, outputWriter);
    }

    // Processes the packet recieved from the server
    private static void processResponse(Packet responsePacket, ObjectInputStream inputStream, PrintWriter outputWriter) throws IOException, InterruptedException {
        String response = responsePacket.xmlString;
        lamportClock.increment(responsePacket.timeStamp);

        System.out.println("Response: " + response + "\r\nTimestamp" + lamportClock.currentTimeStamp);

        if (response.equalsIgnoreCase("201 - HTTP Created") || response.equalsIgnoreCase("200 - Success")) {
            handleResponse200(inputStream, outputWriter);
        } else if (response.equalsIgnoreCase("204 - No Content")) {
            System.out.println("Request not stored.");
        }

        inputStream.close();
    }

    // Handles a response with HTTP status 200
    private static void handleResponse200(ObjectInputStream inputStream, PrintWriter outputWriter) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        String line = null;
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
        Scanner input = new Scanner(System.in);

        // Command line arguments to obtain server information
        ID = args[0];
        inputFile = args[1];
        String[] components = args[2].split(":");
        hostName = components[0];
        port = Integer.parseInt(components[1]);

        // Start the Content Server operation
        operateServer(input, hostName, port);
    }
}