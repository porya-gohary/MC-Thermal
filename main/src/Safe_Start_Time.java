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

    public Safe_Start_Time(Vertex[] v,double n,int deadline,int n_core) {
        this.v = v;
        this.n=n;
        this.deadline=deadline;
        this.n_core=n_core;
    }


    public void scheduling(){
        CPU cpu=new CPU(deadline,n_core);
        int j=0;
        int k=0;
        for(Vertex a: v){
            j=0;
            if(a.getWcet(1)==0) continue;
            if(a.getScheduled()==ceil(n/2)) continue;
            for (int i = (deadline-1); i >= 0; i--) {
                if(!a.check_runnable(cpu.get_Running_Tasks(i),n)) continue;
//                if(cpu.CheckTimeSlot(j,i-a.getWcet(0),i)){
//                  ***********HERE NEED TO COMPLETE*****
//                }
                if (j<n_core)j++;
                else {
                    j=0;
                }
            }
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
