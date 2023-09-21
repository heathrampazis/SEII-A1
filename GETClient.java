import java.net.*;
import java.io.*;
import java.util.Scanner;

public class GETClient {
    private static int lamportClock = 0;
    private static Socket socket;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String hostName = "";
        int port = 0;
        PrintWriter outputWriter = null;
        ObjectInputStream ois = null;

        Scanner input = new Scanner(System.in);
        System.out.println("Enter the Server Name and Port Number: ");
        hostName = input.nextLine();
        hostName = hostName.replace("https://", "");



        String[] nameComponents = hostName.split(":");
        hostName = nameComponents[0];
        port = Integer.parseInt(nameComponents[1]);

        try {
            socket = new Socket(hostName, port);

            System.out.println("HERE 1 - Successfully created a socket");

            // Send GET request to server
            outputWriter = new PrintWriter(socket.getOutputStream(), true);
            outputWriter.println("GET /atom.xml HTTP/1.1");
            lamportClock++; // Increment the Lamport clock after sending the request

            System.out.println("HERE 2 - Successfully sent GET request to server");

            // Recieve XML response and Lamport Timestamp from Server
            ois = new ObjectInputStream(socket.getInputStream());
            Packet responsePacket = (Packet) ois.readObject();
            
            System.out.println("HERE 3 - Successfully recieved XML response from server"); // IT NEVER GETS HERE
            
            String xmlResponse = responsePacket.xmlString;
            int responseTimeStamp = responsePacket.timeStamp;
            
            // Update Lamport Clock
            lamportClock = Math.max(lamportClock, responseTimeStamp) + 1;
            System.out.println("Received XML data with timestamp: " + lamportClock);
            
            // Process and print the XML response or handle it as per requirement
            System.out.println(xmlResponse);

        } catch (IOException e) {
            System.out.println("Error communicating with the server: " + e.getMessage());
        } finally {
            if (ois != null) ois.close();
            if (outputWriter != null) outputWriter.close();
            if (socket != null) socket.close();
        }
    }
}
