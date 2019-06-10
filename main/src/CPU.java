import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CPU {
    //Core of CPU   [#Core Number] [#Time]
    private String[][] core;
    //Power Trace of Cores
    private double [][] power;
    //Deadline of System
    private int deadline;
    //Number of Core in CPU
    private int n_Cores;

    public CPU( int deadline, int n_Cores) {
        this.deadline = deadline;
        this.n_Cores = n_Cores;
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
        for (int i = Start; i <= End ; i++) {
            System.out.println("Check Time: "+Core+"  "+i);
            if(core[Core][i]!=null) return false;
        }
        return true;
    }

    //Set Task to Core
    public void SetTaskOnCore(String Task,int Core,int Start,int End){
        for (int i = Start; i <= End ; i++) {
            core[Core][i]=Task;
        }
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

    public String[] get_Running_Tasks(int Time){
        String[] a=new String[n_Cores];
        for (int i = 0; i < n_Cores; i++) {
            a[i]=core[i][Time];
        }
        return a;
    }

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
