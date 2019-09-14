import java.io.IOException;
import java.util.*;

import static java.lang.Math.ceil;

public class Salehi {
    //DAG
    McDAG dag;
    String xml_name;
    //Number of Core
    int n_core;
    int deadline;
    String benchmark[];
    int benchmark_time[];
    double n;
    Vertex v[];

    CPU cpu,block_cpu;

    //Faulty Tasks
    Set<Vertex> faults;
    Vertex faults_array[];

    //Number of fault
    int n_fault=0;
    double fault_percent;


    public Salehi(McDAG dag, int n_core, int deadline, double n, String xml_name, int n_fault, double fault_percent) throws Exception {
        this.dag = dag;
        this.n_core = n_core;
        this.deadline = deadline;
        this.n = n;
        v=dag.getVertices().toArray(new Vertex[0]);
        this.n_fault = n_fault;
        this.xml_name = xml_name;
        this.fault_percent = fault_percent;
        sort_vertex();
        clean_sch();
        check_feasible();
        inject_fault(n_fault);
    }

    public void check_feasible() throws Exception {
        cpu=new CPU(deadline,n_core,dag);
        int j=0;
        for (int x = 0; x < v.length; x++) {
            for (Vertex a : v) {
                if(a.isHighCr()) {
                    if(a.getScheduled()==ceil(n/2)) continue;
                    //For Add Extra Copy for HI-Critical Tasks
                    for (int l = 0; l < ceil(n / 2); l++) {
                        j = 0;

                        for (int i = 0; i < deadline; i++) {
                            if (!a.check_runnable(cpu.get_Running_Tasks(i), n)) continue;
                            boolean CPU_runnable=true;
                            for(Edge e: a.getRcvEdges()){
                                if(cpu.getEndTimeTask(e.getSrc().getName()+" CR"+(int)(ceil(n/2)-1))>i){
                                    CPU_runnable=false;
                                }
                                if((cpu.getEndTimeTask(e.getSrc().getName()+" CO"+(int)(ceil(n/2)-1))>i) &&(cpu.getEndTimeTask(e.getSrc().getName()+" CO"+(int)(ceil(n/2)-1))!=-1) ){
                                    CPU_runnable=false;
                                }

                            }
                            if(!CPU_runnable) {
                                //  System.out.println("RUN!");
                                continue;
                            }
                            boolean run=true;
                            for (int k = 0; k < (int)(ceil(n/2)); k++) {
                                if(!cpu.CheckTimeSlot(j+k, i,i+a.getWcet(1))){
                                    run=false;
                                }
                            }
                            if(run){
                                for (int k = 0; k < (int)(ceil(n/2)); k++) {
                                    cpu.SetTaskOnCore(a.getName() + " CR" + k, (j+k), i, i + a.getWcet(0) - 1);
                                    cpu.SetTaskOnCore(a.getName() + " CO" + k, (j+k), i +a.getWcet(0),i+ a.getWcet(1)-1);
                                    a.setScheduled(a.getScheduled() + 1);
                                    //System.out.println(a.getScheduled()+"   "+n+"   > "+k);
                                }
                                break;
                            }
                            if (j<(n_core-1)){
                                j++;
                                i--;
                            }
                            else {
                                j = 0;
                            }
                        }
                        if(a.getScheduled()==(int)(ceil(n/2))) break;
                    }

                }else{
                    //One Replica For LO-Critical Tasks
                    if(a.getScheduled()==1) continue;
                    for (int i = 0; i < deadline; i++) {
                        if (!a.check_runnable(cpu.get_Running_Tasks(i), n)) continue;
                        boolean CPU_runnable=true;

                        for(Edge e: a.getRcvEdges()){
                            if(cpu.getEndTimeTask(e.getSrc().getName()+" CR"+(int)(ceil(n/2)-1))>i){
                                CPU_runnable=false;
                            }
                            if((cpu.getEndTimeTask(e.getSrc().getName()+" CO"+(int)(ceil(n/2)-1))>i) &&(cpu.getEndTimeTask(e.getSrc().getName()+" CO"+(int)(ceil(n/2)-1))!=-1) ){
                                CPU_runnable=false;
                            }

                        }


                        if(!CPU_runnable) continue;
                        if (cpu.CheckTimeSlot(j, i, i + a.getWcet(0))){
                            cpu.SetTaskOnCore(a.getName() + " CR"+(int)(ceil(n/2)-1), j, i, i + a.getWcet(0));
                            a.setScheduled(a.getScheduled() + 1);
                            break;
                        }
                        if (j < (n_core - 1)) {
                            j++;
                            i--;
                        } else {
                            j = 0;
                        }
                    }
                }

            }
        }
        for(Vertex a: v){
            if(a.isHighCr()) {
                if(a.getScheduled()<(int)(ceil(n/2)-1)) throw new Exception("Infeasible!");
            }else{
                if(a.getScheduled()!=1) throw new Exception("Infeasible!");
            }
        }


        try {
            cpu.debug("Salehi-mainSCH");
            cpu.Save_Power("OV"+"0.0"+"F"+fault_percent,xml_name, "Salehi-mainSCH");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void inject_fault(int number_of_fault) throws Exception {
        faults=new HashSet<Vertex>();

        Set<Vertex> nodesHI=new HashSet<Vertex>();
        for(Vertex a:dag.getVertices()){
            if (a.getWcet(1)!=0)
                nodesHI.add(a);
        }
        Vertex HIv[] = nodesHI.toArray(new Vertex[0]);

        int f=0;
        Random fault= new Random();
//        System.out.println("...................-->> "+number_of_fault);
        for (int i = 0; i < number_of_fault; i++) {
            do {
                f = fault.nextInt(HIv.length);
            } while (HIv[f].getInjected_fault() != 0);
            System.out.println("↯↯ Fault injected To  " + HIv[f].getName());
            dag.getNodebyName(HIv[f].getName()).setInjected_fault(dag.getNodebyName(HIv[f].getName()).getInjected_fault() + 1);
            faults.add(dag.getNodebyName(HIv[f].getName()));
        }
//        faults_array=faults.toArray(new Vertex[0]);
//        Arrays.sort(faults_array);
//        for(Vertex a:faults_array){
//            System.out.println(a.getName()+"  ==>>  "+(a.getWcet(0)+a.getWcet(1)));
//        }

        int t=cpu.getEndTimeTask(HIv[f].getName()+" CR"+(int)(ceil(n/2)-1));
    }


    public void make_blocks(int time) throws Exception {
        time++;

        Set<Vertex> block=new HashSet<Vertex>();
        //Set<Vertex> faults=new HashSet<Vertex>();
        for(Vertex a: v){
            if (!a.check_runnable(cpu.get_Running_Tasks(time), n)) continue;
            if (faults.contains(a) && a.getScheduled()==n)continue;
            if(cpu.getEndTimeTask(" CR"+(int)(ceil(n/2)-1))<=time) continue;
            block.add(a);
        }


    }

    public void sort_vertex() {
        Arrays.sort(v);
        Collections.reverse(Arrays.asList(v));
        //Show Sorted Vortex Array
//        for(Vertex a:v){
//            System.out.println(a.getName()+"  ==>>  "+(a.getWcet(0)+a.getWcet(1)));
//        }
    }

    public void clean_sch() {
        for (Vertex a : v) {
            a.setScheduled(0);
        }
    }
}