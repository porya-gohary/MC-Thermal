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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
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
    //All possible Freq
    int[] freq;
    //Execution Time
    double t_i;
    //minimum Reliability For each Task
    File Rel;
    ArrayList<Double> rel_f;
    //Minimum Voltage
    double v_min;

    // Minimum Execution Time In Maximum Voltage And Frequency
    double t_min;

    // All possible Voltage
    double[] v;

    //Reliability In Fault Free case
    double R_1;
    //Reliability In Faulty case
    double R_2;

    //Reliability
    double R_3;
    //Number Of Copy For Each Task
    double n;
    McDAG dag;

    String v_name;

    double R_CNMR = 0;
    double R_MEDINA = 0;


    public Reliability_cal(double n, double landa0, double d, double v_max, double v_min, File rel, double[] v, int[] freq, McDAG dag) {
        this.n = n;
        this.landa0 = landa0;
        this.d = d;

        this.v_max = v_max;
        this.v_min = v_min;

        Rel = rel;
        this.v = v;
        this.freq = freq;
        this.dag = dag;
        //cal();
        Read_file();
    }


    //Read Reliability From File And Set it on Every Vertices
    public void Read_file() {

        rel_f = new ArrayList<>();
        BufferedReader reader;
        try {

            reader = new BufferedReader(new FileReader(Rel));

            String line = reader.readLine();
            int i = 0;
            while (dag.getVertices().size() != i) {
                rel_f.add(Double.parseDouble(line));
//                System.out.println(line);
                String s = "D0N" + i;
                dag.getNodebyName(s).setReliability(Double.parseDouble(line));

                i++;
                line = reader.readLine();
            }
            reader.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cal() throws Exception {
        rou_min = v_min / v_max;

        for (int i = 0; i < v.length; i++) {
            v_i = v[i];

            rou = v_i / v_max;
            t_i = t_min * freq[freq.length - 1] / freq[i];
            landa = (landa0 * pow(10, ((d * (1 - rou)) / (1 - rou_min))));
            landa = (-1) * landa;
            double r = exp((landa * t_i));

            R_1 = pow(r, (ceil(n / 2)));
            landa = (landa0 * pow(10, ((d * (1 - 1)) / (1 - rou_min))));
//            System.out.println("landa0 = "+landa0 );
            landa = (-1) * landa;
            r = exp((landa * t_min));

            R_3 = pow(r, (floor(n / 2)));
//            for (int l = 1; l <= (floor(n/2)); l++) {
//                R_2+=combinations((int) n,l)*(pow((1-r),l))*(pow(r,(n-l)));
//            }
            double r_max = exp(-landa0 * t_min);
            for (int k = 1; k <= (floor(n / 2)); k++) {
                for (int j = 1; j <= k; j++) {
                    R_2 += combinations(((int) ceil(n / 2)), j) * (pow((1 - r), j)) * (pow(r, (ceil(n / 2) - j))) * combinations((int) floor(n / 2), k - j)
                            * (pow((1 - r_max), (k - j))) * (pow(r_max, (floor(n / 2) - (k - j))));
                }
            }
            if ((R_1 + R_2) >= dag.getNodebyName(v_name).getReliability()) {
                dag.getNodebyName(v_name).setMin_voltage(v_i);
                R_CNMR = 0;
                for (int j = (int) ceil(n / 2); j <= n; j++) {
                    R_CNMR += combinations((int) n, j) * pow(r, j) * (pow((1 - r), n - j));
//                    R_CNMR =BigDecimal.valueOf(R_CNMR) + BigDecimal.valueOf(combinations((int) n, j) * pow(r, j) * (pow((1 - r), n - j)));
//                    System.out.println(">>> " + combinations((int) n, j));
                }
                R_MEDINA = r;
//                System.out.println("----------------------");
//                System.out.println("PoF Medina=" + (1 - R_MEDINA) + "  *************  " + R_MEDINA);
//                System.out.println("Task "+ v_name +" Voltage= " + v_i + " v  and R =  "+(R_1+R_2)+" ");
//                System.out.println(R_2);
//                System.out.println("----------------------");
//                System.out.println("Task " + v_name + " PoF = " + (1 - (R_CNMR)) + "  *************  " + (R_CNMR));
//                System.out.println(R_3);
//                System.out.println("---------------------------");
//                System.out.println("RESULT");
//                if((R_1+R_2) > (R_1*R_3) ){
//                    System.out.println("Salehi");
//                }
//                else{
//                    System.out.println("All in one Phase");
//                }
//                System.out.println("---------------------------");
                R_1 = 0;
                R_2 = 0;
                return;
            }
            R_1 = 0;
            R_2 = 0;
//            System.out.println("Task "+ v_name +" Voltage=" + v_i + " v  and R =  "+(R_1+R_2)+" ");
//           System.out.println("Reliability [R(1)] For " + v_i + " v =  "+(R_1)+" ");
//           System.out.println("Reliability [R(2)] For " + v_i + " v =  "+(R_2)+" ");


        }
        System.err.println(v_name + " Reliability ⚠ ⚠ Infeasible!");
        //System.out.println("Core  "+core_number+"  Time "+time);
        throw new Exception("Infeasible!");

    }

    //Simple Function For Calculating Combination of Two Number
    private int combinations(int n, int k) {
        return (factorial(n) / (factorial(k) * factorial(n - k)));
    }

    //Function For Calculating Factorial
    private int factorial(int n) {
        if (n == 0)
            return 1;
        else
            return (n * factorial(n - 1));
    }

    public void setV_name(String v_name) {
        this.v_name = v_name;
    }

    public void setT_min(double t_min) {
        this.t_min = t_min;
    }
}
