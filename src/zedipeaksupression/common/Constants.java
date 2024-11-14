package zedipeaksupression.common;

public final class Constants {
	/** The root <code>Context</code> ID. */
	public static final String CONTEXT_ID = "zedipeaksupression";
	public static final int DEFAULT_END_AT=24*4*7;
	public static final int INTERVAL=3;
	public static final double ALPHA= 0.2;
	
    public static final String YEAR_TEMPS_BUILDING_FILE = ".\\data\\FlatsRoomTemperatures.csv";
    public static final String YEAR_BASELOAD_FILE = ".\\data\\electricity_profiles_15m.csv";
    public static final String YEAR_OUTDOORS_FILE = ".\\data\\outdoor_conditions.csv";
    public static final String APPLIANCES_STATS = ".\\data\\appliances.csv";
    
    public static final String CONFIG_NET_2 = ".\\data\\ConfigNet_2.gml";
    
    public static final String PARAMETER_NETWORK_NAME = "netName";
    public static final String PARAMETER_NUMBER_FLATS = "numberofflats";
    public static final String PARAMETER_NUMBER_HVACS = "numberofhvacs";
    public static final String PARAMETER_NUMBER_DAYS = "numberofdays";
    public static final String PARAMETER_START_DATE = "startDate";
    public static final String PARAMETER_END_DATE = "endDate";
    public static final String PARAMETER_TIME_INTERVAL = "timeInterval";
    public static final String PARAMETER_HEATTIME_INTERVAL = "heatTimeInterval";
    public static final String PARAMETER_AVG_DEGREE = "avgDegree";
    public static final String PARAMETER_ALPHA = "alpha";
    		
	/** The <code>Space</code> ID. */
	public static final String SPACE_ID = "space";

	/** The <code>Grid</code> ID. */
	public static final String GRID_ID = "grid";
	
	public static final int TIME_INTERVAL=24;
	public static final int TIME_RESOLUTION =15;
	public static final int OBS_PER_HOUR = 60/TIME_RESOLUTION;
	
	//appliance uptake levels in %
	public static final int dishwasher_uptake = 45;
	public static final int washmachine_uptake = 78;
	public static final int tumbledryer_uptake = 41;
	public static final int washerdryer_uptake = 25;
	public static final int hvac_uptake = 71 ;
	/** Dimension of grid space */
	public static final int xdim=50;
	public static final int ydim=50;
	//dishwasher mean cycle length in minutes
	public static final  double dishwasher_mean_cycle_length=60;
	public static final  double dishwasher_mean_demand = 1.1306;
	public static final  double dishwasher_calibration = 0.007;

	// water boiler mean cycle length in minutes
	public static final double boiler_mean_cycle_length = 150;
	public static final double boiler_mean_demand = 12;
	

	//washing machine
	public static final  double washmachine_mean_cycle_length= 138;
	public static final  double washmachine_mean_demand = 0.9328;
	public static final  double washmachine_calibration = 0.05;
	//tumble dryer
	public static final  double tumbledryer_mean_cycle_length= 60;
	public static final  double tumbledryer_mean_demand = 2.5;
	public static final  double tumbledryer_calibration = 0.02;
	//washer dryer
	public static final  double washerdryer_mean_cycle_length= 198;
	public static final  double washerdryer_mean_demand = 2.6;
	public static final  double washerdryer_calibration = 0.05;

	public static final  double hvac_mean_demand =3.5;
	
	public static final int maxOccupantNo=5;
	public static final int minOccupantNo=1;
	
	//kwh
	public static final double highEnergyDemand = 0.05;
	
	public static final  double WORKING_HOURS = 8;

}
