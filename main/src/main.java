import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class main {
    public static void main(String args[]) throws IOException, SAXException, ParserConfigurationException {
        double n=3;
        int deadline=900;
        int n_core=4;
        McDAG dag;

        //Dag XML Name
        String xml_name="1";
        //TSP File name
        String tsp_name="TSP";
        //Reliability File Name
        String rel_name="rel";

        double landa0=0.000001;
        int d=3;

        int n_overrun=2;
        int n_fault=2;
        int n_DAGs=10;

        //number of cores that can work with max freq in same time
        int max_freq_cores=1;


        //double v[]={0.912,0.9125,0.95,0.987,1.025,1.065,1.1,1.13,1.16,1.212,1.26};
        double v[]={1.023,1.062,1.115,1.3};
        //Possible Frequencies
        int freq[]={1200,1400,1600,2000};
        //Benchmarks Name
        String benchmark[]={"Basicmath", "Bitcount","Dijkstra","FFT","JPEG", "Patricia","Qsort","Sha","Stringsearch","Susan"};
        int benchmark_time[]={156,25,33,160,28,87,25,13,8,20};

        for (int i = 1; i <=n_DAGs ; i++) {
            xml_name=i+"";
            System.out.println("------------> ::: DAG "+xml_name+" Start ::: <----------");
            File file=new File("DAGs\\"+xml_name+".xml");
            dag_Reader dr=new dag_Reader(file);
            dag=dr.getDag();
            benchmark_mapping benchmark_mapping=new benchmark_mapping(dag,benchmark,benchmark_time);
            benchmark_mapping.mapping();
            benchmark_mapping.cal_LPL();
            deadline=benchmark_mapping.cal_deadline(n);
            benchmark_mapping.debug();

            //dr.readXML();

            //deadline=700;
            System.out.println("Deadline= "+deadline);
            //   --->>>>  PROPOSED METHOD
            System.out.println("------------> Proposed Method <----------");
            ProposedMethod proposedMethod=new ProposedMethod(landa0,d,v,freq,tsp_name,dag,n_core,deadline,rel_name,benchmark,
                    benchmark_time,max_freq_cores,n_overrun,n_fault,n,xml_name);
            try {
                proposedMethod.start();
            } catch (Exception e) {
                System.out.println("[ PROPOSED METHOD ] Infeasible!   "+ xml_name);
                e.printStackTrace();
            }
            System.out.println("------------> Classic NMR <----------");
            try {
                ClassicNMR NMR=new ClassicNMR(dag,n_core,deadline,benchmark,benchmark_time,n,n_overrun,xml_name);
            } catch (Exception e) {
                System.out.println("[ CLASSIC NMR ] Infeasible!   "+ xml_name);
                e.printStackTrace();
            }
            System.out.println("------------> Medina 2017 Method <----------");
            try {
                Medina medina=new Medina(dag,n_core,deadline,benchmark,benchmark_time,n_overrun,xml_name);
            } catch (Exception e) {
                System.out.println("[ MEDINA 2017 ] Infeasible!   "+ xml_name);
                e.printStackTrace();
            }
            System.out.println("------------> ::: DAG "+xml_name+" END ::: <----------");
        }


    }

}


