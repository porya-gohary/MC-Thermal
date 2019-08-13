import java.util.Arrays;
import java.util.Random;

public class benchmark_mapping {
    //DAG of Tasks
    McDAG dag;
    String benchmark[];
    int benchmark_time[];

    public benchmark_mapping(McDAG dag, String[] benchmark, int[] benchmark_time) {
        this.dag = dag;
        this.benchmark = benchmark;
        this.benchmark_time = benchmark_time;
    }

    public void mapping(){
        Random rn= new Random();

        int t1=0;
        int t2=0;
        for (Vertex a : dag.getVertices()){
            t1=rn.nextInt(benchmark.length);
            a.setLO_name(benchmark[t1]);
            a.setWCET_LO(benchmark_time[t1]);
            System.out.println("%%%% >> "+benchmark_time[t1]);
            t2=rn.nextInt(benchmark.length);
            a.setHI_name(benchmark[t2]);
            a.setWCET_HI(benchmark_time[t2]+a.getWcet(0));


        }
    }

    public void cal_LPL(){
        for (Vertex a : dag.getVertices()) {
            a.setLPL(LPtoLeaves(a));
        }
    }

    public int cal_deadline(double n){
        Vertex v[]=dag.getVertices().stream().toArray(Vertex[]::new).clone();
        Arrays.sort(v);

        return (int) (v[v.length-1].getLPL()*3*n);
    }

    // A Recursive Method For Finding Longest Path To Leaves for Vertex
    public int LPtoLeaves(Vertex vertex){
        int LPL=0;
        if(vertex.isExitNode()){
            if(vertex.getWcet(0)>vertex.getWcet(1)){
                return vertex.getWcet(0);
            }else {
                return vertex.getWcet(1);
            }
        }

        for (Edge e : vertex.getSndEdges()){
            if(LPtoLeaves(e.getDest())>LPL) LPL=LPtoLeaves(e.getDest());
        }
        if(vertex.getWcet(0)>vertex.getWcet(1)){
            LPL+=vertex.getWcet(0);
        }else{
            LPL+=vertex.getWcet(1);
        }

        return LPL;
    }

    public void debug(){

        System.out.println(">>>>>>   MAPPING DEBUG MODE <<<<<<<");
        for (Vertex a : dag.getVertices()){
            System.out.println(a.getName());
            System.out.println("LO MODE Benchmark:  "+a.getLO_name());
            System.out.println("LO MODE Benchmark TIME:  "+a.getWcet(0));
            System.out.println("HI MODE Benchmark:  "+a.getHI_name());
            System.out.println("HI MODE Benchmark TIME:  "+a.getWcet(1));


        }
    }
}
