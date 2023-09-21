
import java.io.IOException;
import org.w3c.dom.Document;

public interface XMLFactory {
    String buildXML(String contentID, String inputFile) throws IOException;

    void printXML(int number, String xmlString) throws IOException;

    Document stringParser(String xmlString);
}
