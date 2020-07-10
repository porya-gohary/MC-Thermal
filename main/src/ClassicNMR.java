import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static java.lang.Math.ceil;

public class ClassicNMR  {

    McDAG dag;
    int n_core;
    int deadline;
    String benchmark[];
    int benchmark_time[];
    double n;
    Vertex v[];
    int n_overrun=0;

    double overrun_percent;
    CPU cpu;
    String xml_name;

    boolean VERBOSE = false;

    String pathSeparator = File.separator;

    //HotSpot location and information
    String hotspot_path = "HotSpot" + pathSeparator + "hotspot";
    String hotspot_config = "HotSpot" + pathSeparator + "configs" + pathSeparator;
    String floorplan = "HotSpot" + pathSeparator + "floorplans" + pathSeparator;
    String powertrace = "HotSpot" + pathSeparator + "powertrace" + pathSeparator;
    String thermaltrace = "HotSpot" + pathSeparator + "thermaltrace" + pathSeparator + "thermal.ttrace";

    public ClassicNMR(McDAG dag, int n_core, int deadline, String[] benchmark, int[] benchmark_time, double n,int n_overrun,double overrun_percent,String xml_name ) throws Exception {
        this.dag = dag;
        this.n_core = n_core;
        this.deadline = deadline;
        this.benchmark = benchmark;
        this.benchmark_time = benchmark_time;
        this.n = n;
        this.n_overrun=n_overrun;
        this.xml_name=xml_name;
        this.overrun_percent=overrun_percent;
        v=dag.getVertices().toArray(new Vertex[0]);
        this.sort_vertex();
        this.clean_sch();
        this.check_feasible();
        this.clean_sch();
        this.clean_fault();
        this.mScheduling();
        //cpu.power_results();
    }

