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
    double d;
    // scaled supply voltage
    double v_i;
    // maximum supply voltage
    double v_max;
    //ratio of v_i / v_max
    double rou;
    //ratio of v_min / v_max
    double rou_min;
    //Execution Time
    double t_i;
    //minimum Reliability For each Task
    File Rel;
    ArrayList<Float> rel_f;
    //Minimum Voltage
    double v_min;

    // Minimum Execution Time In Maximum Voltage And Frequency
    double t_min;

    // All possible Voltage
    double [] v;

    //Reliability In Fault Free case
    double R_1;
    //Reliability In Faulty case
    double R_2;

    //Number Of Copy For Each Task
    double n;


    public Reliability_cal(int n,double landa0, double d,  double v_max, double v_min, double t_min, File rel, double [] v) {
        this.n=n;
        this.landa0 = landa0;
        this.d = d;

        this.v_max = v_max;
        this.v_min = v_min;

        this.t_min = t_min;
        Rel = rel;
        this.v=v;
        cal();
    }

//    public Reliability_cal() {
//        Read_file();
//    }

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
        for(double v_i:v){
            rou=v_i/v_max;
            t_i=t_min/rou;
            landa=(landa0*pow(10, ((d*(1-rou))/(1-rou_min))));
            landa=(-1)*landa;
            double r=exp((landa*t_i/rou));
            R_1=pow(r,(ceil(n/2)));
            for (int l = 0; l < (floor(n/2)); l++) {
                R_2+=combinations((int) n,l)*(pow((1-r),l))*(pow(r,(n-l)));
            }
            System.out.println("Reliability For " + v_i + " v =  "+(R_1+R_2)+" ");
            R_1=0;
            R_2=0;
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
