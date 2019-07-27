import java.util.Arrays;
import java.util.Collections;

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

    public mainScheduling(Vertex[] v,McDAG mcDAG,double n,int deadline,int n_core,double max_vlotage) {
        this.deadline = deadline;
        this.n_core = n_core;
        this.n = n;
        this.mcDAG = mcDAG;
        this.v = v;
        this.max_voltage = max_voltage;
    }

    public void mScheduling(){
        cpu=new CPU(deadline,n_core,mcDAG);

    }

    public void sort_vertex() {
        Arrays.sort(v);
        Collections.reverse(Arrays.asList(v));
        //Show Sorted Vortex Array
        for(Vertex a:v){
            System.out.println(a.getName()+"  ==>>  "+a.getLPL());
        }
    }
}