    public void check_feasible() throws Exception {
        cpu=new CPU(deadline,n_core,dag);
        int j=0;
        for (int x = 0; x < v.length; x++) {
            for(Vertex a: v){
                if(a.isHighCr()) {

                    if(a.getScheduled()==n) continue;
                    //System.out.println("SCH  "+a.getScheduled()+"   "+ n);
                    for (int l = 0; l < n; l++) {
                        j=0;
                        for (int i = 0; i < deadline; i++) {
                            if(!a.check_runnable(cpu.get_Running_Tasks(i),n)) continue;
                            boolean CPU_runnable=true;
                            for(Edge e: a.getRcvEdges()){
                                if (cpu.getEndTimeTask(e.getSrc().getName()+" CR"+(int)(n-1)) == -1)  CPU_runnable = false;
                                if(cpu.getEndTimeTask(e.getSrc().getName()+" CR"+(int)(n-1))>i){
                                    CPU_runnable=false;
                                }
                                if((cpu.getEndTimeTask(e.getSrc().getName()+" CO"+(int)(n-1))>i) &&(cpu.getEndTimeTask(e.getSrc().getName()+" CO"+(int)(n-1))!=-1) ){
                                    CPU_runnable=false;
                                }

                            }
                            if(!CPU_runnable) {
                              //  System.out.println("RUN!");
                                continue;
                            }

                            boolean run=true;
                            for (int k = 0; k < n; k++) {
                                if(!cpu.CheckTimeSlot(j+k, i,i+a.getWcet(1))){
                                    run=false;
                                }
                            }
                            if(run){
                                for (int k = 0; k < n; k++) {
                                    cpu.SetTaskOnCore(a.getName() + " CR" + k, (j+k), i, i + a.getWcet(0) - 1);
                                    cpu.SetTaskOnCore(a.getName() + " CO" + k, (j+k), i +a.getWcet(0),i+ a.getWcet(1)-1);
                                    a.setScheduled(a.getScheduled() + 1);
                                    //System.out.println(a.getScheduled()+"   "+n+"   > "+k);
                                }
                                break;
                            }
                            if (j<(n_core-1)){
                                j++;
                                i--;
                            }
                            else {
                                j = 0;
                            }
                        }
                        if(a.getScheduled()==n) break;
                    }
                }else{
                    if(a.getScheduled()==1) continue;
                    for (int i = 0; i < deadline; i++) {
                        if (!a.check_runnable(cpu.get_Running_Tasks(i), n)) continue;

                        if (cpu.CheckTimeSlot(j, i, i + a.getWcet(0))){
                            cpu.SetTaskOnCore(a.getName() + " CR0", j, i, i + a.getWcet(0));
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
        for(Vertex a: v){
            if(a.isHighCr()) {
                if(a.getScheduled()<n) throw new Exception("Infeasible!");
            }else{
                if(a.getScheduled()!=1) throw new Exception("Infeasible!");
            }
        }

    }

    public void mScheduling() throws Exception {
        Random overrun= new Random();
        int o = 0;
        String ov_name;
        ArrayList<String> ov=new ArrayList<String>();
        for (int i = 0; i < n_overrun; i++) {
            do{
                do{
                    o=overrun.nextInt(n_core);
                }while(cpu.Endtime(o)==0);
                ov_name=cpu.getRunningTaskWithReplica(o,overrun.nextInt(cpu.Endtime(o)));
            }while(ov_name==null || dag.getNodebyName(ov_name.split(" ")[0]).getWcet(1)==0 || ov_name.contains("CO")||ov.contains(ov_name));
            ov.add(ov_name);
            System.out.println("|||| OV |||||   "+ov_name+"  Core: "+o);
        }

        cpu=new CPU(deadline,n_core,dag);
        int j=0;
        for (int x = 0; x < v.length; x++) {
            for(Vertex a: v){
                if(a.isHighCr()) {

                    if(a.getScheduled()==n) continue;
                    //System.out.println("SCH  "+a.getScheduled()+"   "+ n);
                    for (int l = 0; l < n; l++) {
                        j=0;
                        for (int i = 0; i < deadline; i++) {
                            if(!a.check_runnable(cpu.get_Running_Tasks(i),n)) continue;
                            boolean CPU_runnable=true;
                            for(Edge e: a.getRcvEdges()){
                                if(e.getSrc().isHighCr()) {
                                    if (cpu.getEndTimeTask(e.getSrc().getName() + " CR" + (int) (n - 1)) == -1)
                                        CPU_runnable = false;
                                    if (cpu.getEndTimeTask(e.getSrc().getName() + " CR" + (int) (n - 1)) > i) {
                                        CPU_runnable = false;
                                    }
                                    for (int k = 0; k < n; k++) {
                                        if ((cpu.getEndTimeTask(e.getSrc().getName() + " CO" + (int) (k)) > i) && (cpu.getEndTimeTask(e.getSrc().getName() + " CO" + (int) (k)) != -1)) {
                                            CPU_runnable = false;
                                        }

                                    }
                                }else{
                                    if (cpu.getEndTimeTask(e.getSrc().getName() + " CR" + (int) (0)) == -1) CPU_runnable = false;
                                        if (cpu.getEndTimeTask(e.getSrc().getName() + " CR" + (int) (0)) > i) {
                                            CPU_runnable = false;
                                        }

                                }

                            }
                            if(!CPU_runnable) {
                                //  System.out.println("RUN!");
                                continue;
                            }

                            boolean run=true;
                            for (int k = 0; k < n; k++) {
                                if(ov.indexOf(a.getName()+" CR"+k)!=0) {
                                    if (!cpu.CheckTimeSlot(j + k, i, i + a.getWcet(1))) {
                                        run = false;
                                    }
                                }else{
                                    if (!cpu.CheckTimeSlot(j + k, i, i + a.getWcet(0))) {
                                        run = false;
                                    }
                                }
                            }
                            if(run){
                                for (int k = 0; k < n; k++) {
                                    if(ov.indexOf(a.getName()+" CR"+k)>=0) {
                                        cpu.SetTaskOnCore(a.getName() + " CR" + k, (j + k), i, i + a.getWcet(0) - 1);
                                        cpu.SetTaskOnCore(a.getName() + " CO" + k, (j + k), i + a.getWcet(0), i + a.getWcet(1) - 1);
                                        a.setScheduled(a.getScheduled() + 1);
                                        System.out.println(a.getScheduled() + "   " + n + "   > " + k);
                                    }else{
                                        cpu.SetTaskOnCore(a.getName() + " CR" + k, (j + k), i, i + a.getWcet(0) - 1);
                                        a.setScheduled(a.getScheduled() + 1);
                                        System.out.println(a.getScheduled() + "   " + n + "   > " + k);
                                    }
                                }
                                break;
                            }
                            if (j<(n_core-1)){
                                j++;
                                i--;
                            }
                            else {
                                j = 0;
                            }
                        }
                        if(a.getScheduled()==n) break;
                    }
                }else{
                    if(a.getScheduled()==1) continue;
                    for (int i = 0; i < deadline; i++) {
                        if (!a.check_runnable(cpu.get_Running_Tasks(i), n)) continue;
                        boolean CPU_runnable=true;
                        for(Edge e: a.getRcvEdges()){
                            if(e.getSrc().isHighCr()) {
                                if (cpu.getEndTimeTask(e.getSrc().getName() + " CR" + (int) (n - 1)) == -1)
                                    CPU_runnable = false;
                                if (cpu.getEndTimeTask(e.getSrc().getName() + " CR" + (int) (n - 1)) > i) {
                                    CPU_runnable = false;
                                }
                                for (int k = 0; k < n; k++) {
                                    if ((cpu.getEndTimeTask(e.getSrc().getName() + " CO" + (int) (k)) > i) && (cpu.getEndTimeTask(e.getSrc().getName() + " CO" + (int) (k)) != -1)) {
                                        CPU_runnable = false;
                                    }

                                }
                            }else{
                                if (cpu.getEndTimeTask(e.getSrc().getName() + " CR" + (int) (0)) == -1) CPU_runnable = false;
                                if (cpu.getEndTimeTask(e.getSrc().getName() + " CR" + (int) (0)) > i) {
                                    CPU_runnable = false;
                                }

                            }

                        }
                        if(!CPU_runnable) {
                            //  System.out.println("RUN!");
                            continue;
                        }
                        if (cpu.CheckTimeSlot(j, i, i + a.getWcet(0))){
                            cpu.SetTaskOnCore(a.getName() + " CR0", j, i, i + a.getWcet(0));
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
        try {
            cpu.debug("NMR-mainSCH");
            cpu.Save_Power("OV"+overrun_percent+"F"+"0.0",xml_name,"NMR-mainSCH");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void sort_vertex() {
        Arrays.sort(v);
        Collections.reverse(Arrays.asList(v));
    }

    public void clean_sch(){
        for(Vertex a: v){
            a.setScheduled(0);
        }
    }
    public void clean_fault(){
        for(Vertex a: v){
            a.setInjected_fault(0);

        }
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

    public CPU getCpu() {
        return cpu;
    }


    public McDAG getDag() {
        return dag;
    }
}
