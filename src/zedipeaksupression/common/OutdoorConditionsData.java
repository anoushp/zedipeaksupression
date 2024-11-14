package zedipeaksupression.common;

import java.io.FileNotFoundException;
import java.io.IOException;

public class OutdoorConditionsData {
	public double[] dryBulbAirTemp;
	public double[] infraredRadiation;
	public double[] diffuseSolarRadiation;
	public double[] directSolarRadiation;
	public static OutdoorConditionsData instance = null; 
	
	private OutdoorConditionsData() {
		
	}
	
	private OutdoorConditionsData(String s) {
		instance =new OutdoorConditionsData();
		instance.dryBulbAirTemp = new double [Constants.TIME_INTERVAL];
		instance.infraredRadiation = new double [Constants.TIME_INTERVAL];
		instance.diffuseSolarRadiation = new double [Constants.TIME_INTERVAL];
		instance.directSolarRadiation = new double [Constants.TIME_INTERVAL];
		InputReader ireader=new InputReader();
		double[][] outdoor_conditions =new double[Constants.TIME_INTERVAL][4];
		/*try {
			
			outdoor_conditions = ireader.readOutdoorsData(s, Constants.YEAR_OUTDOORS_FILE);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		for (int m=0; m< Constants.TIME_INTERVAL; m++) {
        	instance.dryBulbAirTemp[m] =outdoor_conditions[m][0];
        	instance.infraredRadiation[m] = outdoor_conditions[m][1];
        	instance.diffuseSolarRadiation[m] =outdoor_conditions[m][2];
        	instance.directSolarRadiation[m] =outdoor_conditions[m][3];
        }
	}
	
	public static OutdoorConditionsData getInstance(String s) 
    { 
        if (instance == null) 
        	instance = new OutdoorConditionsData(s); 
  
        return instance; 
    } 

}
