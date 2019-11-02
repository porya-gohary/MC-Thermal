import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.pow;


public class main {
    public static void main(String args[]) throws IOException, SAXException, ParserConfigurationException {
        double n = 3;
        int deadline = 900;
        int n_core = 36;

        McDAG dag;

        //Dag XML Name
        String xml_name = "1";
        //TSP File name
        String tsp_name = "TSP";
        //Reliability File Name
        String rel_name = "rel";

        double landa0 = 0.000001;
        int d = 3;

        //Number Of Overrun
        int n_overrun = 0;

        double overrun_percent = 0.0;

        //Number Of Fault
        int n_fault = 0;

        //Bool For make New DAGS
        boolean create_dag =false;

        //number of cores that can work with max freq in same time
        int max_freq_cores = 3;

//        double percent[] = {0.0, 0.25, 0.5, 0.75, 1.0};
        double percent[] = {0.0, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.40, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1.0};
        double fault_pecent = 0.0;
        //Number of DAG
        int n_DAGs = 50;
        McDAG All_DAG[] = new McDAG[n_DAGs + 1];
        int All_deadline[] = new int[n_DAGs + 1];

        boolean PR_inf[] = new boolean[n_DAGs + 1];
        boolean NMR_inf[] = new boolean[n_DAGs + 1];
        boolean Med_inf[] = new boolean[n_DAGs + 1];
        boolean Sal_inf[] = new boolean[n_DAGs + 1];
        ;
        //Scheduling Results:
        int PR_Sch;
        int NMR_Sch;
        int Med_Sch;
        int Sal_Sch;

        //without overrun Power Results
        double Pro_power[] = new double[2];
        double NMR_power[] = new double[2];
        double Med_power[] = new double[2];
        double Sal_power[] = new double[2];




        //double v[]={0.912,0.9125,0.95,0.987,1.025,1.065,1.1,1.13,1.16,1.212,1.26};
        double v[] = {0.973,1.023, 1.062, 1.115, 1.3};
        //Possible Frequencies
        int freq[] = {1000,1200, 1400, 1600, 2000};
        //Benchmarks Name
        String benchmark[] = {"Basicmath", "Bitcount", "Dijkstra", "FFT", "JPEG", "Patricia", "Qsort", "Sha", "Stringsearch", "Susan"};
        int benchmark_time[] = {156, 25, 33, 160, 28, 87, 25, 13, 8, 20};

        PR_Sch = n_DAGs;
        NMR_Sch = n_DAGs;
        Med_Sch = n_DAGs;
        if (create_dag) {
            for (int i = 1; i <= n_DAGs; i++) {
                xml_name = i + "";
                System.out.println("Mapping :::> DAG " + xml_name + "");
                File file = new File("DAGs\\" + xml_name + ".xml");
                dag_Reader dr = new dag_Reader(file);
                dag = dr.getDag();
                benchmark_mapping benchmark_mapping = new benchmark_mapping(dag, benchmark, benchmark_time);
                benchmark_mapping.mapping();
                benchmark_mapping.cal_LPL();
                deadline = benchmark_mapping.cal_deadline(n);
                benchmark_mapping.debug();
                All_deadline[i] = deadline;
                All_DAG[i] = dag;
                try {
                    relibility_creator(dag, "rel" + i, n);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            WriteObjectToFile(All_DAG.clone(), "DAGs.txt");
            WriteObjectToFile(All_deadline, "Deadline.txt");
        } else {
            All_DAG = (McDAG[]) ReadObjectFromFile("DAGs.txt");
            All_deadline = (int[]) ReadObjectFromFile("Deadline.txt");
        }

//        xml_name = "1";
//        dag = All_DAG[1];
//        dag.setHINodes();
//        fault_pecent=0.1;
//        n_fault = (int) (dag.getNodes_HI().size() * fault_pecent);
//        deadline = All_deadline[1];
//
//        try {
//            Salehi salehi = new Salehi(dag, n_core, deadline, n, xml_name, n_fault, fault_pecent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

 //       for (int h = 0; h < percent.length; h++) {
   //         fault_pecent = percent[h];
//        fault_pecent=0.25;
//        overrun_percent=0.5;
            // overrun_percent = percent[h];
     //       for (int j = 0; j < percent.length; j++) {
       //         overrun_percent = percent[j];

                Pro_power = new double[2];
                NMR_power = new double[2];
                Med_power = new double[2];
                Sal_power = new double[2];

                PR_Sch = n_DAGs;
                NMR_Sch = n_DAGs;
                Med_Sch = n_DAGs;
                Sal_Sch = n_DAGs;

                File newFolder2 = new File("OV" + overrun_percent + "F" + fault_pecent);
                newFolder2.mkdir();
                BufferedWriter outputWriter = null;
                outputWriter = new BufferedWriter(new FileWriter("OV" + overrun_percent + "F" + fault_pecent + "\\" + "Summary.txt"));
                for (int i = 1; i <= n_DAGs; i++) {
                    xml_name = i + "";
                    outputWriter.write(">>>>>>>>>> ::: DAG " + xml_name + " Start ::: <<<<<<<<<<" + "\n");

                    dag = All_DAG[i];
                    dag.setHINodes();
                    deadline = All_deadline[i];
                    n_overrun = (int) (dag.getNodes_HI().size() * overrun_percent);
                    n_fault = (int) (dag.getNodes_HI().size() * fault_pecent);
                    //System.out.println(dag.getNodes_HI().size());
//                System.out.println("NUMBER OF OVERRUN= "+ n_overrun);
                    //dr.readXML();


                    //deadline=700;
                    System.out.println("Deadline= " + deadline);
                    //   --->>>>  PROPOSED METHOD
                    System.out.println("------------> Proposed Method <----------");
                    outputWriter.write("------------> Proposed Method <----------" + "\n");
                    if (!PR_inf[i]) {
                        ProposedMethod proposedMethod = new ProposedMethod(landa0, d, v, freq, tsp_name, dag, n_core, deadline, rel_name + i, benchmark,
                                benchmark_time, max_freq_cores, n_overrun, n_fault, overrun_percent, fault_pecent, n, xml_name);
                        try {
                            proposedMethod.start();
                            outputWriter.write("Avg. Power= " + proposedMethod.mainScheduling.cpu.power_results()[0] + "\n");
                            outputWriter.write("Peak Power= " + proposedMethod.mainScheduling.cpu.power_results()[1] + "\n");
                            Pro_power[0] += proposedMethod.mainScheduling.cpu.power_results()[0];
                            Pro_power[1] += proposedMethod.mainScheduling.cpu.power_results()[1];
                            proposedMethod = null;

                        } catch (Exception e) {
                            // e.printStackTrace();
                            if (overrun_percent == 0 && fault_pecent == 0) {
                                PR_inf[i] = true;
                            }
                            System.out.println("[ PROPOSED METHOD ] Infeasible!   " + xml_name);
                            outputWriter.write("[ PROPOSED METHOD ] Infeasible!   " + xml_name + "\n");
                            PR_Sch--;
                            e.printStackTrace();

                            if (!Arrays.toString(e.getStackTrace()).contains("Safe_Start_Time"))
                                // System.out.println(Arrays.toString(e.getStackTrace()).contains("Safe_Start_Time"));
                                System.exit(3);
                        }
                    } else {
                        System.out.println("[ PROPOSED METHOD ] Infeasible!   " + xml_name);
                        outputWriter.write("[ PROPOSED METHOD ] Infeasible!   " + xml_name + "\n");
                        PR_Sch--;
                    }

                    if (overrun_percent == 0) {
                        System.out.println("------------> LE-NMR (SALEHI) <----------");
                        outputWriter.write("------------> LE-NMR (SALEHI) <----------" + "\n");
                        try {
                            Salehi salehi = new Salehi(dag, n_core, deadline, n, xml_name, n_fault, fault_pecent);
                            outputWriter.write("Avg. Power= " + salehi.cpu.power_results()[0] + "\n");
                            outputWriter.write("Peak Power= " + salehi.cpu.power_results()[1] + "\n");

                            Sal_power[0] += salehi.cpu.power_results()[0];
                            Sal_power[1] += salehi.cpu.power_results()[1];
                            salehi = null;
                        } catch (Exception e) {
                            System.out.println("[ LE-NMR (SALEHI) ] Infeasible!   " + xml_name);
                            outputWriter.write("[ LE-NMR (SALEHI) ] Infeasible!   " + xml_name + "\n");
                            Sal_Sch--;
                            e.printStackTrace();
                        }
                    }


                    if (fault_pecent == 0) {
                        System.out.println("------------> Classic NMR <----------");
                        outputWriter.write("------------> Classic NMR <----------" + "\n");
                        try {
                            ClassicNMR NMR = new ClassicNMR(dag, n_core, deadline, benchmark, benchmark_time, n, n_overrun, overrun_percent, xml_name);
                            outputWriter.write("Avg. Power= " + NMR.cpu.power_results()[0] + "\n");
                            outputWriter.write("Peak Power= " + NMR.cpu.power_results()[1] + "\n");

                            NMR_power[0] += NMR.cpu.power_results()[0];
                            NMR_power[1] += NMR.cpu.power_results()[1];
                            NMR = null;
                        } catch (Exception e) {
                            System.out.println("[ CLASSIC NMR ] Infeasible!   " + xml_name);
                            outputWriter.write("[ CLASSIC NMR ] Infeasible!   " + xml_name + "\n");
                            NMR_Sch--;
                            e.printStackTrace();
                        }


                        System.out.println("------------> Medina 2017 Method <----------");
                        outputWriter.write("------------> Medina 2017 Method <----------" + "\n");
                        try {
                            Medina medina = new Medina(dag, n_core, deadline, benchmark, benchmark_time, n_overrun, overrun_percent, xml_name);
                            outputWriter.write("Avg. Power= " + medina.cpu.power_results()[0] + "\n");
                            outputWriter.write("Peak Power= " + medina.cpu.power_results()[1] + "\n");

                            Med_power[0] += medina.cpu.power_results()[0];
                            Med_power[1] += medina.cpu.power_results()[1];
                            medina = null;
                        } catch (Exception e) {
                            System.out.println("[ MEDINA 2017 ] Infeasible!   " + xml_name);
                            outputWriter.write("[ MEDINA 2017 ] Infeasible!   " + xml_name + "\n");
                            Med_Sch--;
                            e.printStackTrace();
                        }
                        System.out.println("------------> ::: DAG " + xml_name + " END ::: <----------");
                        outputWriter.write(">>>>>>>>>>>>> ::: DAG " + xml_name + " END ::: <<<<<<<<<<<<" + "\n\n");

                    }
                }
                outputWriter.write("\n");
                outputWriter.write(">>>>>>>>>>>>> SUMMARY OF ALL DAGs <<<<<<<<<<<<" + "\n");
                outputWriter.write("Proposed Method SCH: " + PR_Sch + "\n");
                outputWriter.write("LE-NMR (SALEHI) SCH: " + Sal_Sch + "\n");
                outputWriter.write("Classic NMR SCH:     " + NMR_Sch + "\n");
                outputWriter.write("Medina 2017 SCH:     " + Med_Sch + "\n");

                System.out.println(">>>>>>>>>>>>> SUMMARY OF ALL DAGs [Fault: " + (fault_pecent * 100) + "% Overrun: " + (overrun_percent * 100) + "%] <<<<<<<<<<<<");
                System.out.println("Proposed Method SCH: " + PR_Sch);
                System.out.println("LE-NMR (SALEHI) SCH: " + Sal_Sch);
                System.out.println("Classic NMR SCH:     " + NMR_Sch);
                System.out.println("Medina 2017 SCH:     " + Med_Sch);


                outputWriter.write("Proposed Method Avg. Power= " + (Pro_power[0] / PR_Sch) + "\n");
                outputWriter.write("LE-NMR (SALEHI) Avg. Power= " + (Sal_power[0] / Sal_Sch) + "\n");
                outputWriter.write("Classic NMR Avg. Power= " + (NMR_power[0] / NMR_Sch) + "\n");
                outputWriter.write("Medina 2017 Avg. Power= " + (Med_power[0] / Med_Sch) + "\n");

                System.out.println("Proposed Method Avg. Power= " + (Pro_power[0] / PR_Sch));
                System.out.println("LE-NMR (SALEHI) Avg. Power= " + (Sal_power[0] / Sal_Sch));
                System.out.println("Classic NMR Avg. Power= " + (NMR_power[0] / NMR_Sch));
                System.out.println("Medina 2017 Avg. Power= " + (Med_power[0] / Med_Sch));

                outputWriter.write("Proposed Method Peak Power= " + (Pro_power[1] / PR_Sch) + "\n");
                outputWriter.write("LE-NMR (SALEHI) Peak Power= " + (Sal_power[1] / Sal_Sch) + "\n");
                outputWriter.write("Classic NMR Peak Power= " + (NMR_power[1] / NMR_Sch) + "\n");
                outputWriter.write("Medina 2017 Peak Power= " + (Med_power[1] / Med_Sch) + "\n");

                System.out.println("Proposed Method Peak Power= " + (Pro_power[1] / PR_Sch));
                System.out.println("LE-NMR (SALEHI) Peak Power= " + (Sal_power[1] / Sal_Sch));
                System.out.println("Classic NMR Peak Power= " + (NMR_power[1] / NMR_Sch));
                System.out.println("Medina 2017 Peak Power= " + (Med_power[1] / Med_Sch));
                outputWriter.flush();
                outputWriter.close();
  //          }
    //    }


    }

    public static void relibility_creator(McDAG dag, String rel_name, double n) throws IOException {
        double rel[] = new double[dag.getVertices().size()];
        for (int i = 0; i < dag.getVertices().size(); i++) {
            Random rnd = new Random();
            double t = 0;
            int a = rnd.nextInt(3);
            if (a == 0) t = 0.8;
            else t = 0.9;
            a = rnd.nextInt((int) n + 2);
            for (int j = 2; j < a + 1; j++) {
                t += pow(0.1, j) * 9;
            }
            rel[i] = t;
        }

        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter(rel_name + ".txt"));
        for (int j = 0; j < dag.getVertices().size(); j++) {
            outputWriter.write(rel[j] + "\n");
        }
        ;
        outputWriter.flush();
        outputWriter.close();

    }

    public static void WriteObjectToFile(Object serObj, String filename) {
        try {

            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serObj);
            objectOut.flush();
            objectOut.close();
            System.out.println("The Object  was succesfully written to a file");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(4);
        }
    }

    public static Object ReadObjectFromFile(String filename) {
        try {
            McDAG All_DAG[];

            int d[];
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            if (filename == "Deadline.txt") {
                d = (int[]) objectIn.readObject();
                System.out.println("The Object  was succesfully Read From a file");
                return d;
            } else {
                All_DAG = (McDAG[]) objectIn.readObject();
                objectIn.close();

                System.out.println("The Object  was succesfully Read From a file");
                return All_DAG;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(4);
        }
        return null;
    }

}


