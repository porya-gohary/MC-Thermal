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
        for (int i = Start; i <= End ; i++) {
            core[Core][i]=Task;
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
    //Write Scheduling In File for Debugging
    public void debug() throws IOException {
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter("SST.csv"));
        for (int i = 0; i < getN_Cores(); i++) {
            for (int j = 0; j < getDeadline(); j++) {
                outputWriter.write(core[i][j]+",");
            };
            outputWriter.write("\n");
        }
        outputWriter.flush();
        outputWriter.close();
    }


}
