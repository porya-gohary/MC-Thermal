import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.*;

public class Reliability_cal {
    //the fault rate at the maximum voltage
    double landa0;
    //Transient faults are usually assumed to follow a Poisson distribution with an average rate LANDA
    double landa;
    //d is a technology dependent constant
    float d;
    // scaled supply voltage
    float v_i;
    // maximum supply voltage
    float v_max;
    //ratio of v_i / v_max
    float rou;
    //ratio of v_min / v_max
    float rou_min;
    //Execution Time
    float t_i;
    //minimum Reliability For each Task
    File Rel;
    ArrayList<Float> rel_f;
    //Minimum Voltage
    float v_min;

    // Minimum Execution Time In Maximum Voltage And Frequency
    float t_min;

    // All possible Voltage
    float [] v;

    //Reliability In Fault Free case
    double R_1;
    //Reliability In Faulty case
    double R_2;

    //Number Of Copy For Each Task
    int n;


    public Reliability_cal(int n,float landa0, float d,  float v_max,  float v_min, float t_i, File rel, float [] v) {
        this.n=n;
        this.landa0 = landa0;
        this.d = d;

        this.v_max = v_max;
        this.v_min = v_min;

        this.t_i = t_i;
        Rel = rel;
        this.v=v;
    }

    public Reliability_cal() {
        Read_file();
    }

    public void Read_file (){
        //Read Reliability From File
        rel_f=new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    "C:\\Users\\PC Khafan\\Desktop\\MC-Thermal\\rel.txt"));
            String line = reader.readLine();

            while (line != null) {
                rel_f.add(Float.parseFloat(line));
                System.out.println(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
            System.out.println("...................");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cal(){
        rou_min=v_min/v_max;
        for(float v_i:v){
            t_i=t_min*v_max/v_i;
            rou=v_i/v_max;
            landa=(landa0*pow(10, ((d*(1-rou))/(1-rou_min))));
            double r=exp(((-1)*landa*t_i/rou));
            R_1=pow(r,ceil(n/2));
            for (int l = 0; l < (floor(n/2)); l++) {
                R_2+=combinations(n,l)*(pow((1-r),l))*(pow(r,(n-l)));
            }
        }
    }

    private int combinations(int n, int k){
        return factorial(n) / (factorial (k) * factorial (n-k));
    }
    private int factorial(int n){
        if (n == 0)
            return 1;
        else
            return(n * factorial(n-1));
    }


}
