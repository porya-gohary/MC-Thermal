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

public class TSP {

    //DAG of Tasks
    McDAG dag;
    //File that contain maximum Voltage in Active Core simultaneously
    File file;
    //Number of Core in system
    int number_of_core;
    // All possible Voltage
    double[] v;
    //Voltages in File
    double[] TSP_v;
    //All possible Freq;
    int[] freq;

    public TSP(File file, int number_of_core, double[] v, int[] freq, McDAG dag) {
        this.file = file;
        this.dag = dag;
        this.number_of_core = number_of_core;
        this.v = v;
        this.freq = freq;
        TSP_v = new double[number_of_core];
    }

    //Read TSP File That Contains Active Core Voltages
    public void read_TSP_file() throws IOException {
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(file));
        int i = 0;
        String line = reader.readLine();
        while (line != null) {
            TSP_v[i] = Double.parseDouble(line);
            line = reader.readLine();
            i++;
        }
    }

    public void cal_TSP_core() {
        for (Vertex a : dag.getVertices()) {
            if (a.getMin_voltage() != null) {
                for (int i = (number_of_core - 1); i >= 0; i--) {
                    if (a.getMin_voltage() <= TSP_v[i]) {
                        a.setTSP_Active(i + 1);
                        //a.setMin_freq(freq[Arrays.asList(v).indexOf(a.getMin_voltage())]);
                        for (int j = 0; j < v.length; j++) {
                            if (v[j] == a.getMin_voltage()) {
                                //System.out.println(freq[j]);
                                a.setMin_freq(freq[j]);
                            }
                        }
                        break;
                    }
                }
            } else {
                //if DAG is Infeasible for this configuration
                System.err.println(a.getName() + "  ⚠ ⚠ Infeasible!");
                System.exit(1);
            }

        }
    }

    public void debug() {
        for (Vertex a : dag.getVertices()) {
            System.out.println("-------" + a.getName() + "-------");
            System.out.println("Reliability = " + a.getReliability());
            System.out.println("MSSAC = " + a.getTSP_Active());
            System.out.println("Min. Freq. = " + a.getMin_freq());
        }
    }
}
