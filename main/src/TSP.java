import java.io.*;

public class TSP {

    //DAG of Tasks
    McDAG dag;
    //File that contain maximum Voltage in Active Core simultaneously
    File file;
    //Number of Core in system
    int number_of_core;
    // All possible Voltage
    double [] v;
    //Voltages in File
    double [] TSP_v;

    public TSP(File file,int number_of_core, double [] v ,McDAG dag) {
        this.file=file;
        this.dag=dag;
        this.number_of_core=number_of_core;
        this.v=v;
        TSP_v=new double[number_of_core];
    }

    //Read TSP File That Contains Active Core Voltages
    public void read_TSP_file() throws IOException {
        BufferedReader reader;
        reader=new BufferedReader(new FileReader(file));
        int i=0;
        String line = reader.readLine();
        while (line != null) {
            TSP_v[i]=Double.parseDouble(line);
            line = reader.readLine();
            i++;
        }
    }

    public void cal_TSP_core (){
        for (Vertex a : dag.getVertices()){
            if(a.getMin_voltage()!=null){
                for (int i = (number_of_core-1); i >= 0; i--) {
                    if(a.getMin_voltage()<= TSP_v[i]) {
                        a.setTSP_Active(i+1);
                        break;
                    }
                }
            }else{
                //if DAG is Infeasible for this configuration
                System.err.println(a.getName()+"  ⚠ ⚠ Infeasible!");
            }

        }
    }
}
