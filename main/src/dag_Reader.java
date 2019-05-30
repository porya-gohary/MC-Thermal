
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class dag_Reader {
    File xml;
    public dag_Reader(File input) throws ParserConfigurationException, IOException, SAXException {
        xml=input;
        readXML();
    }
    public void readXML() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        // 	Root element
        Document doc = dBuilder.parse(xml);
        doc.getDocumentElement().normalize();

        // Extract number of cores
        NodeList cList = doc.getElementsByTagName("cores");
        Element c = (Element) cList.item(0);
        //setNbCores(Integer.parseInt(c.getAttribute("number")));

        //HELLO
    }

}
