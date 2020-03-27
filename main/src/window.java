import java.util.HashSet;
import java.util.Set;

public class window {
    private Set<Vertex> tasks;
    //Size of window
    private int size=0;
    private int n_task=0;
    //Number of Core in CPU
    private int n_core=0;
    //MC-DAG
    McDAG mcDAG;
    CPU cpu;


    public window(int size, int n_core, McDAG mcDAG) {
        this.size = size;
        this.n_core = n_core;
        this.mcDAG = mcDAG;
        tasks =new HashSet<Vertex>();
        cpu=new CPU(size,n_core,mcDAG);
    }

    public boolean isExist(Vertex v){
        for (Vertex t:tasks ) {
            if(t.getName()==v.getName())
                return true;
        }
        return false;
    }
    public Set<Vertex> getTasks() {
        return tasks;
    }

    public void addTask(Vertex task) {
        tasks.add(task);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getN_task() {
        return n_task;
    }

    public void setN_task(int n_task) {
        this.n_task = n_task;
    }
}
