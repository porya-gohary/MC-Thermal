/*******************************************************************************
 * Copyright © 2020 Porya Gohary
 * Written by Porya Gohary (Email: gohary@ce.sharif.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import static java.lang.Math.ceil;

public class secondApproach {
    //Deadline
    int deadline;
    //Number of CPU Core
    int n_core;
    //Number of Redundancy
    double n;
    //DAG
    McDAG dag;
    CPU cpu;
    String xml_name;
    double landa0;
    int d;
    double v[];
    int freq[];
    String tsp_name;
    String rel_name;
    String benchmark[];
    int benchmark_time[];

    boolean VERBOSE = false;
    String pathSeparator = File.separator;

    //HotSpot location and information
    String hotspot_path = "HotSpot" + pathSeparator + "hotspot";
    String hotspot_config = "HotSpot" + pathSeparator + "configs" + pathSeparator;
    String floorplan = "HotSpot" + pathSeparator + "floorplans" + pathSeparator;
    String powertrace = "HotSpot" + pathSeparator + "powertrace" + pathSeparator;
    String thermaltrace = "HotSpot" + pathSeparator + "thermaltrace" + pathSeparator + "thermal.ttrace";


    Vertex sorted_tasks[];
    double overrun_percent;
    int n_overrun;

    int max_freq;
    int max_freq_cores;
    Set<String> ov_tasks;

    //Applivation Reliability
    double R_app_medina=1;
    double R_app_CNMR=1;

    public secondApproach(int deadline, int n_core, double n, McDAG dag, String xml_name, double landa0, int d, double[] v,
                          int[] freq, String tsp_name, String rel_name, String[] benchmark, int[] benchmark_time, double overrun_percent, int max_freq, int max_freq_cores) {
        this.deadline = deadline;
        this.n_core = n_core;
        this.n = n;
        this.dag = dag;
        this.xml_name = xml_name;
        this.landa0 = landa0;
        this.d = d;
        this.v = v;
        this.freq = freq;
        this.tsp_name = tsp_name;
        this.rel_name = rel_name;
        this.benchmark = benchmark;
        this.benchmark_time = benchmark_time;
        this.overrun_percent = overrun_percent;
        this.max_freq = max_freq;
        this.max_freq_cores = max_freq_cores;
    }

    public void start() throws Exception {
        File rel = new File(rel_name + ".txt");
        reliability();
        feasibility();
        sorted_tasks = sort_vertex(dag.getVertices().toArray(new Vertex[0]).clone());
        ov_tasks = SelectOverrunTasks(dag.getNodes_HI().toArray(new Vertex[0]).clone());
        boolean finish = false;
        while (!finish) {
            boolean f = true;
            try {
                mainScheduling();
            } catch (Exception e) {
                e.printStackTrace();
                boolean x = drop_task();
                if (!x) {
//                    System.out.println("HERE");
                    System.exit(0);
                }
                f = false;
            } finally {
                if (f) finish = true;
            }
        }
        System.out.println("::::::::::");
        System.out.println(xml_name + " Successfully Scheduled! QoS = " + QoS());
        System.out.println("::::::::::");

        try {
//            System.out.println();
            cpu.debug("Sc_mainSCH");
            cpu.Save_Power("OV" + this.overrun_percent + "F0.0", xml_name, "Sc_mainSCH");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void reliability() throws Exception {
        File rel = new File(rel_name + xml_name + ".txt");
        Reliability_cal rc = new Reliability_cal(n, landa0, d, v[v.length - 1], v[0], rel, v, freq, dag);

        File tsp_input = new File(tsp_name + ".txt");
        TSP tsp = new TSP(tsp_input, n_core, v, freq, dag);

        // ------------> SET RELIABILITY AND VOLTAGE OF EACH TASKS <----------
        for (Vertex a : dag.getVertices()) {
            double WCET = (a.getWcet(0) > a.getWcet(1)) ? a.getWcet(0) : a.getWcet(1);
            rc.setT_min(WCET);
            rc.setV_name(a.getName());
            rc.cal();
            if(a.isHighCr()) {
                R_app_CNMR *=rc.R_CNMR;
                R_app_medina *=rc.R_MEDINA;
//                System.out.println(rc.R_MEDINA);
            }
        }
//        System.out.println("----------------------");
//        System.out.println("APPLICATION PoF CTMR = "+ (1.0-R_app_CNMR));
//        System.out.println("APPLICATION Reliability MEDINA= "+ (R_app_medina));
//        System.out.println("APPLICATION PoF MEDINA= "+ (1-R_app_medina));
        //------------> SET MAX ACTIVE CORE FOR EACH TASKS <----------
        tsp.read_TSP_file();
        tsp.cal_TSP_core();
        tsp.debug();


    }

    public void feasibility() throws Exception {

        //Check Feasibility
        CPU cpu1 = new CPU(deadline, n_core, dag, n, max_freq_cores);
        Vertex sorted_HI[] = sort_vertex(dag.getNodes_HI().toArray(new Vertex[0]).clone());
        for (Vertex a : sorted_HI) {

            int k = 0;
            for (Edge e : a.getRcvEdges()) {
                if (cpu1.getEndTimeTask(e.getSrc().getName() + " OV" + (int) (n - 1)) == -1) {
                    System.out.println("INF ---> Task: " + e.getSrc().getName());
                    throw new Exception("Infeasible!");
                } else {
                    if (cpu1.getEndTimeTask(e.getSrc().getName() + " OV" + (int) (n - 1)) > k) {
                        k = cpu1.getEndTimeTask(e.getSrc().getName() + " OV" + (int) (n - 1));
                    }
                }
            }
            for (int l = 0; l < n; l++) {
                if (l <= (ceil(n / 2) - 1)) {
                    boolean exitFlag = false;
//                    System.out.println("~~~~~~ "+k);
                    for (int i = (k == 0 ? 0 : k + 1); i < deadline - a.getRunningTimeLO(max_freq, a.getMin_freq())
                            - a.getRunningTimeHI(max_freq, a.getMin_freq()) + 1; i++) {
                        for (int j = 0; j < n_core; j++) {
                            if (cpu1.CheckTimeSlot(j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) + a.getRunningTimeHI(max_freq, a.getMin_freq()) - 1) &&
                                    (cpu1.maxCoreInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) + a.getRunningTimeHI(max_freq, a.getMin_freq()) - 1) >= a.getTSP_Active()) &&
                                    (cpu1.numberOfRunningTasksInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) + a.getRunningTimeHI(max_freq, a.getMin_freq()) - 1) < a.getTSP_Active())) {
                                cpu1.SetTaskOnCore(a.getName() + " R" + l, j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1);
                                cpu1.SetTaskOnCore(a.getName() + " OV" + l, j, i + a.getRunningTimeLO(max_freq, a.getMin_freq()), i + a.getRunningTimeLO(max_freq, a.getMin_freq()) + a.getRunningTimeHI(max_freq, a.getMin_freq()) - 1);
                                exitFlag = true;
                                break;
                            }
                        }
                        if (exitFlag) break;
                    }
                } else {
                    boolean exitFlag = false;
                    for (int i = (k == 0 ? 0 : k + 1); i < deadline - a.getRunningTimeLO(max_freq, max_freq)
                            - a.getRunningTimeHI(max_freq, max_freq) + 1; i++) {
                        for (int j = 0; j < n_core; j++) {
                            if (cpu1.CheckTimeSlot(j, i, i + a.getRunningTimeLO(max_freq, max_freq) + a.getRunningTimeHI(max_freq, max_freq) - 1) &&
                                    (cpu1.maxCoreInterval(i, i + a.getRunningTimeLO(max_freq, max_freq) + a.getRunningTimeHI(max_freq, max_freq) - 1) >= max_freq_cores) &&
                                    (cpu1.numberOfRunningTasksInterval(i, i + a.getRunningTimeLO(max_freq, max_freq) + a.getRunningTimeHI(max_freq, max_freq) - 1) < max_freq_cores)) {
                                cpu1.SetTaskOnCore(a.getName() + " R" + l, j, i, i + a.getRunningTimeLO(max_freq, max_freq) - 1);
                                cpu1.SetTaskOnCore(a.getName() + " OV" + l, j, i + a.getRunningTimeLO(max_freq, max_freq), i + a.getRunningTimeLO(max_freq, max_freq) + a.getRunningTimeHI(max_freq, max_freq) - 1);
                                exitFlag = true;
                                break;
                            }
                        }
                        if (exitFlag) break;
                    }
                }
                if (cpu1.getEndTimeTask(a.getName() + " R" + l) == -1) {
                    cpu1.debug("TEST");
                    throw new Exception("Infeasible!");
                }
                if (cpu1.getEndTimeTask(a.getName() + " OV" + l) == -1)
                    throw new Exception("Infeasible!");

            }
        }
        cpu1.debug("TEST");

    }

    public void mainScheduling() throws Exception {



        //------------> Main Scheduling <----------
        cpu = null;
        cpu = new CPU(deadline, n_core, dag, n, max_freq_cores);
        for (Vertex a : sorted_tasks) {
            if(!a.isRun()) continue;
            System.out.println(a.getName());
            int k = 0;
            for (Edge e : a.getRcvEdges()) {
                if (e.getSrc().isHighCr()) {
                    for (int i = 0; i < n; i++) {
                        if (ov_tasks.contains(e.getSrc().getName() + " R" + i)) {
                            if (cpu.getEndTimeTask(e.getSrc().getName() + " OV" + i) == -1) {
                                throw new Exception("Infeasible!");
                            }
                        } else {
                            if (cpu.getEndTimeTask(e.getSrc().getName() + " R" + i) == -1) {
                                throw new Exception("Infeasible!");
                            }
                        }
                    }
                } else {
                    if (cpu.getEndTimeTask(e.getSrc().getName() + " R0") == -1) {
                        System.out.println("INF ---> Task: " + e.getSrc().getName());
                        throw new Exception("Infeasible!");
                    }
                }
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

            if (a.isHighCr()) {
                for (int l = 0; l < n; l++) {
                    boolean exitFlag = false;

                    if (ov_tasks.contains(a.getName() + " R" + l)) {
                        if (l <= (ceil(n / 2) - 1)) {
//                            if Task is in HI Mode
                            for (int i = (k == 0 ? 0 : k + 1); i < deadline - a.getRunningTimeLO(max_freq, a.getMin_freq())
                                    - a.getRunningTimeHI(max_freq, a.getMin_freq()) + 1; i++) {
                                for (int j = 0; j < n_core; j++) {
                                    if (cpu.CheckTimeSlot(j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) + a.getRunningTimeHI(max_freq, a.getMin_freq()) - 1) &&
                                            (cpu.maxCoreInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) + a.getRunningTimeHI(max_freq, a.getMin_freq()) - 1) >= a.getTSP_Active()) &&
                                            (cpu.numberOfRunningTasksInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) + a.getRunningTimeHI(max_freq, a.getMin_freq()) - 1) < a.getTSP_Active())) {
                                        cpu.SetTaskOnCore(a.getName() + " R" + l, j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1);
                                        cpu.SetTaskOnCore(a.getName() + " OV" + l, j, i + a.getRunningTimeLO(max_freq, a.getMin_freq()), i + a.getRunningTimeLO(max_freq, a.getMin_freq()) + a.getRunningTimeHI(max_freq, a.getMin_freq()) - 1);
                                        exitFlag = true;
                                        break;
                                    }
                                }
                                if (exitFlag) break;
                            }
                        } else {
                            for (int i = (k == 0 ? 0 : k + 1); i < deadline - a.getRunningTimeLO(max_freq, max_freq)
                                    - a.getRunningTimeHI(max_freq, max_freq) + 1; i++) {
                                for (int j = 0; j < n_core; j++) {
                                    if (cpu.CheckTimeSlot(j, i, i + a.getRunningTimeLO(max_freq, max_freq) + a.getRunningTimeHI(max_freq, max_freq) - 1) &&
                                            (cpu.maxCoreInterval(i, i + a.getRunningTimeLO(max_freq, max_freq) + a.getRunningTimeHI(max_freq, max_freq) - 1) >= max_freq_cores) &&
                                            (cpu.numberOfRunningTasksInterval(i, i + a.getRunningTimeLO(max_freq, max_freq) + a.getRunningTimeHI(max_freq, max_freq) - 1) < max_freq_cores)) {
                                        cpu.SetTaskOnCore(a.getName() + " R" + l, j, i, i + a.getRunningTimeLO(max_freq, max_freq) - 1);
                                        cpu.SetTaskOnCore(a.getName() + " OV" + l, j, i + a.getRunningTimeLO(max_freq, max_freq), i + a.getRunningTimeLO(max_freq, max_freq) + a.getRunningTimeHI(max_freq, max_freq) - 1);
                                        exitFlag = true;
                                        break;
                                    }
                                }
                                if (exitFlag) break;
                            }
                        }
                        if (cpu.getEndTimeTask(a.getName() + " OV" + l) == -1)
                            throw new Exception("Infeasible!");

                    } else {
//                            if Task is in LO Mode
                        if (l <= (ceil(n / 2) - 1)) {
                            for (int i = (k == 0 ? 0 : k + 1); i < deadline - a.getRunningTimeLO(max_freq, a.getMin_freq()) + 1; i++) {
                                for (int j = 0; j < n_core; j++) {
                                    if (cpu.CheckTimeSlot(j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1) &&
                                            (cpu.maxCoreInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1) >= a.getTSP_Active()) &&
                                            (cpu.numberOfRunningTasksInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1) < a.getTSP_Active())) {
                                        cpu.SetTaskOnCore(a.getName() + " R" + l, j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1);

                                        exitFlag = true;
                                        break;
                                    }
                                }
                                if (exitFlag) break;
                            }
                        } else {
                            for (int i = (k == 0 ? 0 : k + 1); i < deadline - a.getRunningTimeLO(max_freq, max_freq) + 1; i++) {
                                for (int j = 0; j < n_core; j++) {
                                    if (cpu.CheckTimeSlot(j, i, i + a.getRunningTimeLO(max_freq, max_freq) - 1) &&
                                            (cpu.maxCoreInterval(i, i + a.getRunningTimeLO(max_freq, max_freq) - 1) >= max_freq_cores) &&
                                            (cpu.numberOfRunningTasksInterval(i, i + a.getRunningTimeLO(max_freq, max_freq) - 1) < max_freq_cores)) {
                                        cpu.SetTaskOnCore(a.getName() + " R" + l, j, i, i + a.getRunningTimeLO(max_freq, max_freq) - 1);

                                        exitFlag = true;
                                        break;
                                    }
                                }
                                if (exitFlag) break;
                            }
                        }
                        if (cpu.getEndTimeTask(a.getName() + " R" + l) == -1)
                            throw new Exception("Infeasible!");
                    }

                }
            } else {
                //For LO-Critical Tasks
                if (a.isRun()) {
                    boolean exitFlag = false;
                    for (int i = (k == 0 ? 0 : k + 1); i < deadline - a.getRunningTimeLO(max_freq, a.getMin_freq()) + 1; i++) {
                        for (int j = 0; j < n_core; j++) {
                            if (cpu.CheckTimeSlot(j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1) &&
                                    (cpu.maxCoreInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1) >= a.getTSP_Active()) &&
                                    (cpu.numberOfRunningTasksInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1) < a.getTSP_Active())) {
                                cpu.SetTaskOnCore(a.getName() + " R0", j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1);

                                exitFlag = true;
                                break;
                            }
                        }
                        if (exitFlag) break;
                    }
                    if (cpu.getEndTimeTask(a.getName() + " R0") == -1)
                        throw new Exception("Infeasible!");
                }
            }


        }
        System.out.println("Overrun Task Set ==> " + ov_tasks);
        System.out.println("<><><><><><><><><>");
    }


    public Vertex[] sort_vertex(Vertex v[]) {
        Arrays.sort(v);
        Collections.reverse(Arrays.asList(v));
        //Show Sorted Vortex Array
        return v;
    }

    public Set<String> SelectOverrunTasks(Vertex v[]) {
        Set<String> ov_tasks = new HashSet<String>();
        Set<String> temp = new HashSet<String>();

        for (Vertex a : v) {
            for (int i = 0; i < n; i++) {
                temp.add(a.getName() + " R" + i);
            }
        }

        n_overrun = (int) (overrun_percent * temp.size());
        String[] temp2 = temp.stream().toArray(String[]::new).clone();

        //Select Tasks
        Random ov = new Random();
        int o;
        for (int i = 0; i < n_overrun; i++) {
            do {
                o = ov.nextInt(temp2.length);
            } while (ov_tasks.contains(temp2[o]));
            ov_tasks.add(temp2[o]);
        }
        return ov_tasks;
    }

    public boolean drop_task() {
        for (int i = sorted_tasks.length - 1; i >= 0; i--) {
            if (!sorted_tasks[i].isHighCr() && sorted_tasks[i].isRun()) {
                sorted_tasks[i].setRun(false);
                System.out.println("■■■  DROP TASK " + sorted_tasks[i].getName());
                return true;
            }
        }
        return false;
    }

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
        double temp[]= new double[4];
        double Max=0;
        double Avg=0;

        hotspot_config = "HotSpot" + pathSeparator + "configs" + pathSeparator;
        floorplan = "HotSpot" + pathSeparator + "floorplans" + pathSeparator;
        powertrace = "HotSpot" + pathSeparator + "powertrace" + pathSeparator;
        HotSpot hotSpot = new HotSpot(hotspot_path, VERBOSE);
        HS_input_creator hs_input_creator = new HS_input_creator(cpu);
        try {
            hs_input_creator.Save("HotSpot", "powertrace", "A15_" + cpu.getN_Cores() + ".ptrace", cpu.Endtime(-1));
        } catch (IOException e) {
            e.printStackTrace();
        }

        hotspot_config += "hotspot_" + cpu.getN_Cores() + ".config";
        floorplan += "A15_" + cpu.getN_Cores() + ".flp";
        powertrace += "A15_" + cpu.getN_Cores() + ".ptrace";
        hotSpot.run(hotspot_config, floorplan, powertrace, thermaltrace);

        String mFolder = "HotSpot";
        String sFolder = "thermaltrace";
        String filename = "thermal.ttrace";
        File thermalFile = null;
        double MaxDiff=0;
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

                if(getMax(value)>Max) Max = getMax(value);
                Avg+=getMax(value);

                diff += getMax(value) - getMin(value);
                if(getMax(value) - getMin(value)>MaxDiff) MaxDiff =getMax(value) - getMin(value);

            }
            Reader.close();
            if (VERBOSE) {
                System.out.println("Max. Different= " + MaxDiff);
                System.out.println("Avg. Different= " + (diff / cpu.Endtime(-1)));
            }
            //Temperature Results [0] Avg. Diff. [1] Max. Diff. [2] Max. Temp. [3] Avg. Temp.
            temp[0]=(diff / cpu.Endtime(-1));
            temp[1]=MaxDiff;
            temp[2]= Max;
            temp[3]=Avg/ cpu.Endtime(-1);
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

    public CPU getCpu() {
        return cpu;
    }


    public McDAG getDag() {
        return dag;
    }
}
