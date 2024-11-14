package zedipeaksupression.common;

public final class FlatIndoorConfigurations {
	
	//Conductivity (roof (for top floor), ground (bottom floor) and external windows ) (W/m*K)
	//thickness in m
	public static final double wall_outer_finish = 0.84;
	public static final double wall_outer_finish_thickness =0.1;
	public static final double wall_insulation = 0.034;
	public static final double wall_insulation_thickness = 0.0795;
	public static final double wall_concrete = 0.51;
	public static final double wall_concrete_thickness = 0.1;
	public static final double wall_gypsum = 0.4;
	public static final double wall_gypsum_thickness = 0.013;
	
	public static final double roof_asphalt = 0.7;
	public static final double roof_asphalt_thickness = 0.01;
	public static final double roof_insulation = 0.04;
	public static final double roof_insulation_thickness = 0.1445;
	public static final double roof_plasterboard = 0.25;
	public static final double roof_plasterboard_thickness = 0.013;
	
	public static final double floor_earth= 1.28;
	public static final double floor_earth_thickness = 0.0191;
	public static final double floor_sand = 2;
	public static final double floor_sand_thickness = 0.1;
	public static final double floor_brick = 0.72;
	public static final double floor_brick_thickness = 0.075;

	//air heat capacity kWh/(kg C)]
	public static final double air_hc = 0.0001994;
	
	//air density kg/m3
	public static final double air_ds = 1.225;
	
	//power consumption of ac =cooling capacity/EER
	public static final double ac_power = 3.5;
	
	public static final double height =2.8;
}
