import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
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
    int max_freq;

    public hebaFactor(double max_temp, boolean VERBOSE, double n, McDAG dag, CPU cpu,int max_freq, Vertex[] sorted_tasks, Set<String> ov_tasks) {
        this.max_temp = max_temp;
        this.VERBOSE = VERBOSE;
        this.n = n;
        this.dag = dag;
        this.cpu = cpu;
        this.max_freq=max_freq;
        this.sorted_tasks = sorted_tasks;
        this.ov_tasks=ov_tasks;
        n_core=cpu.getN_Cores();
        deadline=cpu.getDeadline();
    }


    public void run(){
        for (Vertex a : sorted_tasks) {
            if (cpu.getEndTimeTask(a.getName() + " R0") != -1) continue;

            int k = 0;
            boolean runnable = true;
            for (Edge e : a.getRcvEdges()) {
                if (e.getSrc().isHighCr()) {
                    for (int i = 0; i < n; i++) {
                        if (ov_tasks.contains(e.getSrc().getName() + " R" + i)) {
                            if (cpu.getEndTimeTask(e.getSrc().getName() + " OV" + i) == -1) {
                                runnable = false;
                            }
                        } else {
                            if (cpu.getEndTimeTask(e.getSrc().getName() + " R" + i) == -1) {
                                runnable = false;
                            }
                        }
                    }
                } else {
                    if (cpu.getEndTimeTask(e.getSrc().getName() + " R0") == -1) {
                        System.out.println("INF ---> Task: " + e.getSrc().getName());
                        runnable = false;
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
            if (!runnable) continue;

            cpu2=cpu;
            try {
                boolean exitFlag = false;
                for (int i = (k == 0 ? 0 : k + 1); i < deadline - a.getRunningTimeLO(max_freq, a.getMin_freq()) + 1; i++) {
                    for (int j = 0; j < n_core; j++) {
                        if (cpu.CheckTimeSlot(j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1) ) {
                            cpu2.SetTaskOnCore(a.getName() + " R0", j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()) - 1);

                            exitFlag = true;
                            break;
                        }
                    }
                    if (exitFlag) break;
                }
                if (cpu.getEndTimeTask(a.getName() + " R0") == -1)
                    throw new Exception("Infeasible!");
            }catch (Exception e){
                continue;
            }
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
            hs_input_creator.run_steady("HotSpot", "powertrace", "A15_" + cpu.getN_Cores() + ".ptrace", cpu.Endtime(-1));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
}
