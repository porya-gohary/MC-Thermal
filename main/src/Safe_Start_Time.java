import java.lang.reflect.Array;
import java.util.Arrays;

public class Safe_Start_Time {
    //Deadline
    int deadline;
    //Number of CPU Core
    int n_core;
    //Number of Redundancy
    int n;
    //DAG
    McDAG dag;
    //Vertex Array for Sorting Vertexes
    Vertex v[];

    public Safe_Start_Time(Vertex[] v) {
        this.v = v;
    }


    public void scheduling(){
        CPU cpu=new CPU(deadline,n_core);
        int j=0;
        for(Vertex a: v){
            j=0;
            if(a.getWcet(1)==0) continue;
            if(a.getScheduled()==2) continue;
            for (int i = 0; i < deadline; i++) {

                if (j<n_core)j++;
            }
        }
    }



    public void sort_vertex() {
        Arrays.sort(v);
        for(Vertex a:v){
            System.out.println(a.getName()+"  ==>>  "+a.getLPL());
            System.out.println(a.getWcet(0)+"      "+a.getWcet(1));
        }
    }


}
