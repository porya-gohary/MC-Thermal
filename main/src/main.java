import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class main {
    public static void main(String args[]) throws IOException, SAXException, ParserConfigurationException {
        File rel= new File("rel.txt");
        double n=5;
        int deadline=900;
        int n_core=4;
        McDAG dag;
        //double v[]={0.912,0.9125,0.95,0.987,1.025,1.065,1.1,1.13,1.16,1.212,1.26};
        double v[]={1.023,1.062,1.115,1.3};
        int freq[]={1200,1400,1600,2000};
        //number of cores that can work with max freq in same time
        int max_freq_cores=1;

        String benchmark[]={"Basicmath", "Bitcount","Dijkstra","FFT","JPEG", "Patricia","Qsort","Sha","Stringsearch","Susan"};
        int benchmark_time[]={157,26,34,162,29,88,26,14,9,21};


        File file=new File("test.xml");
        dag_Reader dr=new dag_Reader(file);
        dag=dr.getDag();
        benchmark_mapping benchmark_mapping=new benchmark_mapping(dag,benchmark,benchmark_time);
        benchmark_mapping.mapping();
        benchmark_mapping.cal_LPL();
        deadline=benchmark_mapping.cal_deadline(n);
       // benchmark_mapping.debug();

        //dr.readXML();
        Reliability_cal rc=new Reliability_cal(n,0.000001,3,v[v.length-1],v[0],rel,v,dag);

        File tsp_input=new File("TSP.txt");
        TSP tsp=new TSP(tsp_input,dr.getNbCores(),v,freq,dag);


       // ------------> RELIABILITY AND VOLTAGE OF EACH TASKS <----------
        for (Vertex a : dag.getVertices()) {
            double WCET= (a.getWcet(0) > a.getWcet(1)) ? a.getWcet(0) : a.getWcet(1);
            rc.setT_min(WCET);
            rc.setV_name(a.getName());
            rc.cal();
        }
        //------------> MAX ACTIVE CORE FOR EACH TASKS <----------
        tsp.read_TSP_file();
        tsp.cal_TSP_core();

        //------------> SAFE START TIME <----------
        Safe_Start_Time ss=new Safe_Start_Time(dag.getVertices().stream().toArray(Vertex[]::new).clone(),dag,n,deadline,n_core, v[v.length-1], freq[freq.length-1],max_freq_cores);
        ss.sort_vertex();
        ss.scheduling();
        ss.overrun();
        ss.inject_fault();
        ss.setSafeStartTime();

        for (Vertex a : dag.getVertices()) {
            a.debug();
        }

        //------------> Main Scheduling <----------
        System.out.println("------------> Main Scheduling <----------");
        mainScheduling mainScheduling=new mainScheduling(dag.getVertices().stream().toArray(Vertex[]::new).clone(),dag,n,deadline,n_core, v[v.length-1],freq[freq.length-1], max_freq_cores);
        mainScheduling.clean_sch();
        mainScheduling.sort_vertex();
        mainScheduling.mScheduling();
        mainScheduling.inject_fault(2);
        mainScheduling.overrun(2);


    }

}


