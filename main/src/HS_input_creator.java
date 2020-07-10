import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/*******************************************************************************
 * Copyright © 2020 Pourya Gohari
 * Written by Pourya Gohari (Email: gohary@ce.sharif.edu)
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
public class HS_input_creator {

    //CPU of System
    CPU cpu;
    int end;
    String pathSeparator = File.separator;
    //HotSpot location and information
    String hotspot_path = "HotSpot" + pathSeparator + "hotspot";
    String hotspot_config = "HotSpot" + pathSeparator + "configs" + pathSeparator;
    String floorplan = "HotSpot" + pathSeparator + "floorplans" + pathSeparator;
    String powertrace = "HotSpot" + pathSeparator + "powertrace" + pathSeparator;
    String thermaltrace = "HotSpot" + pathSeparator + "thermaltrace" + pathSeparator + "thermal.ttrace";
    boolean VERBOSE = false;

    public HS_input_creator(CPU cpu) {
        this.cpu = cpu;
        end = cpu.getDeadline();
    }

    public void Save(String mFolder, String Folder, String Filename, int end) throws IOException {
        this.end = end;
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter(mFolder + "//" + Folder + "//" + Filename));
        //Add HotSpot Header
        for (int i = 0; i < cpu.getN_Cores(); i++) {
            String s = (i != cpu.getN_Cores() - 1) ? "core_" + i + "\t" : "core_" + i + "\n";
            outputWriter.write(s);
        }

        //Add Power of each core
        for (int i = 0; i <= end; i++) {
            for (int j = 0; j < cpu.getN_Cores(); j++) {
                String s = (j != cpu.getN_Cores() - 1) ? cpu.get_power(j, i) + "\t" : cpu.get_power(j, i) + "\n";
                outputWriter.write(s);
            }
        }
        outputWriter.flush();
        outputWriter.close();
    }

    public void run_steady(String mFolder, String Folder, String Filename, int end) throws IOException {
        this.end = end;
        BufferedWriter outputWriter = null;
        hotspot_config = "HotSpot" + pathSeparator + "configs" + pathSeparator;
        floorplan = "HotSpot" + pathSeparator + "floorplans" + pathSeparator;
        powertrace = "HotSpot" + pathSeparator + "powertrace" + pathSeparator;
        HotSpot hotSpot = new HotSpot(hotspot_path, VERBOSE);

        hotspot_config += "hotspot_" + cpu.getN_Cores() + ".config";
        floorplan += "A15_" + cpu.getN_Cores() + ".flp";
        powertrace += "A15_" + cpu.getN_Cores() + ".ptrace";


        //Add Power of each core
        BufferedWriter outputWriter2 = null;
        outputWriter2 = new BufferedWriter(new FileWriter("HotSpot" + pathSeparator + "thermaltrace" + pathSeparator + "thermal.ttrace"));
        for (int i = 0; i <= end; i++) {
            outputWriter = new BufferedWriter(new FileWriter(mFolder + "//" + Folder + "//" + Filename));
            //Add HotSpot Header
            for (int k = 0; k < cpu.getN_Cores(); k++) {
                String s = (k != cpu.getN_Cores() - 1) ? "core_" + k + "\t" : "core_" + k + "\n";
                outputWriter.write(s);
            }
            for (int j = 0; j < cpu.getN_Cores(); j++) {
                String s = (j != cpu.getN_Cores() - 1) ? cpu.get_power(j, i) + "\t" : cpu.get_power(j, i) + "\n";
                outputWriter.write(s);
            }

            outputWriter.flush();
            outputWriter.close();
            hotSpot.run_steady(hotspot_config, floorplan, powertrace);



            File steady_out = null;
            steady_out = new File("A15.steady");
            if (i == 0) {
                Scanner Reader = new Scanner(steady_out);
                for (int j = 0; j < cpu.getN_Cores(); j++) {
                    String data = Reader.nextLine();
                    String Sdatavalue[] = data.split("\t");
                    outputWriter2.write(Sdatavalue[0]);
                    if (j != cpu.getN_Cores() - 1)
                        outputWriter2.write("\t");
                    else
                        outputWriter2.write("\n");

                }
            }

            steady_out = new File("A15.steady");
            Scanner Reader = new Scanner(steady_out);
            for (int j = 0; j < cpu.getN_Cores(); j++) {
                String data = Reader.nextLine();
                String Sdatavalue[] = data.split("\t");
                outputWriter2.write((Double.parseDouble(Sdatavalue[1])-273.15)+"");
                if (j != cpu.getN_Cores() - 1)
                    outputWriter2.write("\t");
                else
                    outputWriter2.write("\n");
            }
            Reader.close();

        }
        outputWriter2.flush();
        outputWriter2.close();

    }
}
