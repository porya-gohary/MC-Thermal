import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class main {
    public static void main(String args[]) throws IOException, SAXException, ParserConfigurationException {
        File rel= new File("rel.txt");
        double n=5;
        int deadline=200;
        int n_core=4;
        double v[]={0.912,0.9125,0.95,0.987,1.025,1.065,1.1,1.13,1.16,1.212,1.26};

       // rc.cal();


        File file=new File("test.xml");
        dag_Reader dr=new dag_Reader(file);
        //dr.readXML();
        Reliability_cal rc=new Reliability_cal(n,0.000001,3,1.26,0.912,rel,v,dr.getDag());

        File tsp_input=new File("TSP.txt");
        TSP tsp=new TSP(tsp_input,dr.getNbCores(),v,dr.getDag());


        System.out.println("------------> RELIABILITY AND VOLTAGE OF EACH TASKS <----------");
        for (Vertex a : dr.getDag().getVertices()) {
            double WCET= (a.getWcet(0) > a.getWcet(1)) ? a.getWcet(0) : a.getWcet(1);
            rc.setT_min(WCET);
            rc.setV_name(a.getName());
            rc.cal();
        }
        System.out.println("------------> MAX ACTIVE CORE FOR EACH TASKS <----------");
        tsp.read_TSP_file();
        tsp.cal_TSP_core();
        for (Vertex a : dr.getDag().getVertices()) {
            System.out.println(a.getName()+"    "+a.getTSP_Active());
            System.out.println("_____________________");
        }

        System.out.println("------------> SAFE START TIME <----------");
        Safe_Start_Time ss=new Safe_Start_Time(dr.getDag().getVertices().stream().toArray(Vertex[]::new),dr.getDag(),n,deadline,n_core);
        ss.sort_vertex();
        ss.scheduling();
    }

}


