import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;

public class Reliability_cal {
    //the fault rate at the maximum voltage
    float landa0;
    //Transient faults are usually assumed to follow a Poisson distribution with an average rate LANDA
    float landa;
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

    // All possible Voltage
    float [] v;

    public Reliability_cal(float landa0, float landa, float d, float v_i, float v_max, float rou, float rou_min, float t_i, File rel, float [] v) {
        this.landa0 = landa0;
        this.landa = landa;
        this.d = d;
        this.v_i = v_i;
        this.v_max = v_max;
        this.rou = rou;
        this.rou_min = rou_min;
        this.t_i = t_i;
        Rel = rel;
        this.v=v;
    }

    public Reliability_cal() {
    }

    public void cal (){
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
            System.out.println(rel_f.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
