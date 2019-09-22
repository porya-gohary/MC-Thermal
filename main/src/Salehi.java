import java.io.IOException;
import java.util.*;

import static java.lang.Math.ceil;

public class Salehi {
    //DAG
    McDAG dag;
    String xml_name;
    //Number of Core
    int n_core;
    int deadline;
    String benchmark[];
    int benchmark_time[];
    double n;
    Vertex v[];

    CPU cpu, block_cpu;

    //Faulty Tasks
    Set<Vertex> faults;
    Vertex faults_array[];

    //Number of fault
    int n_fault = 0;
    double fault_percent;

    //Check feasible
    boolean f_feasible = true;

    //Hi Critical Vertex
    Vertex HIv[];


    public Salehi(McDAG dag, int n_core, int deadline, double n, String xml_name, int n_fault, double fault_percent) throws Exception {
        this.dag = dag;
        this.n_core = n_core;
        this.deadline = deadline;
        this.n = n;
        v = dag.getVertices().toArray(new Vertex[0]);
        this.n_fault = n_fault;
        this.xml_name = xml_name;
        this.fault_percent = fault_percent;
        sort_vertex();
        clean_sch();
        check_feasible();
        clean_sch();
        mScheduling();
    }

    public void check_feasible() throws Exception {
        cpu = new CPU(deadline, n_core, dag);
        int j = 0;
        for (int x = 0; x < v.length; x++) {
            for (Vertex a : v) {
                if (a.isHighCr()) {
                    if (a.getScheduled() == ceil(n / 2)) continue;
                    //For Add Extra Copy for HI-Critical Tasks
                    for (int l = 0; l < ceil(n / 2); l++) {
                        j = 0;

                        for (int i = 0; i < deadline; i++) {
                            if (!a.check_runnable(cpu.get_Running_Tasks(i), n)) continue;
                            boolean CPU_runnable = true;
                            for (Edge e : a.getRcvEdges()) {
                                if (cpu.getEndTimeTask(e.getSrc().getName() + " CR" + (int) (ceil(n / 2) - 1)) > i) {
                                    CPU_runnable = false;
                                }
                                if ((cpu.getEndTimeTask(e.getSrc().getName() + " CO" + (int) (ceil(n / 2) - 1)) > i) && (cpu.getEndTimeTask(e.getSrc().getName() + " CO" + (int) (ceil(n / 2) - 1)) != -1)) {
                                    CPU_runnable = false;
                                }

                            }
                            if (!CPU_runnable) {
                                //  System.out.println("RUN!");
                                continue;
                            }
                            boolean run = true;
                            for (int k = 0; k < (int) (ceil(n / 2)); k++) {
                                if (!cpu.CheckTimeSlot(j + k, i, i + a.getWcet(1))) {
                                    run = false;
                                }
                            }
                            if (run) {
                                for (int k = 0; k < (int) (ceil(n / 2)); k++) {
                                    cpu.SetTaskOnCore(a.getName() + " CR" + k, (j + k), i, i + a.getWcet(0) - 1);
                                    cpu.SetTaskOnCore(a.getName() + " CO" + k, (j + k), i + a.getWcet(0), i + a.getWcet(1) - 1);
                                    a.setScheduled(a.getScheduled() + 1);
                                    //System.out.println(a.getScheduled()+"   "+n+"   > "+k);
                                }
                                break;
                            }
                            if (j < (n_core - 1)) {
                                j++;
                                i--;
                            } else {
                                j = 0;
                            }
                        }
                        if (a.getScheduled() == (int) (ceil(n / 2))) break;
                    }

                } else {
                    //One Replica For LO-Critical Tasks
                    if (a.getScheduled() == 1) continue;
                    for (int i = 0; i < deadline; i++) {
                        if (!a.check_runnable(cpu.get_Running_Tasks(i), n)) continue;
                        boolean CPU_runnable = true;

                        for (Edge e : a.getRcvEdges()) {
                            if (cpu.getEndTimeTask(e.getSrc().getName() + " CR" + (int) (ceil(n / 2) - 1)) > i) {
                                CPU_runnable = false;
                            }
                            if ((cpu.getEndTimeTask(e.getSrc().getName() + " CO" + (int) (ceil(n / 2) - 1)) > i) && (cpu.getEndTimeTask(e.getSrc().getName() + " CO" + (int) (ceil(n / 2) - 1)) != -1)) {
                                CPU_runnable = false;
                            }

                        }


                        if (!CPU_runnable) continue;
                        if (cpu.CheckTimeSlot(j, i, i + a.getWcet(0))) {
                            cpu.SetTaskOnCore(a.getName() + " CR" + (int) (ceil(n / 2) - 1), j, i, i + a.getWcet(0));
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
        for (Vertex a : v) {
            if (a.isHighCr()) {
                if (a.getScheduled() < (int) (ceil(n / 2) - 1)) throw new Exception("Infeasible!");
            } else {
                if (a.getScheduled() != 1) throw new Exception("Infeasible!");
            }
        }
        //System.out.println(dag.getNodes_HI().size());
        inject_fault(dag.getNodes_HI().size());
        //make_blocks(200);
        f_feasible = false;


    }


    public void mScheduling() throws Exception {
        cpu = new CPU(deadline, n_core, dag);
        int j = 0;
        for (int x = 0; x < v.length; x++) {
            for (Vertex a : v) {
                if (a.isHighCr()) {
                    if (a.getScheduled() == ceil(n / 2)) continue;
                    //For Add Extra Copy for HI-Critical Tasks
                    for (int l = 0; l < ceil(n / 2); l++) {
                        j = 0;

                        for (int i = 0; i < deadline; i++) {
                            if (!a.check_runnable(cpu.get_Running_Tasks(i), n)) continue;
                            boolean CPU_runnable = true;
                            for (Edge e : a.getRcvEdges()) {
                                if (cpu.getEndTimeTask(e.getSrc().getName() + " CR" + (int) (ceil(n / 2) - 1)) > i) {
                                    CPU_runnable = false;
                                }
                                if ((cpu.getEndTimeTask(e.getSrc().getName() + " CO" + (int) (ceil(n / 2) - 1)) > i) && (cpu.getEndTimeTask(e.getSrc().getName() + " CO" + (int) (ceil(n / 2) - 1)) != -1)) {
                                    CPU_runnable = false;
                                }

                            }
                            if (!CPU_runnable) {
                                //  System.out.println("RUN!");
                                continue;
                            }
                            boolean run = true;
                            for (int k = 0; k < (int) (ceil(n / 2)); k++) {
                                if (!cpu.CheckTimeSlot(j + k, i, i + a.getWcet(1))) {
                                    run = false;
                                }
                            }
                            if (run) {
                                for (int k = 0; k < (int) (ceil(n / 2)); k++) {
                                    cpu.SetTaskOnCore(a.getName() + " CR" + k, (j + k), i, i + a.getWcet(0) - 1);
                                    //cpu.SetTaskOnCore(a.getName() + " CO" + k, (j+k), i +a.getWcet(0),i+ a.getWcet(1)-1);
                                    a.setScheduled(a.getScheduled() + 1);
                                    //System.out.println(a.getScheduled()+"   "+n+"   > "+k);
                                }
                                break;
                            }
                            if (j < (n_core - 1)) {
                                j++;
                                i--;
                            } else {
                                j = 0;
                            }
                        }
                        if (a.getScheduled() == (int) (ceil(n / 2))) break;
                    }

                } else {
                    //One Replica For LO-Critical Tasks
                    if (a.getScheduled() == 1) continue;
                    for (int i = 0; i < deadline; i++) {
                        if (!a.check_runnable(cpu.get_Running_Tasks(i), n)) continue;
                        boolean CPU_runnable = true;

                        for (Edge e : a.getRcvEdges()) {
                            if (cpu.getEndTimeTask(e.getSrc().getName() + " CR" + (int) (ceil(n / 2) - 1)) > i) {
                                CPU_runnable = false;
                            }
                            if ((cpu.getEndTimeTask(e.getSrc().getName() + " CO" + (int) (ceil(n / 2) - 1)) > i) && (cpu.getEndTimeTask(e.getSrc().getName() + " CO" + (int) (ceil(n / 2) - 1)) != -1)) {
                                CPU_runnable = false;
                            }

                        }


                        if (!CPU_runnable) continue;
                        if (cpu.CheckTimeSlot(j, i, i + a.getWcet(0))) {
                            cpu.SetTaskOnCore(a.getName() + " CR" + (int) (ceil(n / 2) - 1), j, i, i + a.getWcet(0));
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

        inject_fault(n_fault);
        try {
            cpu.debug("Salehi-mainSCH");
            cpu.Save_Power("OV" + "0.0" + "F" + fault_percent, xml_name, "Salehi-mainSCH");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void inject_fault(int number_of_fault) throws Exception {
        faults = new HashSet<Vertex>();

        Set<Vertex> nodesHI = new HashSet<Vertex>();
        for (Vertex a : dag.getVertices()) {
            if (a.getWcet(1) != 0)
                nodesHI.add(a);
        }
        HIv = nodesHI.toArray(new Vertex[0]);

        int f = 0;
        Random fault = new Random();
//        System.out.println("...................-->> "+number_of_fault);
        for (int i = 0; i < number_of_fault; i++) {
            do {
                f = fault.nextInt(HIv.length);
            } while (HIv[f].getInjected_fault() != 0);
            if (!f_feasible) System.out.println("↯↯ Fault injected To  " + HIv[f].getName());
            dag.getNodebyName(HIv[f].getName()).setInjected_fault(dag.getNodebyName(HIv[f].getName()).getInjected_fault() + 1);
            faults.add(dag.getNodebyName(HIv[f].getName()));
        }
        faults_array = faults.toArray(new Vertex[0]);
//        Arrays.sort(faults_array);
//        for(Vertex a:faults_array){
//            System.out.println(a.getName()+"  ==>>  "+(a.getWcet(0)+a.getWcet(1)));
//        }
        for (int i = 0; i < faults_array.length; i++) {
            int t = cpu.getEndTimeTask(faults_array[i].getName() + " CR" + (int) (ceil(n / 2) - 1));
            make_blocks(t);
        }
    }


    public void make_blocks(int time) throws Exception {
        time++;

        Set<Vertex> block = new HashSet<Vertex>();
        //Set<Vertex> faults=new HashSet<Vertex>();
        for (Vertex a : HIv) {
            if (!a.check_runnable(cpu.get_Running_Tasks(time), n)) {
                //System.out.println("1  "+a.getName());
                continue;
            }
            if (faults.contains(a) && a.getScheduled() == n) {
                // System.out.println("2  "+a.getName());
                continue;
            }
            if ((cpu.getEndTimeTask(" CR" + (int) (ceil(n / 2) - 1)) < time) && ((cpu.getEndTimeTask(" CR" + (int) (ceil(n / 2) - 1)) != -1))) {
                //System.out.println(cpu.getEndTimeTask(" CR"+(int)(ceil(n/2)-1)) + "  < "+ time);
                //  System.out.println("3  "+a.getName());
                continue;
            }
            block.add(a);
        }

        int blk_size = Math.min(n_core, block.size());

        Vertex blk[] = block.toArray(new Vertex[0]);
        if (blk.length > 1) {
            Arrays.sort(blk);
            Collections.reverse(Arrays.asList(blk));
        }
//        for(Vertex a:blk){
//            System.out.println(a.getName());
//        }

//        if(!stackTraceElements[2].getClassName().equals("Safe_Start_Time")) {
//
//        }
        if (blk.length == 0) return;
        if (f_feasible) cpu.Task_Shifter(time, blk[0].getWcet(1));
        else cpu.Task_Shifter(time, blk[0].getWcet(0));

        for (int i = 0; i < blk_size; i++) {
            //UnCompleted for N>3
            cpu.SetTaskOnCore(blk[i].getName() + " F", i, time + 1, time + blk[i].getWcet(0));
            if (f_feasible)
                cpu.SetTaskOnCore(blk[i].getName() + " O", i, time + blk[i].getWcet(0) + 1, time + blk[i].getWcet(1));
            dag.getNodebyName(blk[i].getName()).setScheduled(dag.getNodebyName(blk[i].getName()).getScheduled() + 1);
        }


    }

    public void sort_vertex() {
        if (v.length > 1) {
            Arrays.sort(v);
            Collections.reverse(Arrays.asList(v));
        }
        //Show Sorted Vortex Array
//        for(Vertex a:v){
//            System.out.println(a.getName()+"  ==>>  "+(a.getWcet(0)+a.getWcet(1)));
//        }
    }

    public void clean_sch() {
        for (Vertex a : v) {
            a.setScheduled(0);
            a.setInjected_fault(0);
        }
    }
}
