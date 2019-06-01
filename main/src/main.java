import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class main {
    public static void main(String args[]) throws IOException, SAXException, ParserConfigurationException {
        Reliability_cal rc=new Reliability_cal();
        rc.cal();
        File file=new File("C:\\Users\\PC Khafan\\Desktop\\MC-Thermal\\test.xml");
        dag_Reader dr=new dag_Reader(file);
        dr.readXML();


    }
}
