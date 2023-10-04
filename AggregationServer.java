import java.io.*;
import java.net.*;
import java.util.*;

//
//  AGGREGATION SERVER
//  Description : Responds to requests for feeds and accepts feed updates from clients. It is responsible for aggregating ATOM feeds.
//
public class AggregationServer extends Thread {

    // Lamport clock to manage time steps in the Aggregation Server
    public static LamportClock lamportClock = new LamportClock(0);
    
    // Linked List to store feed entries
    public static LinkedList<String> feed = new LinkedList<>();

    // Stores data in a file
    public static void storeInFile(String file, LinkedList<String> data) throws IOException {
        ObjectOutputStream outputStream = null;
        try {
            // write the data to the file
            outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(data);
        }
        catch (Exception e){
            handleException(e);
        }
        finally {
            if (outputStream != null) outputStream.close();
        }
    }

    // Initialises the feed and load data from the previous file if it exists
    private static void initialiseFeed() throws IOException, ClassNotFoundException {
        LinkedList<String> tempFeed = new LinkedList<>();
        File file = new File("oldFeed.txt");

        // if the file exists, read from it
        if (file.length() > 0) {
            FileInputStream fileInputStream = new FileInputStream("oldFeed.txt");
            ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);
            LinkedList<String> storedFeed = (LinkedList<String>) inputStream.readObject();
            tempFeed = storedFeed;
            inputStream.close();
        }
        // update the feed
        feed = tempFeed;
    }

    // Reads request from a client's Buffered Reader
    private static String readRequest(BufferedReader reader) throws IOException {
        StringBuilder request = new StringBuilder();        

        // read lines from the request
        String line = reader.readLine();
        while (!line.isEmpty() && line != null) {
            request.append("\n\r");
            request.append(line);
            line = reader.readLine();
        }
        return request.toString();
    }
    
    // Processes incoming requests and creates handler requests corresponding to them
    private static void processRequest(Socket socket, String request) {

        // process GET request
        if (request.contains("GET /atom.xml HTTP/1.1")) {
            System.out.println("Creating new GETHandler thread...");
            GETHandler getHandler = new GETHandler(socket);
            new Thread(getHandler).start();
        } 
        // process PUT request
        else if (request.contains("PUT /atom.xml HTTP/1.1")) {
            System.out.println("Creating new PUTHandler thread...");
            PUTHandler putHandler = new PUTHandler(socket);
            new Thread(putHandler).start();
        }
        // handle request error
        else {
            System.out.println("Problem with input, using ErrorHandler...");
            ErrorHandler errorHandler = new ErrorHandler(socket);
            new Thread(errorHandler).start();
        }
    }

    // Handles exceptions
    private static void handleException(Exception e) {
        System.err.println("Server exception: " + e.toString());
        e.printStackTrace();
    }
    
    // Closes the server
    private static void closeServer(ServerSocket server) {
        if (server != null) {
            try {
                // close the server
                server.close();
            } catch (IOException e) {
                System.err.println("Error while closing the server socket: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    //
    //  MAIN FUNCTION OF THE AGGREGATION SERVER
    //  CODE IS EXECUTED HERE
    //
    public static void main(String[] args) throws IOException {
        ServerSocket server = null;

        // Port which the server listens for incoming connections
        int port = 4567;

        try {

            // initialise the feed and load existing feeds if they exist
            initialiseFeed();

            // create a server socket
            server = new ServerSocket(port);
            server.setReuseAddress(true);
            
            // server start up message that displays the number of previous entries
            System.out.println("Server starting with file...\r\n" + feed.size() + " previous entries.");

            // listen to incoming connections
            while (true) {
                // accept connection
                Socket socket = server.accept();

                System.out.println("Connected at: " + socket.getInetAddress().getHostAddress() + " (address) " + socket.getInetAddress().getHostName() + " (host name)");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                // Reads incoming request
                String request = readRequest(bufferedReader);
                
                // processes incoming request
                processRequest(socket, request);
            }
        }
        // handle exceptions
        catch (Exception e) {
            handleException(e);
        }
        // close the server
        finally {
            closeServer(server);
        }
    }
}
