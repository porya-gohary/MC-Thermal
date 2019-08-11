/*******************************************************************************
 * Copyright (c) 2019 Porya Gohary
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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class CPU {
    //Core of CPU   [#Core] [#Time]
    private String[][] core;
    //Power Trace of Cores
    private double [][] power;
    //Deadline of System
    private int deadline;
    //Number of Core in CPU
    private int n_Cores;
    //MC-DAG
    McDAG mcDAG;

    public CPU( int deadline, int n_Cores,McDAG mcDAG) {
        this.deadline = deadline;
        this.n_Cores = n_Cores;
        this.mcDAG=mcDAG;
        core=new String[n_Cores][deadline];
        power=new double [n_Cores][deadline];
    }
    //GET Running Task in specific Time
    public String getRunningTask(int Core,int Time){
        return (core[Core][Time] == null) ? null : core[Core][Time].split(" R")[0];
//        System.out.println(core[Core][Time].split(" R")[0]);
//        return core[Core][Time];
    }

    //GET Running Task in specific Time
    public String getRunningTaskWithReplica(int Core,int Time){
       // System.out.println("GET Running Task in specific Time "+Core + "   "+ Time);
        return core[Core][Time];
    }

    //If Time slot was free return true;
    public boolean CheckTimeSlot(int Core,int Start,int End){
        if(Start > End) return false;
        if(Start<0 || End >deadline || Start >deadline) return false;
        for (int i = Start; i <= End ; i++) {
//            System.out.println("Check Time: "+Core+"  "+i);
            if(core[Core][i]!=null) return false;
        }
        return true;
    }

    //Set Task to Core
    public void SetTaskOnCore(String Task,int Core,int Start,int End){
        System.out.println(Task+"  "+ Start+"  "+End);
        try {
            for (int i = Start; i <= End; i++) {
                core[Core][i] = Task;
            }
        }catch(Exception e){
            System.err.println(Task+"  ⚠ ⚠ Infeasible!");
            System.exit(1);
        }
    }

    //Return Max. Core Can Use in specific Time
    public int max_core(int Time){
        String t;
        int max=n_Cores;
        for (int i = 0; i < n_Cores; i++) {
            if (getRunningTask(i,Time)!=null) {
                t = getRunningTask(i, Time);
                if (mcDAG.getNodebyName(t).getTSP_Active() < max) max = mcDAG.getNodebyName(t).getTSP_Active();
            }
        }
        return max;
    }


    public void setN_Cores(int n_Cores) {
        this.n_Cores = n_Cores;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public int getN_Cores() {
        return n_Cores;
    }

    public int getDeadline() {
        return deadline;
    }

    public void instatiate_power(double min_power){
        for (int i = 0; i < deadline; i++) {
            for (int j = 0; j < n_Cores; j++) {
                power[j][i]=min_power;
            }

        }
    }
    //Return Running Tasks
    public String[] get_Running_Tasks(int Time){
        String[] a=new String[n_Cores];
        for (int i = 0; i < n_Cores; i++) {
            a[i]=core[i][Time];
        }
        return a;
    }
    //Return Number Of Running Tasks
    public int numberOfRunningTasks(int Time){
        int r=0;
        for (int i = 0; i < n_Cores; i++) {
            if(core[i][Time]!=null) r++;
        }
        return r;
    }

    //Return Number Of Running Tasks
    public int numberOfRunningTasksInterval(int Start, int End){
        int r=0;
        for (int i = Start; i <= End ; i++) {
            if(this.numberOfRunningTasks(i)>r) r=this.numberOfRunningTasks(i);
        }
        return r;
    }

    //Return Max Core Can Use in specific Interval  [Start Time , End Time]
    public int maxCoreInterval(int Start,int End){
        int max = n_Cores;
        for (int i = Start; i <= End ; i++) {
            if(this.max_core(i)<max) max =this.max_core(i);
        }

        return max;
    }

    public int getSafeTime(String task){
        int SST=deadline;
//        System.out.println("++++>>"+task);
        for (int i = 0; i < n_Cores ; i++) {
            for (int j = 0; j < deadline; j++) {
               // System.out.println(i+"   "+j+"  "+core[i][j]);
                if(core[i][j]!=null) {
                    if (core[i][j].startsWith(task) && j < SST) {
                        SST = j;
                    }
                }
            }
            //if(Arrays.asList(core[i]).indexOf(task) < SST && Arrays.asList(core[i]).indexOf(task)!= -1) SST= Arrays.asList(core[i]).indexOf(task);
        }
        return SST;
    }

    //Write Scheduling In File for Debugging
    public void debug(String Filename) throws IOException {
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter(Filename+".csv"));
        for (int i = 0; i < getN_Cores(); i++) {
            for (int j = 0; j < getDeadline(); j++) {
                outputWriter.write(core[i][j]+",");
            };
            outputWriter.write("\n");
        }
        outputWriter.flush();
        outputWriter.close();
    }

    public void SetTask(int core_number , int time ,String task){
        try {
            core[core_number][time]=task;

        }catch (Exception e){
            System.out.println("Core  "+core_number+"  Time "+time);
        }
    }

    //Return Start Time of a Specific Replica of Tasks
    public int getStartTime(String Task){
        int s=deadline;
        for (int i = 0; i < n_Cores; i++) {
            if(Arrays.asList(core[i]).indexOf(Task)!= -1)
                if(Arrays.asList(core[i]).indexOf(Task)<s)
                    s=Arrays.asList(core[i]).indexOf(Task);
        }
        return s;

    }


    //Return End Time of a Specific Replica of Tasks
    public int getEndTime(String Task){
        int e=-1;
        System.out.println(Task);
        for (int i = 0; i < n_Cores; i++) {
            if(Arrays.asList(core[i]).lastIndexOf(Task) != -1) {
                if (Arrays.asList(core[i]).lastIndexOf(Task) > e) {
                    e = Arrays.asList(core[i]).lastIndexOf(Task);
                }
            }
        }
        System.out.println("   >>> "+e);
        return e;
    }


}
