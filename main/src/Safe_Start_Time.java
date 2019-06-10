import com.sun.deploy.util.ArrayUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

import static java.lang.Math.ceil;

public class Safe_Start_Time {
    //Deadline
    int deadline;
    //Number of CPU Core
    int n_core;
    //Number of Redundancy
    double n;
    //DAG
    McDAG dag;
    //Vertex Array for Sorting Vertexes
    Vertex v[];

    CPU cpu;

    public Safe_Start_Time(Vertex[] v,double n,int deadline,int n_core) {
        this.v = v;
        this.n=n;
        this.deadline=deadline;
        this.n_core=n_core;
    }


    public void scheduling(){
        cpu=new CPU(deadline,n_core);
        int j=0;
        int k=0;
        for(Vertex a: v){
            if(a.getWcet(1)==0) continue;
            if(a.getScheduled()==ceil(n/2)) continue;
            for (int l = 0; l < ceil(n/2) ; l++) {
                j=0;
                for (int i = (deadline-1); i >= 0; i--) {
                    if(!a.check_runnable(cpu.get_Running_Tasks(i),n)) continue;
                    if(cpu.CheckTimeSlot(j,i-a.getWcet(0),i)){
                        cpu.SetTaskOnCore(a.getName(),j,i-a.getWcet(0),i);
                        a.setScheduled(a.getScheduled()+1);
                        System.out.println(a.getName()+"   "+a.getScheduled());
                        break;
//                  ***********HERE NEED TO COMPLETE*****
                    }
                    if (j<(n_core-1)){
                        j++;
                        i++;
                    }
                    else {
                        j=0;

                    }
                    if(i==0 && j==3) System.err.println(a.getName()+"  ⚠ ⚠ Infeasible!");
                    //                  ***********HERE NEED TO COMPLETE*****

                }
            }
        }

        try {
            cpu.debug();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sort_vertex() {
        Arrays.sort(v);
        //Show Sorted Vortex Array
        for(Vertex a:v){
            System.out.println(a.getName()+"  ==>>  "+a.getLPL());
        }
    }




}
