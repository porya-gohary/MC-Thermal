import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;

import static java.lang.Math.ceil;

public class optimal {
    //Deadline
    int deadline;
    //Number of CPU Core
    int n_core = 4;
    //Number of Redundancy
    double n;
    //DAG
    McDAG dag;
    CPU cpu;
    String xml_name;

    //Faulty Tasks
    Set<Vertex> faults;
    Vertex faults_array[];

    //Number of fault
    int n_fault = 0;
    double fault_percent;

    Vertex v[];

    //Check feasible
    boolean f_feasible = true;

    //Hi Critical Vertex
    Vertex HIv[];

    int max_freq = 1200;

    double overrun_percent;
    int n_overrun;

    Vertex sorted_tasks[];

    boolean VERBOSE = false;

    String pathSeparator = File.separator;

    //HotSpot location and information
    String hotspot_path = "HotSpot" + pathSeparator + "hotspot";
    String hotspot_config = "HotSpot" + pathSeparator + "configs" + pathSeparator;
    String floorplan = "HotSpot" + pathSeparator + "floorplans" + pathSeparator;
    String powertrace = "HotSpot" + pathSeparator + "powertrace" + pathSeparator;
    String thermaltrace = "HotSpot" + pathSeparator + "thermaltrace" + pathSeparator + "thermal.ttrace";
    private boolean f;

    public optimal(int deadline, int n_core, double n, McDAG dag, String xml_name, double fault_percent, boolean VERBOSE) {
        this.deadline = deadline;
        this.n_core = n_core;
        this.n = n;
        this.dag = dag;
        this.xml_name = xml_name;
        this.fault_percent = fault_percent;
        this.VERBOSE = VERBOSE;
        n_fault = (int) (fault_percent * dag.getNodes_HI().size() / 100);
        v = dag.getVertices().toArray(new Vertex[0]);
    }

