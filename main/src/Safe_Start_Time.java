/*******************************************************************************
 * Copyright © 2019 Porya Gohary
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
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

import static java.lang.Math.*;

public class Safe_Start_Time {
    //Deadline
    int deadline;
    //Number of CPU Core
    int n_core;
    //Number of Redundancy
    double n;
    //DAG
    McDAG mcDAG;
    //Vertex Array for Sorting Vertexes
    Vertex v[];

    double max_voltage;
    int max_freq;

    CPU cpu;
    int max_freq_cores;


    public Safe_Start_Time(Vertex[] v,McDAG mcDAG,double n,int deadline,int n_core,double max_voltage, int max_freq, int max_freq_cores) {
        this.v = v;
        this.n=n;
        this.deadline=deadline;
        this.n_core=n_core;
        this.mcDAG=mcDAG;
        this.max_voltage=max_voltage;
        this.max_freq=max_freq;
        this.max_freq_cores=max_freq_cores;
    }


    public void scheduling(){
        cpu=new CPU(deadline,n_core,mcDAG);
        int j=0;
        int k=0;
        for (int x = 0; x < v.length ; x++) {
            for(Vertex a: v){
                if(!a.isHighCr()) continue;
                if(a.getScheduled()==ceil(n/2)) continue;
                for (int l = 0; l < ceil(n/2) ; l++) {
                    j=0;
                    System.out.println(a.getName()+"   "+a.getWcet(1));
                    for (int i = (deadline-1); i >= 0; i--) {
                        // ***********HERE NEED TO CHECKING NUMBER OF ACTIVE CORE*****

                        if(!a.reverse_running(cpu.get_Running_Tasks(i),n)) continue;
                        if(cpu.CheckTimeSlot(j,i-a.getRunningTimeLO(max_freq,a.getMin_freq())+1,i) && (cpu.maxCoreInterval(i-a.getRunningTimeLO(max_freq,a.getMin_freq())+1,i)>=a.getTSP_Active()) &&
                                (cpu.numberOfRunningTasksInterval(i-a.getRunningTimeLO(max_freq,a.getMin_freq())+1,i)<a.getTSP_Active())){
                            cpu.SetTaskOnCore(a.getName()+" R"+l,j,i-a.getRunningTimeLO(max_freq,a.getMin_freq())+1,i);
                            a.setScheduled(a.getScheduled()+1);
                            System.out.println(a.getName()+"   "+a.getScheduled());
                            break;
//                  ***********HERE NEED TO COMPLETE*****
                        }
                        if (j<(n_core-1)){
                            j++;
                            i++;
                        }
                        else {
                            j=0;

                        }
         //               if(i==0 && j==3) System.err.println(a.getName()+"  ⚠ ⚠ Infeasible!");
                        //                  ***********HERE NEED TO COMPLETE*****

                    }
                    if(a.getScheduled()==(int)(ceil(n/2))) break;
                }
            }

        }

        try {
            cpu.debug("main_SST_SCH");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sort_vertex() {
        Arrays.sort(v);
        //Show Sorted Vortex Array
        for(Vertex a:v){
            System.out.println(a.getName()+"  ==>>  "+a.getLPL());
        }
    }
    
    public int starttime(int core){
        for (int i = 0; i < deadline; i++) {
            if(cpu.getRunningTask(core,i)!=null){
                return i;
            }
        }
        return -1;
    }

    //Shift Running Tasks For adding Overrun and Faults
    public void Task_shifter(int shiftTime ,int amount ) {
        for (int i = 0; i < n_core; i++) {
            if(starttime(i)==-1)continue;
            for (int j = starttime(i); j < shiftTime ; j++) {
                try {
                    //System.out.println("Shift::: "+shiftTime+"   ///  "+amount+" \\\\  "+ starttime(i));
                    cpu.SetTask(i, j - amount, cpu.getRunningTaskWithReplica(i, j));
                }catch(Exception ex)
                {
                    System.err.println(cpu.getRunningTaskWithReplica(i, j)+"  ⚠ ⚠ Infeasible!");
                    System.exit(1);
                }
            }
            for (int j = shiftTime -amount; j < shiftTime; j++) {
                cpu.SetTask(i, j , null);
            }
        }
    }

    //Add Overrun of Tasks To Safe Start Time
    public void overrun(){
        for(Vertex a: mcDAG.getVertices()){
            if(!a.isHighCr()) continue;
            for (int i = 0; i < ceil(n/2); i++) {
                int t=cpu.getStartTime(a.getName()+" R"+i);
                Task_shifter(t,a.getRunningTimeHI(max_freq,max_freq)-a.getRunningTimeLO(max_freq,max_freq));
                cpu.SetTaskOnCore(a.getName()+" OV"+i,1,t-(a.getRunningTimeHI(max_freq,max_freq)-a.getRunningTimeLO(max_freq,max_freq)),t-1);
            }

        }

        try {
            cpu.debug("With_Overrun");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void inject_fault(){
        for(Vertex a: mcDAG.getVertices()){
            if(!a.isHighCr()) continue;
            int t=cpu.getStartTime(a.getName()+" OV"+(int)(ceil(n/2)-1));
            System.out.println((a.getName()+" OV"+(int)(ceil(n/2)-1))+"  "+t);
            int min= (max_freq_cores<floor(n/2)) ? max_freq_cores : (int) floor(n / 2);
            int b=0;
            for (int i = 0; i < (int)floor(n/2)/min; i++) {
                Task_shifter(t,a.getRunningTimeHI(max_freq,max_freq));
                for (int j = 0; j < min; j++) {
                    cpu.SetTaskOnCore(a.getName()+" F"+b,j,t-(a.getRunningTimeHI(max_freq,max_freq)),t-1);
                    b++;
                }
                t=t-a.getRunningTimeHI(max_freq,max_freq);
            }

        }
        try {
            cpu.debug("With_Fault");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Set Safe Start Time (It Must Call After Scheduling Method)
    public void setSafeStartTime(){
        for(Vertex a: mcDAG.getVertices()){
            if(!a.isHighCr()) continue;
            if (cpu.getSafeTime(a.getName())== deadline || cpu.getSafeTime(a.getName())<= 0) {
                System.err.println(a.getName()+"  ⚠ ⚠ Infeasible!");
                System.exit(1);
            }
            a.setSST(cpu.getSafeTime(a.getName()));
        }

    }




}
