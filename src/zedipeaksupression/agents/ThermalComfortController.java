package zedipeaksupression.agents;

import zedipeaksupression.framework.NodeController;

public class ThermalComfortController implements NodeController {
	
	public void evaluateThermalDiscomfort(int timestep, int interval_length, double gamma, Room r) {
		double thermalDiscomf = 0;
		double[] insideTemp = r.getInsideTemp();
		//here possibly  we can check if the occupant is present e.g multiply by  occupant presence probability then evaluate otherwise leave the same value.
		thermalDiscomf = (1 - Math.sqrt(1- (Math.abs((insideTemp[timestep] - insideTemp[timestep-1])/r.getFixedTemp())))*new Integer(interval_length).doubleValue()) + gamma*r.getThermalDiscomf();
		r.setThermalDiscomf(thermalDiscomf);
	}

}
