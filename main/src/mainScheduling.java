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
import java.util.*;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

public class mainScheduling {
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
    int max_freq_cores;
    CPU cpu;

    public mainScheduling(Vertex[] v,McDAG mcDAG,double n,int deadline,int n_core,double max_voltage, int max_freq, int max_freq_cores) {
        this.deadline = deadline;
        this.n_core = n_core;
        this.n = n;
        this.mcDAG = mcDAG;
        this.v = v;
        this.max_voltage = max_voltage;
        this.max_freq=max_freq;
        this.max_freq_cores=max_freq_cores;
    }

    public void mScheduling(){
        cpu=new CPU(deadline,n_core,mcDAG);
        int j=0;
        for (int x = 0; x < v.length; x++) {
            for(Vertex a: v){

                if(a.isHighCr()) {
                    if(a.getScheduled()==ceil(n/2)) continue;
                    //For Add Extra Copy for HI-Critical Tasks
                    for (int l = 0; l < ceil(n / 2); l++) {
                        j=0;
                        for (int i = 0; i < deadline; i++) {
                            if(!a.check_runnable(cpu.get_Running_Tasks(i),n)) continue;
                            if(cpu.CheckTimeSlot(j, i,i+a.getRunningTimeLO(max_freq,a.getMin_freq())-1) && (cpu.maxCoreInterval(i,i+a.getRunningTimeLO(max_freq,a.getMin_freq())-1)>=a.getTSP_Active()) &&
                                    (cpu.numberOfRunningTasksInterval(i,i+a.getRunningTimeLO(max_freq,a.getMin_freq())-1)<a.getTSP_Active())){
                                cpu.SetTaskOnCore(a.getName()+" R"+l,j,i,i+a.getRunningTimeLO(max_freq,a.getMin_freq())-1);
                                a.setScheduled(a.getScheduled()+1);
                                System.out.println(a.getName()+"   "+a.getScheduled());
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
                            
                        
                    }
                }else{
                    //One Replica For LO-Critical Tasks
                    if(a.getScheduled()==1) continue;
                    for (int i = 0; i < deadline; i++) {
                        if (!a.check_runnable(cpu.get_Running_Tasks(i), n)) continue;
                        if (cpu.CheckTimeSlot(j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq())) && (cpu.maxCoreInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq())) >= a.getTSP_Active()) &&
                                (cpu.numberOfRunningTasksInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq())) < a.getTSP_Active())) {
                            cpu.SetTaskOnCore(a.getName() + " R0", j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()));
                            a.setScheduled(a.getScheduled() + 1);
                            System.out.println(a.getName() + "   " + a.getScheduled());
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
            cpu.debug("mainSCH");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clean_sch(){
        for(Vertex a: v){
            a.setScheduled(0);
        }
    }

    // a Function for inject fault to tasks
    public void inject_fault(int number_of_fault){
        Set<Vertex> nodesHI=new HashSet<Vertex>();
        for(Vertex a:mcDAG.getVertices()){
            if (a.getWcet(1)!=0)
                nodesHI.add(a);
        }
        Vertex HIv[] = nodesHI.toArray(new Vertex[0]);

        int f=0;
        Random fault= new Random();
        for (int i = 0; i < number_of_fault; i++) {
            do {
                f = fault.nextInt(HIv.length);
            }while(HIv[f].getInjected_fault()!=0);

            System.out.println("↯↯ Fault injected To  "+HIv[f].getName());
            mcDAG.getNodebyName(HIv[f].getName()).setInjected_fault(mcDAG.getNodebyName(HIv[f].getName()).getInjected_fault()+1);

            int t=cpu.getEndTime(HIv[f].getName()+" R"+(int)(ceil(n/2)-1));
//            Task_Shifter(t,(int) (HIv[f].getWcet(0)*floor(n/2)));
//            for (int j = 0; j < floor(n/2); j++) {
//                cpu.SetTaskOnCore(HIv[f].getName()+" F"+j,1,t+1+(j*(HIv[f].getWcet(0))),t+((j+1)*(HIv[f].getWcet(0))));
//            }
            int min= (max_freq_cores<floor(n/2)) ? max_freq_cores : (int) floor(n / 2);
            int b=0;
            for (int k = 0; k < (int)floor(n/2)/min; k++) {
                Task_Shifter(t,HIv[f].getRunningTimeLO(max_freq,max_freq));
                for (int j = 0; j < min; j++) {
                    cpu.SetTaskOnCore(HIv[f].getName()+" F"+b,j,t+1,t+(HIv[f].getRunningTimeLO(max_freq,max_freq)));
                    b++;
                }
                t=t+HIv[f].getRunningTimeLO(max_freq,max_freq);
            }
        }

        try {
            cpu.debug("mainSCH+Fault");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void overrun(int number_of_overrun){
        Random overrun= new Random();
        int o = 0;
        String ov_name;
        ArrayList <String> ov=new ArrayList<String>();
        for (int i = 0; i < number_of_overrun; i++) {
            do{
                o=overrun.nextInt(n_core);
            }while(Endtime(o)==0);
            //System.out.println("|||| OV-CORE ||||| "+o+"  "+ Endtime(o));
            do{
                ov_name=cpu.getRunningTaskWithReplica(o,overrun.nextInt(Endtime(o)));
            }while(ov_name==null || mcDAG.getNodebyName(ov_name.split(" ")[0]).getWcet(1)==0);
            ov.add(ov_name);
            int t=cpu.getEndTime(ov_name);
            System.out.println("|||| OV |||||   "+ov_name+"  Core: "+o);
            Vertex v=mcDAG.getNodebyName(ov_name.split(" ")[0]);

            //System.out.println("*****   "+v.getWcet(1));
            Task_Shifter(t,v.getRunningTimeHI(max_freq,max_freq)-v.getRunningTimeLO(max_freq,max_freq));
            cpu.SetTaskOnCore(v.getName()+" OV",1,t+1,t+(v.getRunningTimeHI(max_freq,max_freq)-v.getRunningTimeLO(max_freq,max_freq)));
        }
        try {
            cpu.debug("mainSCH+Fault+Overrun");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //a function for determine end time in each core
    public int Endtime(int core){
        for (int i = deadline-1; i >= 0; i--) {
            if(cpu.getRunningTask(core,i)!=null){
                return i;
            }
        }
        return 0;
    }

    public void Task_Shifter(int shiftTime ,int amount ){
        System.out.println("TASK SHIFTER  "+ shiftTime+"  > > "+amount);
        for (int i = 0; i < n_core; i++) {
            for (int j = Endtime(i); j > (shiftTime) ; j--) {
                try {
                    cpu.SetTask(i, j + amount, cpu.getRunningTaskWithReplica(i, j));
                }catch(Exception ex)
                {
                    System.err.println(cpu.getRunningTaskWithReplica(i, j)+"  ⚠ ⚠ Infeasible!");
                    System.exit(1);
                }
            }
            for (int j = shiftTime+1 ; j < shiftTime+amount+1; j++) {
                cpu.SetTask(i, j , null);
            }
        }
    }

    public void sort_vertex() {
        Arrays.sort(v);
        Collections.reverse(Arrays.asList(v));
        //Show Sorted Vortex Array

        for(Vertex a:v){
            System.out.println(a.getName()+"  ==>>  "+a.getLPL()+"    -=>  "+a.getScheduled());
        }
    }
    public CPU getCpu(){
        return cpu;
    }
}
