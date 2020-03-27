import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

public class faulty_window {

    McDAG dag;
    //Number of Core
    int n_core;
    double n;
    Vertex v[];
    Set<Vertex> HIv;
    //Max freq
    int max_freq = 2000;
    //number of cores that can work with max freq in same time
    int max_freq_cores = 1;

    Set<window> block;

    public void make_faulty_window() throws Exception {
        block = new HashSet<window>();

        do {
            int t = 0;
            Vertex temp = null;
            for (Vertex a : HIv) {
                if (a.getWcets()[0] > t) {
                    boolean r = true;
                    for (Edge e : a.getRcvEdges()) {
                        for (Vertex b : HIv) {
                            if (b == e.getSrc()) {
                                r = false;
                                break;
                            }
                        }
                        if (!r) break;
                    }
                    if (!r) continue;
                    t = a.getWcets()[0];
                    temp = a;
                }
            }
            int k = (int) ceil((floor(n / 2)) / max_freq_cores);
            int size = k * temp.getWcet(0);
            HIv.remove(temp);
            window w = new window(size, n_core, dag);
            w.addTask(temp);
            for (int l = 0; l < floor(n / 2); l++) {
                for (int j = 0; j < n_core; j++) {
                    boolean b = false;
                    for (int i = 0; i < size - temp.getWcet(0) + 1; i++) {
                        if (w.cpu.CheckTimeSlot(j, i, i + temp.getWcet(0) - 1) &&
                                w.cpu.numberOfRunningTasksInterval(i, i + temp.getWcet(0) - 1) < max_freq_cores) {
                            b = true;
                            w.cpu.SetTaskOnCore(temp.getName() + " F" + l, j, i, i + temp.getWcet(0) - 1);
                        }
                    }
                    if (b) break;

                }

            }

            // Check other tasks for fitting in this window
            Set<Vertex> HIv2;
            HIv2 = HIv;
            do {
                t = 0;
                temp = null;
                for (Vertex a : HIv2) {
                    if (a.getWcets()[0] > t) {
                        boolean r = true;
                        for (Edge e : a.getRcvEdges()) {
                            for (Vertex b : HIv) {
                                if (b == e.getSrc() || w.isExist(e.getSrc())) { // Checking the task dependencies
                                    r = false;
                                    HIv2.remove(a);
                                    break;
                                }
                            }
                            if (!r) break;
                        }
                        if (!r) continue;
                        t = a.getWcets()[0];
                        temp = a;
                    }
                }
                if (temp != null) {
                    for (int l = 0; l < floor(n / 2); l++) {
                        for (int j = 0; j < n_core; j++) {
                            boolean b = false;
                            for (int i = 0; i < size - temp.getWcet(0) + 1; i++) {
                                if (w.cpu.CheckTimeSlot(j, i, i + temp.getWcet(0) - 1) &&
                                        w.cpu.numberOfRunningTasksInterval(i, i + temp.getWcet(0) - 1) < max_freq_cores) {
                                    b = true;
                                    w.cpu.SetTaskOnCore(temp.getName() + " F" + l, j, i, i + temp.getWcet(0) - 1);
                                }
                            }
                            if (b) break;

                        }

                    }
                    // Check if the task completed or not
                    if (w.cpu.getEndTimeTask(temp.getName() + " F" + (int) (floor(n / 2) - 1)) == -1) {
                        w.cpu.remove_task(temp.getName());
                        HIv2.remove(temp);
                    } else {
                        HIv2.remove(temp);
                        HIv.remove(temp);
                        w.addTask(temp);
                    }
                }
            } while (HIv2.size() != 0);
            block.add(w);

        } while (HIv.size() != 0);
    }

    public void debug(String file_name) {
        for (window w : block) {
            try {
                w.cpu.debug(file_name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