    public void start() throws Exception {
        //Sort Tasks
        sorted_tasks = sort_tasks(dag.getVertices().toArray(new Vertex[0]).clone());
        reset_schedule();
        feasibility();
        reset_schedule();
        clean_fault();
        clean_sch();
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter("Temperature.txt"));
        for (int i = 0; i < dag.getVertices().size(); i++) {
            String task_sel = "D0N" + i;
            if (VERBOSE) System.out.println("-->  " + task_sel);
            if (dag.getNodebyName(task_sel).isHighCr()) {
                for (int j = 0; j < n; j++) {
                    while (true) {
                        reset_schedule();
                        clean_sch();
                        try {
                            mainScheduling(dag.getNodebyName(task_sel), j);
                            double[] T = balanceCalculator();
                            outputWriter.write(T[2] + "\n");
                            outputWriter.flush();
                        } catch (Exception e) {
                            System.out.println("Exception --> END");
                            if (VERBOSE) e.printStackTrace();
                            break;
                        }
                    }
                }
            } else {
                while (true) {
                    try {
                        reset_schedule();
                        clean_sch();
                        mainScheduling(dag.getNodebyName(task_sel), 0);
                        double[] T = balanceCalculator();
                        outputWriter.write(T[2] + "\n");
                        outputWriter.flush();
                    } catch (Exception e) {
                        if (VERBOSE) e.printStackTrace();
                        break;
                    }
                }
            }

            outputWriter.close();
        }


    }

    //Main Scheduling of System
    public void mainScheduling(Vertex optTask, int n_rep) throws Exception {
        Vertex t;
        cpu = new CPU(deadline, n_core, dag);
        do {
            t = dag.getNodebyName(get_task(true));
            if (t == null) break;
            int startTime = 0;
            if (t.isHighCr()) {
                for (int k = 0; k < n; k++) {
                    startTime = 0;
                    for (Edge e : t.getRcvEdges()) {
                        if (cpu.getEndTimeTask(e.getSrc().getName() + " OV" + (int) (n - 1)) > startTime) {
                            startTime = cpu.getEndTimeTask(e.getSrc().getName() + " OV" + (int) (n - 1)) + 1;
                        }
                    }
                    if (t == optTask && k == n_rep) {
                        if (startTime > t.getOpt(n_rep)) {
                            t.setOpt(startTime, n_rep);
                        } else {
                            t.setOpt(t.getOpt(n_rep) + 1, n_rep);
                            startTime = t.getOpt(n_rep);
                        }
                    }
                    boolean scheduled = false;
                    for (int i = startTime; i < deadline - t.getRunningTimeLO(max_freq, t.getMin_freq()); i++) {
                        for (int j = 0; j < n_core; j++) {
                            if (cpu.CheckTimeSlot(j, i, i + t.getRunningTimeHI(max_freq, t.getMin_freq()))) {
                                cpu.SetTaskOnCore(t.getName() + " R" + k, j, i, i + t.getRunningTimeLO(max_freq, t.getMin_freq()) - 1);
                                cpu.SetTaskOnCore(t.getName() + " OV" + k, j, i + t.getRunningTimeLO(max_freq, t.getMin_freq()), i + t.getRunningTimeHI(max_freq, t.getMin_freq()) - 1);
                                if (VERBOSE)
                                    System.out.println(">>> " + (t.getName() + " R" + k) + " S: " + i + " E: " + (i + t.getRunningTimeHI(max_freq, t.getMin_freq()) - 1));
                                scheduled = true;
                                if (t == optTask && k == n_rep) {
                                    if (i > t.getOpt(n_rep)) {
                                        t.setOpt(i , n_rep);
                                    }
                                }
                                break;
                            }
                        }
                        if (scheduled) break;
                    }
                    if (!scheduled)
                        throw new Exception("Infeasible!");
                }
                t.setDone(true);
            } else {
                startTime = 0;
                for (Edge e : t.getRcvEdges()) {
                    if (e.getSrc().isHighCr()) {
                        if (cpu.getEndTimeTask(e.getSrc().getName() + " OV" + (int) (n - 1)) > startTime) {
                            startTime = cpu.getEndTimeTask(e.getSrc().getName() + " OV" + (int) (n - 1)) + 1;
                        }
                    } else {
                        if (cpu.getEndTimeTask(e.getSrc().getName() + " R" + 1) > startTime) {
                            startTime = cpu.getEndTimeTask(e.getSrc().getName() + " R" + 1) + 1;
                        }
                    }
                }
                boolean scheduled = false;

                if (t == optTask) {
                    if (startTime > t.getOpt(n_rep)) {
                        t.setOpt(startTime , n_rep);
                    } else {
                        t.setOpt(t.getOpt(n_rep) + 1, n_rep);
                        startTime = t.getOpt(n_rep);
                    }
                }
                for (int i = startTime; i < deadline - t.getRunningTimeLO(max_freq, t.getMin_freq()); i++) {
                    for (int j = 0; j < n_core; j++) {
                        if (cpu.CheckTimeSlot(j, i, i + t.getRunningTimeLO(max_freq, t.getMin_freq()))) {
                            cpu.SetTaskOnCore(t.getName() + " R" + 1, j, i, i + t.getRunningTimeLO(max_freq, t.getMin_freq()) - 1);
                            if (VERBOSE)
                                System.out.println(">>> " + (t.getName() + " R" + 0) + " S: " + i + " E: " + (i + t.getRunningTimeLO(max_freq, t.getMin_freq()) - 1));

                            if (t == optTask) {
                                if (i > t.getOpt(n_rep)) {
                                    t.setOpt(i , n_rep);
                                }
                            }
                            scheduled = true;
                            break;
                        }
                    }
                    if (scheduled) break;
                }
                if (!scheduled)
                    throw new Exception("Infeasible!");

                t.setDone(true);
            }


        } while (t != null);
    }


    //Check Feasibility of System
    public void feasibility() throws Exception {
        Vertex t;
        CPU cpu1 = new CPU(deadline, n_core, dag);
        do {
            t = dag.getNodebyName(get_task(false));
            if (t == null) break;
            int startTime = 0;
            for (Edge e : t.getRcvEdges()) {
                if (cpu1.getEndTimeTask(e.getSrc().getName() + " OV" + (int) (ceil(n / 2) - 1)) > startTime) {
                    startTime = cpu1.getEndTimeTask(e.getSrc().getName() + " OV" + (int) (ceil(n / 2) - 1)) + 1;
                }
            }
            for (int k = 0; k < n; k++) {
                boolean scheduled = false;
                for (int i = startTime; i < deadline - t.getWcet(1); i++) {
                    for (int j = 0; j < n_core; j++) {
                        if (cpu1.CheckTimeSlot(j, i, i + t.getWcet(1))) {
                            cpu1.SetTaskOnCore(t.getName() + " R" + k, j, i, i + t.getWcet(0) - 1);
                            cpu1.SetTaskOnCore(t.getName() + " OV" + k, j, i + t.getWcet(0), i + t.getWcet(1) - 1);
                            scheduled = true;
                            break;
                        }
                    }
                    if (scheduled) break;
                }
                if (!scheduled)
                    throw new Exception("Infeasible!");

            }
            t.setDone(true);


        } while (t != null);
        f_feasible = false;
    }


    //get the Task that must be run
    public String get_task(boolean LO) {
        String x = null;
        for (Vertex a : sorted_tasks) {
            if (!a.isHighCr() && !LO) continue;
            else if (LO && !a.isRun()) continue;
            boolean run_flag = true;
            if (a.isDone()) continue;
            for (Edge e : a.getRcvEdges()) {
                if (!e.getSrc().isDone()) {
                    run_flag = false;
                    break;
                }
            }
            if (!run_flag) continue;
            else {
                x = a.getName();
                break;
            }
        }
        return x;
    }


    //Sorting Tasks from big LPL to small LPL
    public Vertex[] sort_tasks(Vertex v[]) {
        Arrays.sort(v);
        Collections.reverse(Arrays.asList(v));

        if (VERBOSE) {
            //Show Sorted Vortex Array
            System.out.println("---------------");
            System.out.println("Sorted Tasks:");
            for (Vertex a : v) {
                System.out.println(a.getName() + "  " + a.getWcet(0));
            }
            System.out.println("---------------");
        }
        return v;
    }

    //Drop LO Tasks That cannot schedule in offline phase
    public boolean drop_task() {
        for (int i = sorted_tasks.length - 1; i >= 0; i--) {
            if (!sorted_tasks[i].isHighCr() && sorted_tasks[i].isRun()) {
                sorted_tasks[i].setRun(false);
                if (VERBOSE) System.out.println("■■■  DROP TASK " + sorted_tasks[i].getName());
                return true;
            }
        }
        return false;
    }

    //Reset condition of scheduled tasks
    public void reset_schedule() {
        for (Vertex a : sorted_tasks) {
            a.setDone(false);
            a.setScheduled(0);
        }
    }

    // QoS Calculator
    public double QoS() {
        double QoS = 0;
        for (int i = 0; i < sorted_tasks.length; i++) {
            if (!sorted_tasks[i].isHighCr() && sorted_tasks[i].isRun()) QoS++;
        }
        QoS = QoS / (sorted_tasks.length - dag.getNodes_HI().size());
        return QoS;
    }

    public double[] balanceCalculator() {
        //Temperature Results [0] Avg. Diff. [1] Max. Diff. [2] Max. Temp. [3] Avg. Temp.
        double temp[] = new double[4];
        double Max = 0;
        double Avg = 0;

        hotspot_config = "HotSpot" + pathSeparator + "configs" + pathSeparator;
        floorplan = "HotSpot" + pathSeparator + "floorplans" + pathSeparator;
        powertrace = "HotSpot" + pathSeparator + "powertrace" + pathSeparator;
        HotSpot hotSpot = new HotSpot(hotspot_path, VERBOSE);
        HS_input_creator hs_input_creator = new HS_input_creator(cpu);
        try {
            hs_input_creator.run_steady("HotSpot", "powertrace", "A15_" + cpu.getN_Cores() + ".ptrace", cpu.Endtime(-1));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String mFolder = "HotSpot";
        String sFolder = "thermaltrace";
        String filename = "thermal.ttrace";
        File thermalFile = null;
        double MaxDiff = 0;
        try {
            thermalFile = new File(mFolder + pathSeparator + sFolder + pathSeparator + filename);
            Scanner Reader = new Scanner(thermalFile);
            //Reader.hasNextLine()
            double diff = 0;
            Reader.nextLine();
            for (int j = 0; j < cpu.Endtime(-1); j++) {
                String data = Reader.nextLine();
                String Sdatavalue[] = data.split("\t");
                double value[] = new double[cpu.getN_Cores()];
                for (int i = 0; i < cpu.getN_Cores(); i++) {
                    value[i] = Double.parseDouble(Sdatavalue[i]);
                }

                if (getMax(value) > Max) Max = getMax(value);
                Avg += getMax(value);

                diff += getMax(value) - getMin(value);
                if (getMax(value) - getMin(value) > MaxDiff) MaxDiff = getMax(value) - getMin(value);

            }
            Reader.close();

            //Temperature Results [0] Avg. Diff. [1] Max. Diff. [2] Max. Temp. [3] Avg. Temp.
            temp[0] = (diff / cpu.Endtime(-1));
            temp[1] = MaxDiff;
            temp[2] = Max;
            temp[3] = Avg / cpu.Endtime(-1);
            if (VERBOSE) {
                System.out.println("Max. = " + Max);
                System.out.println("Avg.= " + temp[3]);
            }
        } catch (FileNotFoundException e) {
            if (VERBOSE) {
                System.out.println("An error occurred in Reading Thermal Trace File.");
                System.out.println("Path: " + thermalFile.getAbsolutePath());
                e.printStackTrace();
            }
        }
        return temp;
    }

    // Method for getting the minimum value
    public double getMin(double[] inputArray) {
        double minValue = inputArray[0];
        for (int i = 1; i < inputArray.length; i++) {
            if (inputArray[i] < minValue) {
                minValue = inputArray[i];
            }
        }
        return minValue;
    }


    //Method for getting the maximum value
    public double getMax(double[] inputArray) {
        double maxValue = inputArray[0];
        for (int i = 1; i < inputArray.length; i++) {
            if (inputArray[i] > maxValue) {
                maxValue = inputArray[i];
            }
        }
        return maxValue;
    }

    public void clean_sch() {
        for (Vertex a : v) {
            a.setScheduled(0);
            a.setInjected_fault(0);
        }
    }

    public void clean_fault() {
        for (Vertex a : v) {
            a.setInjected_fault(0);

        }
    }


    public CPU getCpu() {
        return cpu;
    }


    public McDAG getDag() {
        return dag;
    }

    public boolean schedule() {

        return false;
    }

}
