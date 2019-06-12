/*******************************************************************************
 * Copyright (c) 2019 Porya Gohary
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static java.lang.Math.ceil;

public abstract class Vertex implements Comparable<Vertex>{
	
	public static final short LO = 0;
	public static final short HI = 1;

	private int id;
	private String name;
	
	private int[] wcets;
	
	private int cpFromNode[];
	
	private Set<Edge> rcvEdges;
	private Set<Edge> sndEdges;
	private int LPL;
	private Double reliability;
	private Double min_voltage;
	private int TSP_Active;
	//Safe Start Time
	private int SST;
	//Scheduled
	private int scheduled=0;
	private boolean running=false;

	private boolean done=false;


	
	public Vertex (int id, String name, int nbLevels) {
		this.setId(id);
		this.setName(name);
		wcets = new int[nbLevels];
		rcvEdges = new HashSet<Edge>();
		sndEdges = new HashSet<Edge>();
		cpFromNode = new int[nbLevels];
	}
	
	/**
	 * Returns the jth Ci(J)
	 *
	 */
	public int getWcet (int level) {
		return this.wcets[level];
	}
	
	
	/**
	 * Tests if the node is an exit node
	 * @return
	 */
	public boolean isExitNode() {
		if (this.getSndEdges().size() == 0)
			return true;
		else
			return false;
	}
	
	/**
	 * Calculates the critical Path from a given node
	 */
	public int CPfromNode (short mode) {
		
		if (this.getRcvEdges().size() == 0) {
			if (mode == 0) {
				getCpFromNode()[mode] = this.getWcets()[0];
				return this.getWcets()[0];
			} else {
				getCpFromNode()[mode] = this.getWcets()[1];
				return this.getWcets()[1];
			}
		} else {
			int max = 0;
			int tmp = 0;
			Iterator<Edge> it_e = this.getRcvEdges().iterator();
			
			while (it_e.hasNext()){
				Edge e = it_e.next();
				if (mode == VertexScheduling.LO) {
					tmp = e.getSrc().CPfromNode(VertexScheduling.LO);
					if (max < tmp)
						max = tmp;
				} else {
					tmp = e.getSrc().CPfromNode(VertexScheduling.HI);
					if (max < tmp)
						max = tmp;
				}
			}
			if (mode == VertexScheduling.LO) {
				max += this.getWcets()[0];
				getCpFromNode()[mode] = max;
			} else {
				max += this.getWcets()[1];
				getCpFromNode()[mode] = max;
			}
			
			return max;
		}
	}
	
	/**
	 * Calculates the critical Path from a given node

	 */
	public int CPfromNode (int level) {
		
		if (this.getRcvEdges().size() == 0) {
			this.getCpFromNode()[level] = this.getWcet(level);
			return this.getWcet(level);
		} else {
			int max = 0;
			int tmp = 0;
			Iterator<Edge> it_e = this.getRcvEdges().iterator();
			
			while (it_e.hasNext()){
				Edge e = it_e.next();
				
				tmp = e.getSrc().getCpFromNode()[level];
				if (max < tmp)
					max = tmp;
			}
			
			max += this.getWcet(level);
			this.getCpFromNode()[level] = max;

			return max;
		}
	}
	
	/**
	 * Returns all LOpredecessors of a node
	 * @return
	 */
	public Set<Vertex> getLOPred() {
		HashSet<Vertex> result = new HashSet<Vertex>();
		Iterator<Edge> ie = this.getRcvEdges().iterator();
		
		while (ie.hasNext()){
			Edge e = ie.next();
			if (e.getSrc().getWcets()[1] == 0) {
				result.add(e.getSrc());
				result.addAll(e.getSrc().getLOPred());
			}
		}
		return result;
	}
	
	/**
	 * Returns true if the actor is a source in L mode
	 * @param l
	 * @return
	 */
	public boolean isSourceinL (int l) {
		if (this.getWcet(l) == 0)
			return false;
		
		for (Edge e : this.getRcvEdges()) {
			if (e.getSrc().getWcet(l) != 0)
				return false;
		}
		return true;
	}
	
	/**
	 * Returns true if the vertex is a source in L mode on the dual
	 * @param l
	 * @return
	 */
	public boolean isSourceinLReverse (int l) {
		if (this.getWcet(l) == 0)
			return false;
		
		for (Edge e : this.getSndEdges()) {
			if (e.getDest().getWcet(l) != 0)
				return false;
		}
		return true;
	}
	
	/**
	 * Returns true if the actor is a sink in L mode
	 * @param l
	 * @return
	 */
	public boolean isSinkinL (int l) {
		if (this.getWcet(l) == 0)
			return false;
		for (Edge e : this.getSndEdges()) {
			if (e.getDest().getWcet(l) != 0)
				return false;
		}
		return true;
	}

	public int compareTo(Vertex obj)
	{
		// compareTo returns a negative number if this is less than obj,
		// a positive number if this is greater than obj,
		// and 0 if they are equal.
		return this.getLPL() - obj.getLPL();
	}

	public int getbigWCET(){
		return (this.getWcet(0) > this.getWcet(1)) ? this.getWcet(0) : this.getWcet(1);
	}
	
	/*
	 * Getters and setters
	 *
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int[] getWcets() {
		return wcets;
	}
	
	public void setWcets(int[] cIs) {
		this.wcets = cIs;
	}
	
	public Set<Edge> getRcvEdges() {
		return rcvEdges;
	}
	
	public void setRcvEdges(Set<Edge> rcvEdges) {
		this.rcvEdges = rcvEdges;
	}
	
	public Set<Edge> getSndEdges() {
		return sndEdges;
	}
	
	public void setSndEdges(Set<Edge> sndEdges) {
		this.sndEdges = sndEdges;
	}

	public int[] getCpFromNode() {
		return cpFromNode;
	}

	public void setCpFromNode(int cpFromNode[]) {
		this.cpFromNode = cpFromNode;
	}

	public void setLPL(int LPL) {
		this.LPL = LPL;
	}

	public int getLPL() {
		return LPL;
	}

	public void setReliability(Double reliability) {
		this.reliability = reliability;
	}

	public Double getReliability() {
		return reliability;
	}

	public Double getMin_voltage() {
		return min_voltage;
	}

	public void setMin_voltage(Double min_voltage) {
		this.min_voltage = min_voltage;
	}

	public int getTSP_Active() {return TSP_Active;	}

	public void setTSP_Active(int TSP_Active) {	this.TSP_Active = TSP_Active;}

	public void setSST(int SST) {
		this.SST = SST;
	}

	public int getSST() {
		return SST;
	}

	public int getScheduled() {
		return scheduled;
	}

	public void setScheduled(int scheduled) {
		this.scheduled = scheduled;
	}

	//Checking the execution of previous vertex
	public boolean check_runnable(String[] running_Tasks,double n){
	    //it's Determine that that Vertex is ROOT
        if(this.getRcvEdges().size()==0) {
//            System.out.println(this.getName());
//            System.out.println(this.getRcvEdges().size());
//            System.out.println("<<<<<<<<------>>>>>>>>");
            return true;
        }
        for (Edge e: this.getRcvEdges()){
            //System.out.print(e.getSrc().getName()+"     ");
            for (String t:running_Tasks){
                if (e.getSrc().getName().equals(t)) return false;
            }
            if(e.getSrc().scheduled!=ceil(n/2)) return false;

        }
//        System.out.println(this.getName());
//        System.out.println("<<<<<<<<------>>>>>>>>");
		return true;
	}

	public boolean reverse_running(String[] running_Tasks,double n){
		if(this.isExitNode()) return true;
		for (Edge e: this.getSndEdges()){
			//System.out.print(e.getSrc().getName()+"     ");
			if(!e.getDest().isHighCr()) continue;
			for (String t:running_Tasks){
				if (e.getDest().getName().equals(t)) return false;
			}
			if(e.getDest().scheduled!=ceil(n/2)) return false;

		}

		return true;
	}

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isHighCr(){
		return this.getWcet(1) != 0;
	}

	public void debug(){
		System.out.println("---------> DEBUG MODE <---------");
		System.out.println("              " + this.getName());
		System.out.println("Reliability= "+this.getReliability()+"   Min.Voltage= "+this.getMin_voltage());
		System.out.println("Max. Active Core= "+this.getTSP_Active());

	}
}
