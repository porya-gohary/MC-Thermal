import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static java.lang.Math.ceil;

public class ClassicNMR {

    McDAG dag;
    int n_core;
    int deadline;
    String benchmark[];
    int benchmark_time[];
    double n;
    Vertex v[];
    int n_overrun=0;
    CPU cpu;

    public ClassicNMR(McDAG dag, int n_core, int deadline, String[] benchmark, int[] benchmark_time, double n,int n_overrun ) {
        this.dag = dag;
        this.n_core = n_core;
        this.deadline = deadline;
        this.benchmark = benchmark;
        this.benchmark_time = benchmark_time;
        this.n = n;
        this.n_overrun=n_overrun;
        v=dag.getVertices().toArray(new Vertex[0]);
        this.sort_vertex();
        this.clean_sch();
        this.check_feasible();
        this.clean_sch();
        this.mScheduling();
    }

    public void check_feasible(){
        cpu=new CPU(deadline,n_core,dag);
        int j=0;
        for (int x = 0; x < v.length; x++) {
            for(Vertex a: v){
                if(a.isHighCr()) {

                    if(a.getScheduled()==n) continue;
                    //System.out.println("SCH  "+a.getScheduled()+"   "+ n);
                    for (int l = 0; l < n; l++) {
                        j=0;
                        for (int i = 0; i < deadline; i++) {
                            if(!a.check_runnable(cpu.get_Running_Tasks(i),n)) continue;
                            boolean CPU_runnable=true;
                            for(Edge e: a.getRcvEdges()){
                                if(cpu.getEndTimeTask(e.getSrc().getName()+" CR"+(int)(n))>i){
                                    CPU_runnable=false;
                                }
                                if((cpu.getEndTimeTask(e.getSrc().getName()+" CO"+(int)(n))>i) &&(cpu.getEndTimeTask(e.getSrc().getName()+" CO"+(int)(n))!=-1) ){
                                    CPU_runnable=false;
                                }

                            }
                            if(!CPU_runnable) {
                              //  System.out.println("RUN!");
                                continue;
                            }

                            boolean run=true;
                            for (int k = 0; k < n; k++) {
                                if(!cpu.CheckTimeSlot(j+k, i,i+a.getWcet(1))){
                                    run=false;
                                }
                            }
                            if(run){
                                for (int k = 0; k < n; k++) {
                                    cpu.SetTaskOnCore(a.getName() + " CR" + k, (j+k), i, i + a.getWcet(0) - 1);
                                    cpu.SetTaskOnCore(a.getName() + " CO" + k, (j+k), i +a.getWcet(0),i+ a.getWcet(1)-1);
                                    a.setScheduled(a.getScheduled() + 1);
                                    System.out.println(a.getScheduled()+"   "+n+"   > "+k);
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
                        if(a.getScheduled()==n) break;
                    }
                }else{
                    if(a.getScheduled()==1) continue;
                    for (int i = 0; i < deadline; i++) {
                        if (!a.check_runnable(cpu.get_Running_Tasks(i), n)) continue;

                        if (cpu.CheckTimeSlot(j, i, i + a.getWcet(0))){
                            cpu.SetTaskOnCore(a.getName() + " CR0", j, i, i + a.getWcet(0));
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

    }

    public void mScheduling(){
        Random overrun= new Random();
        int o = 0;
        String ov_name;
        ArrayList<String> ov=new ArrayList<String>();
        for (int i = 0; i < n_overrun; i++) {
            do{
                o=overrun.nextInt(n_core);
            }while(cpu.Endtime(o)==0);

            do{
                ov_name=cpu.getRunningTaskWithReplica(o,overrun.nextInt(cpu.Endtime(o)));
            }while(ov_name==null || dag.getNodebyName(ov_name.split(" ")[0]).getWcet(1)==0 || ov_name.contains("CO"));
            ov.add(ov_name);
            System.out.println("|||| OV |||||   "+ov_name+"  Core: "+o);
        }

        cpu=new CPU(deadline,n_core,dag);
        int j=0;
        for (int x = 0; x < v.length; x++) {
            for(Vertex a: v){
                if(a.isHighCr()) {

                    if(a.getScheduled()==n) continue;
                    //System.out.println("SCH  "+a.getScheduled()+"   "+ n);
                    for (int l = 0; l < n; l++) {
                        j=0;
                        for (int i = 0; i < deadline; i++) {
                            if(!a.check_runnable(cpu.get_Running_Tasks(i),n)) continue;
                            boolean CPU_runnable=true;
                            for(Edge e: a.getRcvEdges()){
                                if(cpu.getEndTimeTask(e.getSrc().getName()+" CR"+(int)(n))>i){
                                    CPU_runnable=false;
                                }
                                for (int k = 0; k < n ; k++) {
                                    if((cpu.getEndTimeTask(e.getSrc().getName()+" CO"+(int)(k))>i) &&(cpu.getEndTimeTask(e.getSrc().getName()+" CO"+(int)(k))!=-1) ){
                                        CPU_runnable=false;
                                    }

                                }

                            }
                            if(!CPU_runnable) {
                                //  System.out.println("RUN!");
                                continue;
                            }

                            boolean run=true;
                            for (int k = 0; k < n; k++) {
                                if(ov.indexOf(a.getName()+" CR"+k)!=0) {
                                    if (!cpu.CheckTimeSlot(j + k, i, i + a.getWcet(1))) {
                                        run = false;
                                    }
                                }else{
                                    if (!cpu.CheckTimeSlot(j + k, i, i + a.getWcet(0))) {
                                        run = false;
                                    }
                                }
                            }
                            if(run){
                                for (int k = 0; k < n; k++) {
                                    if(ov.indexOf(a.getName()+" CR"+k)>=0) {
                                        cpu.SetTaskOnCore(a.getName() + " CR" + k, (j + k), i, i + a.getWcet(0) - 1);
                                        cpu.SetTaskOnCore(a.getName() + " CO" + k, (j + k), i + a.getWcet(0), i + a.getWcet(1) - 1);
                                        a.setScheduled(a.getScheduled() + 1);
                                        System.out.println(a.getScheduled() + "   " + n + "   > " + k);
                                    }else{
                                        cpu.SetTaskOnCore(a.getName() + " CR" + k, (j + k), i, i + a.getWcet(0) - 1);
                                        a.setScheduled(a.getScheduled() + 1);
                                    }
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
                        if(a.getScheduled()==n) break;
                    }
                }else{
                    if(a.getScheduled()==1) continue;
                    for (int i = 0; i < deadline; i++) {
                        if (!a.check_runnable(cpu.get_Running_Tasks(i), n)) continue;

                        if (cpu.CheckTimeSlot(j, i, i + a.getWcet(0))){
                            cpu.SetTaskOnCore(a.getName() + " CR0", j, i, i + a.getWcet(0));
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
        try {
            cpu.debug("NMR-mainSCH");
            cpu.Save_Power("1","NMR-mainSCH");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void sort_vertex() {
        Arrays.sort(v);
        Collections.reverse(Arrays.asList(v));
    }

    public void clean_sch(){
        for(Vertex a: v){
            a.setScheduled(0);
        }
    }
}
