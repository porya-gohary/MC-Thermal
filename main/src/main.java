import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class main {
    public static void main(String args[]) throws IOException, SAXException, ParserConfigurationException {
        File f= new File("C:\\Users\\PC Khafan\\Desktop\\MC-Thermal\\rel.txt");
        double v[]={0.912,0.9125,0.95,0.987,1.025,1.065,1.1,1.13,1.16,1.212,1.26};
        Reliability_cal rc=new Reliability_cal(3,0.000001,3,1.26,0.912,274,f,v);
       // rc.cal();


        File file=new File("C:\\Users\\PC Khafan\\Desktop\\MC-Thermal\\test.xml");
        dag_Reader dr=new dag_Reader(file);
        dr.readXML();


    }
}
