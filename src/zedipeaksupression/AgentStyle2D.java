package zedipeaksupression;

import java.awt.Color;
import java.awt.Font;


import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import zedipeaksupression.agents.BuildingCentralController;
import zedipeaksupression.agents.Flat;
import zedipeaksupression.agents.Room;
import zedipeaksupression.agents.ThermalComfortController;
import zedipeaksupression.common.NetworkUtils;


public class AgentStyle2D extends DefaultStyleOGL2D {

	@Override
	public Color getColor(Object o){
		if (o instanceof BuildingCentralController)
			return Color.ORANGE;
		else
		if (o instanceof Room)
			return Color.BLUE;
		
		else if (o instanceof Flat)
			if (((Flat) o).getActivateTime() >=NetworkUtils.stepNumber)
			return Color.RED;
			else return Color.GREEN;
		
		return null;
	}
	
	@Override
	public float getScale(Object o) {
		if (o instanceof BuildingCentralController)
			return 3f;
		if (o instanceof Flat)
			return 2f;
		
		else if (o instanceof Room)
		   return 1f;
		
		return 1f;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		if (o instanceof Room) {
			
			return "Room";
		}
		
		if (o instanceof Flat)
			return "Flat";
		
		if (o instanceof ThermalComfortController)
			return "TCC";
		
		return "Building Controller";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.visualizationOGL2D.StyleOGL2D#getLabelColor(java.lang
	 * .Object)
	 */
	public Color getLabelColor(Object object) {
		return Color.green;
	}
	
	public Font getLabelFont(Object object) {
		return new Font("SansSerif", Font.BOLD, 20);
	}


}