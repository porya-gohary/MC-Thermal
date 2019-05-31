import java.io.File;
import java.lang.Math;

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

    public void cal (){

    }

}
