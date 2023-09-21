
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import javax.xml.parsers.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.util.*;
import java.io.*;
import java.text.*;

public class XMLFactoryImplementation implements XMLFactory {
    public XMLFactoryImplementation() throws IOException {}

    static Document document = null;
    static String newString = null;
    static String ID = null;

    public String buildXML(String inputFile, String contentID) throws IOException, TransformerFactoryConfigurationError {
        try {        
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            document = builder.newDocument();
            Reader reader = new FileReader(inputFile);
            Scanner scanner = new Scanner(reader);
            String ID = contentID;
        
            Element root = document.createElement("feed");
            document.appendChild(root);
            Element firstEntry = document.createElement("entry");

            Attr attribute = document.createAttribute("id");
            attribute.setValue(ID);
            root.setAttributeNode(attribute);
            
            Attr language = document.createAttribute("xml:lang");
            language.setValue("en-US");
            root.setAttributeNode(language);
            
            Attr xmlns = document.createAttribute("xmlns");
            xmlns.setValue("http://www.w3.org/2005/Atom");
            root.setAttributeNode(xmlns);
            
            root.appendChild(firstEntry);
      
            Element tempEntry = firstEntry;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                
                if (line != "entry") {
                    String[] temp = line.split(":");
                    Element entryElement = document.createElement(temp[0]);
                    Text child = document.createTextNode(temp[1]);
                    entryElement.appendChild(child);
                    tempEntry.appendChild(entryElement);
                }
                else {
                    Element entry = document.createElement("entry");
                    root.appendChild(entry);
                    tempEntry = entry;
                }
            }
            TransformerFactory tsf = TransformerFactory.newInstance();
            Transformer ts = tsf.newTransformer();
            
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);

            DOMSource source = new DOMSource(document);

            ts.transform(source,result);
            newString = sw.toString();
            ts.transform(source, result);      
            scanner.close();      
        }
        catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            return newString;
        }
    }
    
    public void printXML(int number, String xmlString) throws IOException {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            byte[] bytes = xmlString.getBytes();
            ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(bytes);
            document = builder.parse(byteArrayIS);
            document.getDocumentElement().normalize();
            NodeList elements = document.getElementsByTagName("*");
            String nodeName = null;

            int counter = 0;
            while (elements.getLength() > counter) {
                Node node = elements.item(counter);
                Element element = (Element) node;
                nodeName = element.getNodeName();
                if (nodeName.equalsIgnoreCase("entry")) {
                    System.out.println("\r\n" + nodeName);
                } else if (nodeName.equalsIgnoreCase("feed")) {
                    System.out.println("\r\n" + nodeName + " " + number + " source: Content Server " + 
                        element.getAttribute("id") + "\r\n----------------------------------------");
                } else {
                    System.out.println(nodeName + ": " + element.getTextContent());
                }
                counter++;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Document stringParser(String xmlString) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            document = builder.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            return document;
        }
    }
}
