
import java.net.Socket;
import java.net.ConnectException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.util.Scanner;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;

class ContentServer {
    public static int lamportClock = 0;
    private static int port = 0;
    public static int count = 0;
    public static String hostName = "";
    public static String ID = "";
    public static String inputFile = "";
    public static Socket socket = null;

    public static void operateServer(Scanner input, String hostName, int port) throws IOException, ClassNotFoundException, InterruptedException {
        System.out.println("Starting Content Server, timestamp: " + lamportClock);
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        
        Socket socket = new Socket(hostName, port);
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            
            PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);
            
            //ois = new ObjectInputStream(socket.getInputStream()); -----> Moved below

            int length = 0;
            XMLFactory xmlFactory = new XMLFactoryImplementation();
            String xmlOut = xmlFactory.buildXML(inputFile, ID); // Read and return data from file
            if (xmlOut != null) {
                length = xmlOut.length();
            }
            
            System.out.println("PUT /atom.xml HTTP/1.1");
            System.out.println("User-Agent: " + ID );
            System.out.println("Content Type: application/atom+xml");
            System.out.println("Content Length: " + length);
            
            outputWriter.println("PUT /atom.xml HTTP/1.1");
            outputWriter.println("User-Agent: " + ID);
            outputWriter.println("Content Type: application/atom+xml");
            outputWriter.println("Content Length: " + length);
            lamportClock++;

            ois = new ObjectInputStream(socket.getInputStream());
            
            // May need to be removed
            Packet packet = new Packet(xmlOut, lamportClock);
            oos.writeObject(packet);

            Packet responsePacket = (Packet) ois.readObject();            
            int responseTimeStamp  = responsePacket.timeStamp;
            String response = responsePacket.xmlString;
            lamportClock =  Math.max(lamportClock, responseTimeStamp);
            lamportClock++;
            System.out.println("Response: " + response + "\r\nTimestamp" + lamportClock);

            if (response.equalsIgnoreCase("204 - No Content")) {
                System.out.println("Request not stored.");
            } else if ((response.equalsIgnoreCase("201 - HTTP Created"))  || (response.equalsIgnoreCase("200 - Success"))) {
                
                String temp = null;
                while (true) {
                    if (input.hasNextLine()) {
                        temp = input.nextLine();
                    }
                    if (temp != null) {
                        if(!temp.equalsIgnoreCase("exit")) {
                            outputWriter.println("1");
                            Thread.sleep(2000);
                        } else {
                            break;
                        }
                    }
                }
            }
            ois.close();
            oos.close();
        }
        catch (ConnectException e) {
            System.out.println("Connection error, retrying...");
            Thread.sleep(10000);
            count++;
            if (count < 3) {
                operateServer(input, hostName, port);
            }
        } 
        catch( Exception e ) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
        finally {
            socket.close();
        }
    }

    public static void main (String args[]) throws IOException, ClassNotFoundException, InterruptedException {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter ID and Input File: ");
        ID = input.nextLine();
        inputFile = input.nextLine();
        System.out.println("Enter the Client Name and Port Number: ");
        hostName = input.nextLine();
        hostName = hostName.replace("https://", "");
        String[] nameComponents = hostName.split(":");
        hostName = nameComponents[0];
        port = Integer.parseInt(nameComponents[1]);

        operateServer(input, hostName, port);    
    }
}
