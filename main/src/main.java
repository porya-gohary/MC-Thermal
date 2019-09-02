import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


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

        //Number Of Overrun
        int n_overrun=0;
        //Number Of Fault
        int n_fault=0;
        //Number of DAG
        int n_DAGs=70;

        //Scheduling Results:
        int PR_Sch;
        int NMR_Sch;
        int Med_Sch;

        //Power Results
        double Pro_power[]=new double[2];
        double[] NMR_power =new double[2];
        double Med_power[]=new double[2];

        //number of cores that can work with max freq in same time
        int max_freq_cores=1;


        //double v[]={0.912,0.9125,0.95,0.987,1.025,1.065,1.1,1.13,1.16,1.212,1.26};
        double v[]={1.023,1.062,1.115,1.3};
        //Possible Frequencies
        int freq[]={1200,1400,1600,2000};
        //Benchmarks Name
        String benchmark[]={"Basicmath", "Bitcount","Dijkstra","FFT","JPEG", "Patricia","Qsort","Sha","Stringsearch","Susan"};
        int benchmark_time[]={156,25,33,160,28,87,25,13,8,20};

        PR_Sch=n_DAGs;
        NMR_Sch=n_DAGs;
        Med_Sch=n_DAGs;

        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter("Summary.txt"));
        for (int i = 1; i <=n_DAGs ; i++) {
            xml_name=i+"";
            outputWriter.write(">>>>>>>>>> ::: DAG "+xml_name+" Start ::: <<<<<<<<<<"+"\n");
            System.out.println(">>>>>>>>>> ::: DAG "+xml_name+" Start ::: <<<<<<<<<<");
            File file=new File("DAGs\\"+xml_name+".xml");
            dag_Reader dr=new dag_Reader(file);
            dag=dr.getDag();
            benchmark_mapping benchmark_mapping=new benchmark_mapping(dag,benchmark,benchmark_time);
            benchmark_mapping.mapping();
            benchmark_mapping.cal_LPL();
            deadline=benchmark_mapping.cal_deadline(n);
            benchmark_mapping.debug();

            //dr.readXML();

            n_overrun= (int) (dag.getNodes_HI().size()*0.1);

            //deadline=700;
            System.out.println("Deadline= "+deadline);
            //   --->>>>  PROPOSED METHOD
            System.out.println("------------> Proposed Method <----------");
            outputWriter.write("------------> Proposed Method <----------"+"\n");
            ProposedMethod proposedMethod=new ProposedMethod(landa0,d,v,freq,tsp_name,dag,n_core,deadline,rel_name,benchmark,
                    benchmark_time,max_freq_cores,n_overrun,n_fault,n,xml_name);
            try {
                proposedMethod.start();
                outputWriter.write("Avg. Power= "+proposedMethod.mainScheduling.cpu.power_results()[0]+"\n");
                outputWriter.write("Peak Power= "+proposedMethod.mainScheduling.cpu.power_results()[1]+"\n");
                Pro_power[0]+=proposedMethod.mainScheduling.cpu.power_results()[0];
                Pro_power[1]+=proposedMethod.mainScheduling.cpu.power_results()[1];

            } catch (Exception e) {
                System.out.println("[ PROPOSED METHOD ] Infeasible!   "+ xml_name);
                outputWriter.write("[ PROPOSED METHOD ] Infeasible!   "+ xml_name+"\n");
                PR_Sch--;
                e.printStackTrace();
            }
            System.out.println("------------> Classic NMR <----------");
            outputWriter.write("------------> Classic NMR <----------"+"\n");
            try {
                ClassicNMR NMR=new ClassicNMR(dag,n_core,deadline,benchmark,benchmark_time,n,n_overrun,xml_name);
                outputWriter.write("Avg. Power= "+NMR.cpu.power_results()[0]+"\n");
                outputWriter.write("Peak Power= "+NMR.cpu.power_results()[1]+"\n");

                NMR_power[0]+=NMR.cpu.power_results()[0];
                NMR_power[1]+=NMR.cpu.power_results()[1];
            } catch (Exception e) {
                System.out.println("[ CLASSIC NMR ] Infeasible!   "+ xml_name);
                outputWriter.write("[ CLASSIC NMR ] Infeasible!   "+ xml_name+"\n");
                NMR_Sch--;
                e.printStackTrace();
            }
            System.out.println("------------> Medina 2017 Method <----------");
            outputWriter.write("------------> Medina 2017 Method <----------"+"\n");
            try {
                Medina medina=new Medina(dag,n_core,deadline,benchmark,benchmark_time,n_overrun,xml_name);
                outputWriter.write("Avg. Power= "+medina.cpu.power_results()[0]+"\n");
                outputWriter.write("Peak Power= "+medina.cpu.power_results()[1]+"\n");

                Med_power[0]+=medina.cpu.power_results()[0];
                Med_power[1]+=medina.cpu.power_results()[1];
            } catch (Exception e) {
                System.out.println("[ MEDINA 2017 ] Infeasible!   "+ xml_name);
                outputWriter.write("[ MEDINA 2017 ] Infeasible!   "+ xml_name+"\n");
                Med_Sch--;
                e.printStackTrace();
            }
            System.out.println("------------> ::: DAG "+xml_name+" END ::: <----------");
            outputWriter.write(">>>>>>>>>>>>> ::: DAG "+xml_name+" END ::: <<<<<<<<<<<<"+"\n\n");
        }
        outputWriter.write("\n");
        outputWriter.write(">>>>>>>>>>>>> SUMMARY OF ALL DAGs <<<<<<<<<<<<"+"\n");
        outputWriter.write("Proposed Method SCH: "+PR_Sch+"\n");
        outputWriter.write("Classic NMR SCH:     "+NMR_Sch+"\n");
        outputWriter.write("Medina 2017 SCH:     "+Med_Sch+"\n");


        System.out.println("Proposed Method SCH: "+PR_Sch);
        System.out.println("Classic NMR SCH:     "+NMR_Sch);
        System.out.println("Medina 2017 SCH:     "+Med_Sch);


        outputWriter.write("Proposed Method Avg. Power= "+(Pro_power[0]/PR_Sch)+"\n");
        outputWriter.write("Classic NMR Avg. Power= "+(NMR_power[0]/NMR_Sch)+"\n");
        outputWriter.write("Medina 2017 Avg. Power= "+(Med_power[0]/Med_Sch)+"\n");

        System.out.println("Proposed Method Avg. Power= "+(Pro_power[0]/PR_Sch));
        System.out.println("Classic NMR Avg. Power= "+(NMR_power[0]/NMR_Sch));
        System.out.println("Medina 2017 Avg. Power= "+(Med_power[0]/Med_Sch));

        outputWriter.write("Proposed Method Peak Power= "+(Pro_power[1]/PR_Sch)+"\n");
        outputWriter.write("Classic NMR Peak Power= "+(NMR_power[1]/NMR_Sch)+"\n");
        outputWriter.write("Medina 2017 Peak Power= "+(Med_power[1]/Med_Sch)+"\n");

        System.out.println("Proposed Method Peak Power= "+(Pro_power[1]/PR_Sch));
        System.out.println("Classic NMR Peak Power= "+(NMR_power[1]/NMR_Sch));
        System.out.println("Medina 2017 Peak Power= "+(Med_power[1]/Med_Sch));
        outputWriter.flush();
        outputWriter.close();
    }

}


