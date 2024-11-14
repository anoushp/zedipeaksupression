package zedipeaksupression.agents;

import zedipeaksupression.common.*;

public class Room {
	
	private double[] hvacDemand;
	
	private double[] insideTemp;
	private double leakage_rate;
	private Flat flat;
	private double fixedTemp;
	private double thermalDiscomf;
	private boolean hasHVAC;
	private String roomType; 
	private boolean[] action_acOn;
	private boolean[] action_hcOn;
	//m*m
	private double area;

	public Room ()	{
		
		hvacDemand = new double[Constants.TIME_INTERVAL];
		action_acOn = new boolean [Constants.TIME_INTERVAL];
		action_hcOn = new boolean [Constants.TIME_INTERVAL];
	}

	//set air conditioning action on for given timestep
	public void set_action_ac(int step, boolean a) {
		this.action_acOn[step]= a;
	}

	public boolean get_action_ac(int step) {
		return this.action_acOn[step];
	}

	//set heating action on for given timestep
	public void set_action_hc(int step, boolean a) {
		this.action_hcOn[step]= a;
	}

	public boolean get_action_hc(int step) {
		return this.action_hcOn[step];
	}

	
	public boolean[] getAction_acOn() {
		return action_acOn;
	}

	public void setAction_acOn(boolean[] action_acOn) {
		this.action_acOn = action_acOn;
	}

	public double getFixedTemp() {
		return fixedTemp;
	}

	public void setFixedTemp(double fixedTemp) {
		this.fixedTemp = fixedTemp;
	}

	public double[] getDemand() {
		return hvacDemand;

	}
	public void setDemand(double[] d) {
		this.hvacDemand = d;

	}
	
	public double[] getHvacDemand() {
		return hvacDemand;
	}
	public void setHvacDemand(double[] hvacDemand) {
		this.hvacDemand = hvacDemand;
	}
	
	public double[] getInsideTemp() {
		return insideTemp;
	}

	public void setInsideTemp(double[] insideTemp) {
		this.insideTemp = insideTemp;
	}


	public boolean isHasHVAC() {
		return hasHVAC;
	}

	public void setHasHVAC(boolean hasHVAC) {
		this.hasHVAC = hasHVAC;
	}

	public String getRoomType() {
		return roomType;
	}

	public void setRoomType(String roomType) {
		this.roomType = roomType;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}




	public Flat getFlat() {
		return flat;
	}

	public void setFlat(Flat flat) {
		this.flat = flat;
	}

	public void setAction(int step) {
		//write the logic why action should turn ac off or on
	}

	//Evaluate thermal discomfort for give time t and interval length. Note that discomfort id dependent on discomfort experienced at previous step.
	public double evaluateThermalDiscomfort(int timestep, int interval_length, double gamma) {
		//here possibly  we can check if the occupant is present e.g multiply by  occupant presence probability then evaluate otherwise leave the same value.
		this.thermalDiscomf = (1 - Math.sqrt(1- (Math.abs((insideTemp[timestep] - insideTemp[timestep-1])/fixedTemp)))*new Integer(interval_length).doubleValue()) + gamma*this.thermalDiscomf;
		return this.thermalDiscomf;
	}

	public double calculateHeatLoss(int timestep, int interval_length) {
		double leakage_rate = 0;
		double heat_loss = 0;
		double ac_on = 0;
		if (hasHVAC) {
			if (this.action_acOn[timestep]) ac_on = 1; else  ac_on = 0;

			heat_loss=FlatIndoorConfigurations.ac_power*ac_on - leakage_rate*(Math.abs(insideTemp[timestep]-OutdoorConditionsData.instance.dryBulbAirTemp[timestep]));

		}
		return heat_loss;

	}

	public double calculateHeatGain(int timestep, int interval_length) {
		double leakage_rate = 0;
		double heat_gain = 0;
		double hc_on = 0;
		if (hasHVAC) {
			if (this.action_hcOn[timestep]) hc_on = 1; else  hc_on = 0;

			heat_gain=FlatIndoorConfigurations.ac_power*hc_on + leakage_rate*(Math.abs(insideTemp[timestep]-OutdoorConditionsData.instance.dryBulbAirTemp[timestep]));

		}
		return heat_gain;

	}

	public void setInsideTempStep(int step, int interval_length) {
		double updated_temp = insideTemp[step-1];
		double heat_loss = this.calculateHeatLoss(step, interval_length);
		double ms_air=this.getArea()*FlatIndoorConfigurations.height*FlatIndoorConfigurations.air_ds;
		if (this.action_acOn[step-1])
			updated_temp= insideTemp[step-1] - (heat_loss*interval_length)/(FlatIndoorConfigurations.air_hc * ms_air);
		else if (this.action_hcOn[step-1])
			updated_temp= insideTemp[step-1] - (heat_loss*interval_length)/(FlatIndoorConfigurations.air_hc * ms_air);


		insideTemp[step] = updated_temp;
	}

	public double getThermalDiscomf() {
		return thermalDiscomf;
	}

	public void setThermalDiscomf(double thermalDiscomf) {
		this.thermalDiscomf = thermalDiscomf;
	}

	public boolean[] getAction_hcOn() {
		return action_hcOn;
	}

	public void setAction_hcOn(boolean[] action_hcOn) {
		this.action_hcOn = action_hcOn;
	}

	public double getLeakage_rate() {
		return leakage_rate;
	}

	public void setLeakage_rate(double leakage_rate) {
		this.leakage_rate = leakage_rate;
	}


	
	
}
