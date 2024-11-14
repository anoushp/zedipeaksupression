package zedipeaksupression.common;


import zedipeaksupression.framework.NodeProperty;

public abstract class Appliance implements NodeProperty {
	private String type;
	private double energyHourDemand;
	private boolean activeState;
	private double [] appEnergyDemand;
	
	public Appliance(String type, double energyHourDemand) {
		super();
		this.type = type;
		this.energyHourDemand=energyHourDemand;
		this.appEnergyDemand =new double [Constants.DEFAULT_END_AT];
		
		
	}
	@Override
	public double[] getDemand() {
		// TODO Auto-generated method stub
		return this.appEnergyDemand;
	}

	@Override
	public void setDemand(double[] d) {
		this.appEnergyDemand = d;
		
	}
	public void addEnergyDemand(int time, double demand) {
		this.appEnergyDemand[time] += demand;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	public double getEnergyHourDemand() {
		return energyHourDemand;
	}

	public void setEnergyHourDemand(double energyHourDemand) {
		this.energyHourDemand = energyHourDemand;
	}
	
	public double [] getAppEnergyDemand() {
		return appEnergyDemand;
	}
	public double  getAppEnergyDemand(int time) {
		return appEnergyDemand[time];
	}


	public void setAppEnergyDemand(double [] appEnergyDemand) {
		this.appEnergyDemand = appEnergyDemand;
	}
	public void setActiveState(boolean activeState) {
		this.activeState = activeState;
	}
	public boolean getActiveState() {
		return this.activeState;
	}
	

}
