
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.transform.stream.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;

public class AggregationServer extends Thread {
    public static int lamportClock = 0;
    public static LinkedList<String> feed = new LinkedList<>();
    
    private static void storeInFile(String file, LinkedList<String> data) {
        try {
            ObjectOutputStream fileObjOut = new ObjectOutputStream(new FileOutputStream(file));
            fileObjOut.writeObject(data);
        }
        catch (Exception e){
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private static class GETHandler implements Runnable {
        private final Socket handlerSocket;

        public GETHandler(Socket socket) {
            this.handlerSocket = socket;
        }

        public void run() {
            ObjectInputStream ois = null;
            PrintWriter writer = null;
            ObjectOutputStream oos = null;
            BufferedReader reader = null;

            try {
                System.out.println("GET request received, timestamp: " + lamportClock);
                ois = new ObjectInputStream(handlerSocket.getInputStream());            
                Packet packet = (Packet) ois.readObject();
                lamportClock = Math.max(lamportClock, packet.timeStamp);
                lamportClock++;
                oos = new ObjectOutputStream(handlerSocket.getOutputStream());
                oos.writeObject(new XMLPacket(feed, lamportClock));
            }
            catch (Exception e) {
                System.err.println("Server exception: " + e.toString());
                e.printStackTrace();
            }
            finally {
                try {
                    if (reader != null) {
                        reader.close();
                        handlerSocket.close();
                    }
                    if (writer != null) {
                        writer.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class PUTHandler implements Runnable {
        private BufferedReader reader = null;
        private Packet packet = null;
        private ObjectOutputStream oos = null;
        private ObjectInputStream ois = null;
        private final Socket socket;
        private boolean newCheck  = false;
    
        public PUTHandler(Socket socket) {
            this.socket = socket;
        }
    
        public void run() {
            try {
                System.out.println("PUT REQUEST");
                ois = new ObjectInputStream(socket.getInputStream());

                packet = (Packet) ois.readObject();
                
                lamportClock = Math.max(lamportClock, packet.timeStamp);
                lamportClock++;
                System.out.println("Current timestamp: " + lamportClock);
                
                String string = packet.xmlString;
                if (string == null) {
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    lamportClock++;
                    Packet resPacket = new Packet("204 - No Content", lamportClock);
                    oos.writeObject(resPacket);
                } else {
                    XMLFactory xmlFactory = new XMLFactoryImplementation();                
                    Node node = xmlFactory.stringParser(string).getElementsByTagName("feed").item(0);
                    Element element = (Element) node;
                    String ID = element.getAttribute("id");
                    String tag = "id = \"" + ID;
                    tag = tag + "\"";
                    int i = 0;
                    while (i < feed.size()) {
                        if (feed.get(i).contains(tag)) {
                            feed.set(i,string);
                            newCheck = true;
                        }
                        i++;
                    }
                    if (newCheck == false) {
                        if(feed.size() > 20) {
                            feed.removeFirst();
                            feed.add(string);
                            storeInFile("oldFeed.txt", feed);
                        } else {
                            feed.add(string);
                            storeInFile("oldFeed.txt", feed);
                        }
                    }
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    lamportClock++;
                    Packet resPacket = new Packet("200 - Success", lamportClock);
    
                    if (newCheck == false) {
                        resPacket.xmlString = "201 - HTTP Created";
                    }

                    oos.writeObject(resPacket);

                    InputStream inputStream = socket.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    reader = new BufferedReader(inputStreamReader);
    
                    while (true) {
                        Thread.sleep(12000);
                        String input = reader.readLine();
                        
                        if (input == null) {
                            int j = 0;
                            while(j < feed.size() ) {
                                if (feed.get(j).contains(tag)) {
                                    feed.remove(feed.get(j));
                                    System.out.println("Feed " + j + ". Removed.");
                                    storeInFile("oldFeed.txt", feed);
                                    j = j-1;
                                }
                                j++;
                            }
                            break;
                        }
                    }
                }
            }
            catch (Exception e) {
                System.err.println("Server exception: " + e.toString());
                e.printStackTrace();
            }
            finally {
                try {
                    socket.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ErrorHandler implements Runnable {
        private final Socket socket;
        private ObjectOutputStream oos = null;

        public ErrorHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                Packet packet = new Packet("400 - Bad Request", lamportClock);
                oos.writeObject(packet);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket server = null;
        int port = 4567;

        try {
            System.out.println("Custom port? (y/n): ");
            Scanner scanner = new Scanner(System.in);
            
            if (scanner.nextLine().equals("y")) {
                System.out.println("Enter port number: ");
                String temp = scanner.nextLine();
                port = Integer.parseInt(temp);
            }

            LinkedList<String> temp = new LinkedList<>();
            File file = new File("oldFeed.txt");
            if (file.length() > 0) {
                FileInputStream fileInputStream = new FileInputStream("oldFeed.txt");
                ObjectInputStream ois = new ObjectInputStream(fileInputStream);
                LinkedList<String> stored = (LinkedList<String>) ois.readObject();
                temp = stored;
            }
            feed = temp;
            server = new ServerSocket(port);
            server.setReuseAddress(true);
            
            System.out.println("Server starting with file...\r\n" + feed.size() + " previous entries.");

            while (true) {

                // Aceept incoming connection
                Socket socket = server.accept();

                System.out.println("HERE 1 - Accepted a connection");

                System.out.println("Connected at: " + socket.getInetAddress().getHostAddress() + " (address) " + socket.getInetAddress().getHostName() + " (host name)");
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //ERROR SEEMS TO BE HERE WHERE THE BUFFERED READER READS INPUT AS MENTIONED IN README
                String request = "";

                int k = 0;
                while (k < 3) {
                    System.out.println("Point 1");
                    request = request + "\n\r";
                    System.out.println("Point 2");
                    System.out.println(in.readLine());
                    request = request + in.readLine(); // ENCOUNTERED ERROR POINT HERE
                    System.out.println("Point 3");
                    k++; 
                    System.out.println("Point 4");
                }
                
                //System.out.println(request);
                System.out.println("HERE 2 - Successfully reads the client request");

                // Process client request
                if (request.contains("GET /atom.xml HTTP/1.1")) {
                    System.out.println("HERE 3 - Process the incoming GET request");
                    System.out.println("Creating new GETHandler thread...");
                    GETHandler gHandler = new GETHandler(socket);
                    System.out.println("HERE 4 - Created the GET Handler");
                    new Thread(gHandler).start();
                    System.out.println("HERE 5 - Send the response back to the client.");
                } else if (request.contains("PUT /atom.xml HTTP/1.1")) {
                    System.out.println("HERE 3 - Process the incoming PUT request");
                    System.out.println("Creating new PUTHandler thread...");
                    PUTHandler pHandler = new PUTHandler(socket);
                    System.out.println("HERE 4 - Created the PUT Handler");
                    new Thread(pHandler).start();
                    System.out.println("HERE 5 - Send the response back to the server.");
                } else {
                    System.out.println("Problem with input, using ErrorHandler...");
                    ErrorHandler errorHandler = new ErrorHandler(socket);
                    new Thread(errorHandler).start();
                }
            }
        }
        catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
        finally {
            if (server == null) {
                server.close();
                return;
            } else {
                for (String string:feed) {
                    System.out.println(string);
                }
                server.close();
            }
        }
    }
}
