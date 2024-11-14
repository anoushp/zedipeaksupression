package zedipeaksupression.agents;


import zedipeaksupression.common.Appliance;
import zedipeaksupression.common.NetworkUtils;
import zedipeaksupression.framework.Node;
import zedipeaksupression.framework.NodeController;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class Flat extends Node implements Comparable {
	private int occupantNo;
	private int floorLevel;
	private String action="none";
	private double averageConsumption;
	private String occupancyType;
	private double [] energyDemand;
	private double [] energyDemandScheduled;
	//sleep time and wake up time; leave to work and arrive from work time
	private int activateTime;
	private int deactivateTime;
	private int leaveWorkTime;
	private int arrivebackTime;
	private double probIncrease;
	private double probDecrease;
	
	private ArrayList<Appliance> appliances;
	private String incomeLevel;
	
    private double area;
    private ArrayList<Flat> socialInfluences;
	
	public Flat() {
		appliances = new ArrayList<Appliance>();
		

		
	}
	/**
	 * Implementation of the agent activity in each turn.
	 * 
	 */
	public void step(int step) {
		int neighbLessConsumption = 0;
		int neighbMoreConsumption = 0;
		String flatid;
		int flatnum;
		double average_neighb =this.getAverageNeighbourhood(step);
		this.setAverageConsumption(average_neighb);
		this.setAction("none");
		//check if one or more neighbours consume less energy for time t-1 to facilitate decision to increase or decrease
	for (Flat neighbour : this.socialInfluences)
	 {
		flatid =  neighbour.getId().replaceAll("[^0-9]", "");
		flatnum = Integer.valueOf(flatid)-1;
		//neghbour consumes high amount of energy
		// if (neighbour.getEnergyDemandTime(step-1) < 0.7)
		//if (neighbour.getEnergyDemandTime(step-1) < 0.013)NetworkUtils.maxloadflats[flatnum]
		//if (neighbour.getEnergyDemandTime(step-1) < average_neighb)
		if (neighbour.getEnergyDemandTime(step-1) < NetworkUtils.maxloadflats[flatnum])
				 neighbLessConsumption++;
		 else
			 neighbMoreConsumption ++;
	 }
	 this.probIncrease = (double)neighbLessConsumption/this.socialInfluences.size();
	 this.probDecrease = (double)neighbMoreConsumption/this.socialInfluences.size();
		
	}

	 public ArrayList<Flat> getSocialInfluences() {
		return socialInfluences;
	}
	public void setSocialInfluences(ArrayList<Flat> socialInfluences) {
		this.socialInfluences = socialInfluences;
	}
	public void step () {
		System.out.println("step");

     }

	//calculate resulting load after actions
   public void calculateFinalLoad(int step, ArrayList<Room> rooms ) {
	   
   }

	public int getOccupantNo() {
		return occupantNo;
	}



	public void setOccupantNo(int occupantNo) {
		this.occupantNo = occupantNo;
	}



	public String getOccupancyType() {
		return occupancyType;
	}



	public void setOccupancyType(String occupancyType) {
		this.occupancyType = occupancyType;
	}
		
	public int getFloorLevel() {
		return floorLevel;
	}
	public void setFloorLevel(int floorLevel) {
		this.floorLevel = floorLevel;
	}
	public double getArea() {
		return area;
	}
	public void setArea(double area) {
		this.area = area;
	}
	
	
	public double evaluateInfluenceUtility(Flat f,int step){
		double infUtility = 0;
		int increaseInfluence =0;
		
		ArrayList<Flat> flats=f.getSocialInfluences();
	/*	for (Flat fl:flats) {
			for (Room r : fl.getRooms()) {
				if (r.isHasHVAC()){
					if (r.get_action_ac(step))
						increaseInfluence ++;
					    break;
				}
			}
			
		}*/
		infUtility = increaseInfluence/(double)f.getSocialInfluences().size();
		return infUtility;
		
		
	}
	public ArrayList<Appliance> getAppliances() {
		return appliances;
	}
	public void setAppliances(ArrayList<Appliance> appliances) {
		this.appliances = appliances;
	}
	public void addAppliance(Appliance a) {
		this.appliances.add(a);
	}
	public String getIncomeLevel() {
		return incomeLevel;
	}
	public void setIncomeLevel(String incomeLevel) {
		this.incomeLevel = incomeLevel;
	}
	public int getActivateTime() {
		return activateTime;
	}
	public void setActivateTime(int activateTime) {
		this.activateTime = activateTime;
	}
	public int getDeactivateTime() {
		return deactivateTime;
	}
	public void setDeactivateTime(int deactivateTime) {
		this.deactivateTime = deactivateTime;
	}
	public int getLeaveWorkTime() {
		return leaveWorkTime;
	}
	public void setLeaveWorkTime(int leaveWorkTime) {
		this.leaveWorkTime = leaveWorkTime;
	}
	public int getArrivebackTime() {
		return arrivebackTime;
	}
	public void setArrivebackTime(int arrivebackTime) {
		this.arrivebackTime = arrivebackTime;
	}
	public double [] getEnergyDemand() {
		return energyDemand;
	}
	public void setEnergyDemand(double [] energyDemand) {
		this.energyDemand = energyDemand;
	}
	public void addEnergyDemand(int time, double demand) {
		this.energyDemand[time] += demand;
	}
	public void addEnergyDemandScheduled(int time, double demand) {
		this.energyDemandScheduled[time] += demand;
	}
	public double getEnergyDemandTime(int time) {
		return this.energyDemand[time];
	}
	public double getScheduledEnergyDemandTime(int time) {
		return this.energyDemandScheduled[time];
	}
	@Override
	public int compareTo(Object arg0) {
		Flat f1= (Flat)arg0;
		int fid1=0;
		int fid2=0;
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(f1.getId());
		if (m.find()) {
			fid1= Integer.parseInt(m.group());
		}
		m = p.matcher(this.getId());
		if (m.find()) {
			fid2= Integer.parseInt(m.group());
		}
		return (fid2-fid1);
		
	}
	public String toString() {
		 return this.getId();
	}
	public double getProbIncrease() {
		return probIncrease;
	}
	public void setProbIncrease(double probIncrease) {
		this.probIncrease = probIncrease;
	}
	public double getProbDecrease() {
		return probDecrease;
	}
	public void setProbDecrease(double probDecrease) {
		this.probDecrease = probDecrease;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public double getAverageNeighbourhood(int step) {
		double average_neighb =0;
		double num=this.socialInfluences.size();
		for (Flat neighbour : this.socialInfluences)
		 {
			average_neighb += neighbour.getEnergyDemandTime(step-1) ;
			
		}
		average_neighb =average_neighb/num;
		return average_neighb;
		
	}
	
	public double getSumNeighbEnergyDemand(int step, boolean included) {
		
		double sum_neighb = 0;
		String flatid;
		
		for (Flat neighbour : this.socialInfluences)
		 {
			if (!included) {
				flatid =  neighbour.getId();
				if (!this.getId().equals(flatid)) 
                    sum_neighb+= neighbour.getEnergyDemandTime(step-1) ;
				
			}
			else
				sum_neighb+= neighbour.getEnergyDemandTime(step-1) ;
			 
			
		}
		
		return sum_neighb;
		
	}
public double getSumNeighbScheduledEnergyDemand(int step, boolean included) {
		
		double sum_neighb = 0;
		String flatid;
		
		for (Flat neighbour : this.socialInfluences)
		 {
			if (!included) {
				flatid =  neighbour.getId();
				if (!this.getId().equals(flatid)) 
                    sum_neighb+= neighbour.getScheduledEnergyDemandTime(step-1) ;
				
			}
			else
				sum_neighb+= neighbour.getScheduledEnergyDemandTime(step-1) ;
			 
			
		}
		
		return sum_neighb;
		
	}
	public double getAverageConsumption() {
		return averageConsumption;
	}
	public void setAverageConsumption(double averageConsumption) {
		this.averageConsumption = averageConsumption;
	}
	public double [] getEnergyDemandScheduled() {
		return energyDemandScheduled;
	}
	public void setEnergyDemandScheduled(double [] energyDemandScheduled) {
		this.energyDemandScheduled = energyDemandScheduled;
	}
	
	
}
