import java.util.ArrayList;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

public class Heba {
    ArrayList<window> block;
    //Deadline
    int deadline;
    //Number of CPU Core
    int n_core;
    //Number of Redundancy
    double n;
    //DAG
    McDAG mcDAG;
    CPU cpu;
    String xml_name;

    int max_freq;

    public Heba(ArrayList<window> block, int deadline, int n_core, double n, McDAG mcDAG, String xml_name, int max_freq) {
        this.block = block;
        this.deadline = deadline;
        this.n_core = n_core;
        this.n = n;
        this.mcDAG = mcDAG;
        this.xml_name = xml_name;
        this.max_freq = max_freq;
    }

    public void scheduling() throws Exception {
        cpu = new CPU(deadline, n_core, mcDAG);
        for (window w : block) {
            int k = 0;
            for (Vertex a : w.getTasks()) {
                System.out.println(".... " + a.getName());
                for (Edge e : a.getRcvEdges()) {
                    if (cpu.getEndTimeTask(e.getSrc().getName() + " F" + (int) (floor(n / 2) - 1)) == -1) {
                        System.out.println("HEBA ---> Task: " + a.getName());
                        cpu.debug(xml_name);
                        throw new Exception("Infeasible!");
                    } else {
                        if (cpu.getEndTimeTask(e.getSrc().getName() + " F" + (int) (floor(n / 2) - 1)) > k) {
                            k = cpu.getEndTimeTask(e.getSrc().getName() + " F" + (int) (floor(n / 2) - 1));
                        }
                    }
                }
                System.out.println("TSP Task: " + a.getName()+" = "+a.getTSP_Active());
                for (int l = 0; l < ceil(n / 2); l++) {
                    boolean exitFlag = false;
                    for (int j = 0; j < n_core; j++) {
                        for (int i = (k == 0 ? 0 : k + 1); i < deadline - a.getRunningTimeLO(max_freq, a.getMin_freq()) + 1; i++) {
                            if (cpu.CheckTimeSlot(j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1) &&
                                    (cpu.maxCoreInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1) >= a.getTSP_Active()) &&
                                    (cpu.numberOfRunningTasksInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1) < a.getTSP_Active())) {
                                cpu.SetTaskOnCore(a.getName() + " R" + l, j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1);
                                System.out.println("SCH --> Task: " + a.getName());
                                a.setScheduled(a.getScheduled() + 1);
                                exitFlag = true;
                                break;
                            }
                        }
                        if (exitFlag) break;
                    }
                }
                 if(a.getScheduled()!=(int)(ceil(n/2))) throw new Exception("Infeasible!");


            }

            //insert Faulty window
            k = 0;
            for (Vertex a : w.getTasks()) {  //Find Max. End time of tasks in the window
                if (cpu.getEndTimeTask(a.getName() + " R" + (int) (ceil(n / 2) - 1)) > k) {
                    k = cpu.getEndTimeTask(a.getName() + " R" + (int) (ceil(n / 2) - 1));
                }
            }
            int b = 0;
            for (int i = k + 1; i < k + w.getSize() + 1; i++) {
                for (int j = 0; j < w.cpu.getN_Cores(); j++) {
                    cpu.SetTask(j, i, w.cpu.getRunningTask(j, b));
                }
                b++;
            }

        }
    }
}
