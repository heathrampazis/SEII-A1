import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.util.*;
import java.io.*;
import java.io.IOException;
import org.w3c.dom.Document;

//
//  XML FACTORY
//  Description : Creates a document from an ATOM feed
//
public class XMLFactory {

    // Stores the content within the XML document
    static Document document = null;
    
    static String xmlDocumentAsString = null;

    // Create the XML Factory and initialise the document
    public XMLFactory() {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            document = builder.newDocument();
        } catch (ParserConfigurationException e) {
            handleException(e);
        }
    }

    // Method to build and return an XML String
    public String buildXML(String inputFile, String contentServerID) throws TransformerFactoryConfigurationError, Exception {
        try {        
            createDocument(contentServerID);
            populateDocument(inputFile);
            transformDocumentToString();
        }
        catch (IOException e) {
            handleException(e);
        }
        return xmlDocumentAsString;
    }
        
    // Method to print the XML document 
    public void printXML(int number, String xmlString) throws IOException {
        try {
            parseXMLString(xmlString);
            normalizeDocument();
            printDocument(number);
        }
        catch (Exception e) {
            handleException(e);
        }
    }
    
    // Method to parse an XML string and return the document
    public Document stringParser(String xmlString) {
        try {
            parseXMLString(xmlString);
            return document;
        }
        catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    // Creates an XML document with a root element
    private void createDocument(String contentServerID) throws ParserConfigurationException {

        // Create the root element of the document
        Element root = document.createElement("feed");
        document.appendChild(root);

        // Set the attributes of the root element
        root.setAttribute("id", contentServerID);
        root.setAttribute("xml:lang", "en-US");
        root.setAttribute("xmlns", "http://www.w3.org/2005/Atom");
    } 

    // Populates the XML Document using the input file
    private void populateDocument(String inputFile) throws IOException {
        // Reads input file
        try (Reader reader = new FileReader(inputFile);
            Scanner scanner = new Scanner(reader)) {

            Element currentEntry = document.getDocumentElement();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (!line.equals("entry")) {
                    // Split each line into key-value pairs and create XML elements
                    String[] temp = line.split(":");
                    Element entryElement = document.createElement(temp[0]);
                    Text child = document.createTextNode(temp[1]);
                    entryElement.appendChild(child);
                    currentEntry.appendChild(entryElement);
                } else {
                    // Start a new "entry" element
                    Element entry = document.createElement("entry");
                    document.getDocumentElement().appendChild(entry);
                    currentEntry = entry;
                }
            }
        }
    }

    // Parses the XML String and loads it into the document
    private void parseXMLString(String xmlString) throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        document = builder.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
    }

    // Normalises the XML Document
    private void normalizeDocument() {
        document.getDocumentElement().normalize();
    }

    // Tranforms the XML Document to a String
    private void transformDocumentToString() throws Exception {

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter stringWriter = new StringWriter();
        StreamResult result = new StreamResult(stringWriter);

        // Tranform document to string
        transformer.transform(new DOMSource(document), result);
        xmlDocumentAsString = stringWriter.toString();
    }

    // Prints the XML document elements
    private void printDocument(int number) {
        
        NodeList elements = document.getElementsByTagName("*");

        // Loop to output to elements in the XML document
        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String nodeName = element.getNodeName();
                if (nodeName.equalsIgnoreCase("entry")) {
                    System.out.println("\r\n" + nodeName);
                } else if (nodeName.equalsIgnoreCase("feed")) {
                    System.out.println("\r\n" + nodeName + " " + number + " source: Content Server " + element.getAttribute("id") + "\r\n----------------------------------------");
                } else {
                    System.out.println(nodeName + ": " + element.getTextContent());
                }
            }
        }

    }

    // Handles exceptions by printing error message
    private void handleException(Exception e) {
        System.out.println("Error: " + e.getMessage());
        e.printStackTrace();
    }

}
