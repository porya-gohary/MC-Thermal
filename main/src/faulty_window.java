import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

public class faulty_window {

    McDAG dag;
    //Number of Core
    int n_core;
    double n;
    Vertex v[];
    Set<Vertex> HIv;
    //Max freq
    int max_freq=2000;
    //number of cores that can work with max freq in same time
    int max_freq_cores = 1;


    public void make_faulty_window() throws Exception {
        Set<window> block = new HashSet<window>();

        do {
            int t=0;
            Vertex temp = null;
            for (Vertex a : HIv) {
                if (a.getWcets()[0]>t){
                    boolean r=true;
                    for(Edge e: a.getRcvEdges()){
                        for (Vertex b : HIv) {
                            if (b==e.getSrc()) {
                                r = false;
                                break;
                            }
                        }
                        if (!r) break;
                    }
                    if(!r) continue;
                    t=a.getWcets()[0];
                    temp=a;
                }
            }
            int size= (int) ceil((floor(n/2))/max_freq_cores)*temp.getWcet(0);
            window w=new window(size,n_core,dag);


        }while (HIv.size()!=0);
    }

}
