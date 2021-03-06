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

    //Number Of Fault injected
    int number_of_fault;

    int number_of_overrun;

    double overrun_percent;
    double fault_percent;

    double max_voltage;
    int max_freq;
    int max_freq_cores;
    CPU cpu;
    String xml_name;

    //Number of low-critical Tasks
    int low_num=0;

    public mainScheduling(Vertex[] v,McDAG mcDAG,double n,int deadline,int n_core,int number_of_overrun,int number_of_fault,double overrun_percent, double fault_percent,double max_voltage, int max_freq, int max_freq_cores,String xml_name) {
        this.deadline = deadline;
        this.n_core = n_core;
        this.n = n;
        this.mcDAG = mcDAG;
        this.v = v;
        this.max_voltage = max_voltage;
        this.max_freq=max_freq;
        this.max_freq_cores=max_freq_cores;
        this.xml_name=xml_name;
        this.number_of_fault=number_of_fault;
        this.number_of_overrun=number_of_overrun;
        this.overrun_percent=overrun_percent;
        this.fault_percent=fault_percent;
        this.low_num=mcDAG.getVertices().size()-mcDAG.getNodes_HI().size();
    }

    public void mScheduling() throws Exception {
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
                            boolean CPU_runnable=true;
                            for(Edge e: a.getRcvEdges()){
                                if(cpu.getEndTimeTask(e.getSrc().getName()+" R"+(int)(ceil(n/2)-1))==-1)CPU_runnable=false;
                                if(cpu.getEndTimeTask(e.getSrc().getName()+" R"+(int)(ceil(n/2)-1))>i){
                                    CPU_runnable=false;
                                }
                            }
                            if(!CPU_runnable) continue;
                            if(cpu.CheckTimeSlot(j, i,i+a.getRunningTimeLO(max_freq,a.getMin_freq())-1) && (cpu.maxCoreInterval(i,i+a.getRunningTimeLO(max_freq,a.getMin_freq())-1)>=a.getTSP_Active()) &&
                                    (cpu.numberOfRunningTasksInterval(i,i+a.getRunningTimeLO(max_freq,a.getMin_freq())-1)<a.getTSP_Active())){
                                cpu.SetTaskOnCore(a.getName()+" R"+l,j,i,i+a.getRunningTimeLO(max_freq,a.getMin_freq())-1);
                                a.setScheduled(a.getScheduled()+1);
                                //System.out.println(a.getName()+"   "+a.getScheduled());
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
                        if(a.getScheduled()==(int)(ceil(n/2))) break;
                            
                        
                    }
                }else{
                    //One Replica For LO-Critical Tasks
                    if(a.getScheduled()==1) continue;
                    if(!a.isRun())continue;
                    for (int i = 0; i < deadline; i++) {
                        if (!a.check_runnable(cpu.get_Running_Tasks(i), n)) continue;
                        boolean CPU_runnable=true;
                        for(Edge e: a.getRcvEdges()){
                            if(e.getSrc().isHighCr()) {
                                if(cpu.getEndTimeTask(e.getSrc().getName()+" R"+(int)(ceil(n/2)-1))==-1)CPU_runnable=false;
                                if (cpu.getEndTimeTask(e.getSrc().getName() + " R" + (int) (ceil(n / 2) - 1)) > i) {
                                    CPU_runnable = false;
                                }
                            }else{
                                if(cpu.getEndTimeTask(e.getSrc().getName() + " R0")==-1)  CPU_runnable = false;
                                if(cpu.getEndTimeTask(e.getSrc().getName() + " R0")>i)  CPU_runnable = false;
                            }
                        }
                        if(!CPU_runnable) continue;
                        if (cpu.CheckTimeSlot(j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq())) && (cpu.maxCoreInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq())) >= a.getTSP_Active()) &&
                                (cpu.numberOfRunningTasksInterval(i, i + a.getRunningTimeLO(max_freq, a.getMin_freq())) < a.getTSP_Active())) {
                            cpu.SetTaskOnCore(a.getName() + " R0", j, i, i + a.getRunningTimeLO(max_freq, a.getMin_freq()));
                            a.setScheduled(a.getScheduled() + 1);
                            //System.out.println(a.getName() + "   " + a.getScheduled());
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
            cpu.Save_Power("OV"+this.overrun_percent+"F"+this.fault_percent,xml_name,"mainSCH");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    // a Function for inject fault to tasks
    public void inject_fault(int number_of_fault) throws Exception {

        Set<Vertex> nodesHI=new HashSet<Vertex>();
//        for(Vertex a:mcDAG.getVertices()){
//            if (a.getWcet(1)!=0)
//                nodesHI.add(a);
//        }
       //Vertex HIv[] = nodesHI.toArray(new Vertex[0]);
        Vertex HIv[]= mcDAG.getNodes_HI().toArray(new Vertex[0]);

        int f=0;
        Random fault= new Random();
        for (int i = 0; i < number_of_fault; i++) {
            do {
                f = fault.nextInt(HIv.length);
            }while(HIv[f].getInjected_fault()!=0);

            System.out.println("↯↯ Fault injected To  "+HIv[f].getName());
            mcDAG.getNodebyName(HIv[f].getName()).setInjected_fault(mcDAG.getNodebyName(HIv[f].getName()).getInjected_fault()+1);

            int t=cpu.getEndTimeTask(HIv[f].getName()+" R"+(int)(ceil(n/2)-1));
//            Task_Shifter(t,(int) (HIv[f].getWcet(0)*floor(n/2)));
//            for (int j = 0; j < floor(n/2); j++) {
//                cpu.SetTaskOnCore(HIv[f].getName()+" F"+j,1,t+1+(j*(HIv[f].getWcet(0))),t+((j+1)*(HIv[f].getWcet(0))));
//            }
            int min= (max_freq_cores<floor(n/2)) ? max_freq_cores : (int) floor(n / 2);
            int b=0;
            for (int k = 0; k < (int)floor(n/2)/min; k++) {
                cpu.Task_Shifter(t,HIv[f].getRunningTimeLO(max_freq,max_freq));
                for (int j = 0; j < min; j++) {
                    cpu.SetTaskOnCore(HIv[f].getName()+" F"+b,j,t+1,t+(HIv[f].getRunningTimeLO(max_freq,max_freq)));
                    b++;
                }
                t=t+HIv[f].getRunningTimeLO(max_freq,max_freq);
            }
        }

        try {
            cpu.debug("mainSCH+Fault");
            cpu.Save_Power("OV"+this.overrun_percent+"F"+this.fault_percent,xml_name,"mainSCH+Fault");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void overrun(int number_of_overrun) throws Exception {
        Random overrun= new Random();
        int o = 0;
        String ov_name;
        ArrayList <String> ov=new ArrayList<String>();
        for (int i = 0; i < number_of_overrun; i++) {

            //System.out.println("|||| OV-CORE ||||| "+o+"  "+ Endtime(o));
            do{
                do{
                    o=overrun.nextInt(n_core);
                }while(cpu.Endtime(o)==0);
                ov_name=cpu.getRunningTaskWithReplica(o,overrun.nextInt(cpu.Endtime(o)));
            }while(ov_name==null || mcDAG.getNodebyName(ov_name.split(" ")[0]).getWcet(1)==0);
            ov.add(ov_name);
            int t=cpu.getEndTimeTask(ov_name);
            System.out.println("|||| OV |||||   "+ov_name+"  Core: "+o);
            Vertex v=mcDAG.getNodebyName(ov_name.split(" ")[0]);

            //System.out.println("*****   "+v.getWcet(1));
            cpu.Task_Shifter(t,v.getRunningTimeHI(max_freq,max_freq)-v.getRunningTimeLO(max_freq,max_freq));
            cpu.SetTaskOnCore(v.getName()+" OV",1,t+1,t+(v.getRunningTimeHI(max_freq,max_freq)-v.getRunningTimeLO(max_freq,max_freq)));
        }
        try {
            cpu.debug("mainSCH+Fault+Overrun");
            cpu.Save_Power("OV"+this.overrun_percent+"F"+this.fault_percent,xml_name,"mainSCH+Fault+Overrun");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sort_vertex() {
        Arrays.sort(v);
        Collections.reverse(Arrays.asList(v));
        //Show Sorted Vortex Array
    }
    public CPU getCpu(){
        return cpu;
    }

    public void Drop_task(){
        for (int i = v.length-1; i >=0 ; i--) {
            if(!v[i].isHighCr()&& v[i].isRun()) {
                v[i].setRun(false);
                System.out.println("■■■  DROP TASK "+v[i].getName());
                break;
            }
        }
    }

    public double QoS(){
        double QoS=0;
        for (int i = 0; i < v.length; i++) {
            if(!v[i].isHighCr() && v[i].isRun()) QoS++;
        }
        QoS=QoS/(v.length-mcDAG.getNodes_HI().size());
        return QoS;
    }
}
