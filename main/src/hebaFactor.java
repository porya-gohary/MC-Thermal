import java.io.File;
import java.util.Set;

public class hebaFactor {
    double max_temp=60;
    boolean VERBOSE = false;
    String pathSeparator = File.separator;

    //HotSpot location and information
    String hotspot_path = "HotSpot" + pathSeparator + "hotspot";
    String hotspot_config = "HotSpot" + pathSeparator + "configs" + pathSeparator;
    String floorplan = "HotSpot" + pathSeparator + "floorplans" + pathSeparator;
    String powertrace = "HotSpot" + pathSeparator + "powertrace" + pathSeparator;
    String thermaltrace = "HotSpot" + pathSeparator + "thermaltrace" + pathSeparator + "thermal.ttrace";
    Vertex sorted_tasks[];

    //Deadline
    int deadline;
    //Number of CPU Core
    int n_core;
    //Number of Redundancy
    double n;
    //DAG
    McDAG dag;
    //System CPU
    CPU cpu;
    //System CPU temp
    CPU cpu2;
    Set<String> ov_tasks;

    public hebaFactor(double max_temp, boolean VERBOSE, double n, McDAG dag, CPU cpu, Vertex[] sorted_tasks, Set<String> ov_tasks) {
        this.max_temp = max_temp;
        this.VERBOSE = VERBOSE;
        this.n = n;
        this.dag = dag;
        this.cpu = cpu;
        this.sorted_tasks = sorted_tasks;
        this.ov_tasks=ov_tasks;
        n_core=cpu.getN_Cores();
        deadline=cpu.getDeadline();
    }


    public void run(){
        for (Vertex a : sorted_tasks) {
            if (cpu.getEndTimeTask(a.getName() + " R0") != -1) continue;

            int k = 0;
            boolean runnable=true;
            for (Edge e : a.getRcvEdges()) {
                if (e.getSrc().isHighCr()) {
                    for (int i = 0; i < n; i++) {
                        if (ov_tasks.contains(e.getSrc().getName() + " R" + i)) {
                            if (cpu.getEndTimeTask(e.getSrc().getName() + " OV" + i) == -1) {
                                runnable=false;
                            }
                        } else {
                            if (cpu.getEndTimeTask(e.getSrc().getName() + " R" + i) == -1) {
                                runnable=false;
                            }
                        }
                    }
                } else {
                    if (cpu.getEndTimeTask(e.getSrc().getName() + " R0") == -1) {
                        System.out.println("INF ---> Task: " + e.getSrc().getName());
                        runnable=false;
                    }
                }
                if (!runnable) break;
                if (e.getSrc().isHighCr()) {
                    for (int i = 0; i < n; i++) {
                        if (ov_tasks.contains(e.getSrc().getName() + " R" + i)) {
                            if (cpu.getEndTimeTask(e.getSrc().getName() + " OV" + i) > k) {
                                k = cpu.getEndTimeTask(e.getSrc().getName() + " OV" + i);
                            }
                        } else {
                            if (cpu.getEndTimeTask(e.getSrc().getName() + " R" + i) > k) {
                                k = cpu.getEndTimeTask(e.getSrc().getName() + " R" + i);
                            }
                        }
                    }

                } else {
                    if (cpu.getEndTimeTask(e.getSrc().getName() + " R0") > k) {
                        k = cpu.getEndTimeTask(e.getSrc().getName() + " R0");
                    }
                }
            }
            if(!runnable) continue;
        }
    }

    public double QoS() {
        double QoS = 0;
        for (int i = 0; i < sorted_tasks.length; i++) {
            if (!sorted_tasks[i].isHighCr() && sorted_tasks[i].isRun()) QoS++;
        }
        QoS = QoS / (sorted_tasks.length - dag.getNodes_HI().size());
        return QoS;
    }
}
