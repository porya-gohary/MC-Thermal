import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import static java.lang.Math.pow;

public class ProposedMethod {

    double landa0;
    int d;
    double v[];
    int freq[];
    String tsp_name;

    McDAG dag;
    int n_core;
    int deadline;
    String rel_name;
    String benchmark[];
    int benchmark_time[];
    int max_freq_cores;
    double n;

    public ProposedMethod(double landa0, int d, double[] v, int[] freq, String TSP_File_name, McDAG dag, int n_core,
                          int deadline, String rel_name, String[] benchmark, int[] benchmark_time, int max_freq_cores, double n) {
        this.landa0 = landa0;
        this.d = d;
        this.v = v;
        this.freq = freq;
        this.tsp_name= TSP_File_name;
        this.dag = dag;
        this.n_core = n_core;
        this.deadline = deadline;
        this.rel_name = rel_name;
        this.benchmark = benchmark;
        this.benchmark_time = benchmark_time;
        this.max_freq_cores = max_freq_cores;
        this.n = n;
    }

    public void start() throws Exception {
        try {
            this.relibility_creator();
        }catch (IOException e){
            e.printStackTrace();
        }
        File rel= new File(rel_name+".txt");
        Reliability_cal rc=new Reliability_cal(n,landa0,d,v[v.length-1],v[0],rel,v,dag);

        File tsp_input=new File(tsp_name+".txt");
        TSP tsp=new TSP(tsp_input,n_core,v,freq,dag);


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

        mainScheduling mainScheduling=new mainScheduling(dag.getVertices().stream().toArray(Vertex[]::new).clone(),dag,n,deadline,n_core, v[v.length-1],freq[freq.length-1], max_freq_cores);
        mainScheduling.clean_sch();
        mainScheduling.sort_vertex();
        mainScheduling.mScheduling();
        mainScheduling.inject_fault(2);
        mainScheduling.overrun(2);

    }

    public void relibility_creator() throws IOException {
        double rel[]=new double[dag.getVertices().size()];
        for (int i = 0; i < dag.getVertices().size(); i++) {
            Random rnd= new Random();
            double t=0;
            int a=rnd.nextInt(3);
            if(a==0) t=0.8;
            else t=0.9;
            a=rnd.nextInt((int)n+2);
            for (int j = 2; j < a+1; j++) {
                t+=pow(0.1,j)*9;
            }
            rel[i]=t;
        }

        BufferedWriter outputWriter = null;
            outputWriter = new BufferedWriter(new FileWriter(rel_name+".txt"));
            for (int j = 0; j < dag.getVertices().size(); j++) {
                outputWriter.write(rel[j]+"\n");
            };
            outputWriter.flush();
            outputWriter.close();

    }
}
