import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static java.lang.Math.ceil;

public class mainScheduling {
    //Deadline
    int deadline;
    //Number of CPU Core
    int n_core;
    //Number of Redundancy
    double n;
    //DAG
    McDAG mcDAG;
    //Vertex Array for Sorting Vertexes
    Vertex v[];

    double max_voltage;

    CPU cpu;

    public mainScheduling(Vertex[] v,McDAG mcDAG,double n,int deadline,int n_core,double max_voltage) {
        this.deadline = deadline;
        this.n_core = n_core;
        this.n = n;
        this.mcDAG = mcDAG;
        this.v = v;
        this.max_voltage = max_voltage;
    }

    public void mScheduling(){
        cpu=new CPU(deadline,n_core,mcDAG);
        int j=0;
        for (int x = 0; x < v.length; x++) {
            for(Vertex a: v){

                if(a.isHighCr()) {
                    if(a.getScheduled()==ceil(n/2)) continue;
                    //For Add Extra Copy for HI-Critical Tasks
                    for (int l = 0; l < ceil(n / 2); l++) {
                        j=0;
                        for (int i = 0; i < deadline; i++) {
                            if(!a.check_runnable(cpu.get_Running_Tasks(i),n)) continue;
                            if(cpu.CheckTimeSlot(j, i,i+a.getRunningTimeLO(max_voltage,a.getMin_voltage())-1) && (cpu.maxCoreInterval(i,i+a.getRunningTimeLO(max_voltage,a.getMin_voltage())-1)>=a.getTSP_Active()) &&
                                    (cpu.numberOfRunningTasksInterval(i,i+a.getRunningTimeLO(max_voltage,a.getMin_voltage())-1)<a.getTSP_Active())){
                                cpu.SetTaskOnCore(a.getName()+" R"+l,j,i,i+a.getRunningTimeLO(max_voltage,a.getMin_voltage())-1);
                                a.setScheduled(a.getScheduled()+1);
                                System.out.println(a.getName()+"   "+a.getScheduled());
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
                            
                        
                    }
                }else{
                    //One Replica For LO-Critical Tasks
                    if(a.getScheduled()==1) continue;
                    for (int i = 0; i < deadline; i++) {
                        if (!a.check_runnable(cpu.get_Running_Tasks(i), n)) continue;
                        if (cpu.CheckTimeSlot(j, i, i + a.getRunningTimeLO(max_voltage, a.getMin_voltage())) && (cpu.maxCoreInterval(i, i + a.getRunningTimeLO(max_voltage, a.getMin_voltage())) >= a.getTSP_Active()) &&
                                (cpu.numberOfRunningTasksInterval(i, i + a.getRunningTimeLO(max_voltage, a.getMin_voltage())) < a.getTSP_Active())) {
                            cpu.SetTaskOnCore(a.getName() + " R0", j, i, i + a.getRunningTimeLO(max_voltage, a.getMin_voltage()));
                            a.setScheduled(a.getScheduled() + 1);
                            System.out.println(a.getName() + "   " + a.getScheduled());
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
            cpu.debug("mainSCH");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clean_sch(){
        for(Vertex a: v){
            a.setScheduled(0);
        }
    }

    public void sort_vertex() {
        Arrays.sort(v);
        Collections.reverse(Arrays.asList(v));
        //Show Sorted Vortex Array

        for(Vertex a:v){
            System.out.println(a.getName()+"  ==>>  "+a.getLPL()+"    -=>  "+a.getScheduled());
        }
    }
}
