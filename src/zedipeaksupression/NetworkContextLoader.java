package zedipeaksupression;


import zedipeaksupression.agents.BuildingCentralController;
import zedipeaksupression.agents.Flat;
import zedipeaksupression.agents.Room;
import zedipeaksupression.agents.ThermalComfortController;
import zedipeaksupression.common.Appliance;
import zedipeaksupression.common.Constants;
import zedipeaksupression.common.InputReader;
import zedipeaksupression.common.NetworkUtils;
import zedipeaksupression.common.NonShiftableAppliance;
import zedipeaksupression.common.OutdoorConditionsData;
import zedipeaksupression.common.ShiftableAppliance;
import zedipeaksupression.common.State;
import zedipeaksupression.framework.NodeController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.ptolemy.fmi.FMICallbackFunctions;
import org.ptolemy.fmi.FMILibrary;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMUFile;
import org.ptolemy.fmi.FMULibrary;
import org.ptolemy.fmi.FMULibrary.FMUAllocateMemory;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.DefaultEdgeCreator;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.util.SimUtilities;



public class NetworkContextLoader extends DefaultContext<Object> implements ContextBuilder<Object> {
	private Network<Object>  net=null;
	private ArrayList<Flat> flatList;
	
	//double[][] temps;
	double[][] base_loads ;
	double[][] outdoor_conditions;
	double[] dryBulbAirTemp;
	double[] infraredRadiation;
	double[] diffuseSolarRadiation;
	double[] directSolarRadiation;
	Calendar cal;
	String netName;
	int time_interval;
	int heat_time_interval;
	double alpha;
	int degree;
	static int count_time=0;
	
	double time =0;
	double stepSize=600.0;
	BuildingCentralController bcontroller1;
	//Function doStep = getFunction("_fmiDoStep");
	
	
	/*fmu test
	 * 
	 * 
	 * 
	 */
    FMUAllocateMemory _fmuAllocateMemory;

    /** The name of the .fmu file.
     *  The initial default is the empty string.
     */
    static String _fmuFileName = "C:/testfmu/fmu_export_actuator.fmu";
 // Parse the .fmu file.
    

    /** The modelIdentifier from modelDescription.xml. */
    String _modelIdentifier;

    /** The NativeLibrary that contains the functions. */
    NativeLibrary _nativeLibrary ;
;

    /** The output file name.
     *  The initial value is "results.csv".
     */
    static String _outputFileName = "results.csv";

    /** The step size, in seconds.
     *  The initial default is 0.1 seconds.
     */
    static double _stepSize = 0.1;
    
    Pointer fmiComponent;
	/**
	 * Builds and returns a context. Building a context consists of filling it with
	 * agents, adding projects and so forth. When this is called for the master context
	 * the system will pass in a created context based on information given in the
	 * model.score file. When called for subcontexts, each subcontext that was added
	 * when the master context was built will be passed in.
	 *
	 * @param context
	 * @return the built context.
	 */
	@Override
	public Context<Object> build(final Context<Object> context) {
		assert (context != null);
		//context.setId(Constants.CONTEXT_ID);
		//System.out.println("TTTTTT") ;
		Parameters p = RunEnvironment.getInstance().getParameters();
		// The environment parameters contain the user-editable values that appear in the GUI.
				//  Get the parameters p and then specifically the initial numbers of flats and days monitored.
		time_interval = (Integer)p.getValue(Constants.PARAMETER_TIME_INTERVAL);
		heat_time_interval = (Integer)p.getValue(Constants.PARAMETER_HEATTIME_INTERVAL);
		degree = (Integer)p.getValue(Constants.PARAMETER_AVG_DEGREE);
		alpha = (Double)p.getValue(Constants.PARAMETER_ALPHA);
		int numFlats = (Integer)p.getValue(Constants.PARAMETER_NUMBER_FLATS);
		cal= Calendar.getInstance();
		int numHvac = (Integer)p.getValue(Constants.PARAMETER_NUMBER_HVACS);
		int numofrooms=4;
		int numOfdays = (Integer)p.getValue(Constants.PARAMETER_NUMBER_DAYS);
		netName  = (String)p.getValue(Constants.PARAMETER_NETWORK_NAME);
		String startDate = (String)p.getValue(Constants.PARAMETER_START_DATE);
		String endDate = (String)p.getValue(Constants.PARAMETER_END_DATE);
	    Date start = NetworkUtils.parseStringtoDate(startDate);
	 //   System.out.println("start   "+ start);
	    cal.setTime(start);
	    Date end = NetworkUtils.parseStringtoDate(endDate);
	    int numofdays=(int)ChronoUnit.DAYS.between(start.toInstant(),end.toInstant());
		flatList = new ArrayList<Flat>();
		//temps =new double[Constants.TIME_INTERVAL][24];
		outdoor_conditions =new double[Constants.TIME_INTERVAL][4];
		dryBulbAirTemp = new double [Constants.TIME_INTERVAL*numOfdays];
		infraredRadiation = new double [Constants.TIME_INTERVAL*numOfdays];
		diffuseSolarRadiation = new double [Constants.TIME_INTERVAL*numOfdays];
		directSolarRadiation = new double [Constants.TIME_INTERVAL*numOfdays];
		NetworkUtils.stepNumber =0;
		NetworkUtils.stepTime =0;
		NetworkUtils.stepDay=0 ;
		// Set a specified context ID
		context.setId("zedipeaksupression");
		InputReader ireader=new InputReader();
		
		
		BuildingCentralController.network_name = (String) p.getValue(Constants.PARAMETER_NETWORK_NAME);
		
		try {
			
		//	temps= ireader.readRoomTemperatureData(12, 2, "01/01",  Constants.YEAR_TEMPS_BUILDING_FILE);
			base_loads=ireader.readBaseLoadData(numFlats, start, end , numofdays, Constants.YEAR_BASELOAD_FILE);
			outdoor_conditions = ireader.readOutdoorsData(start, end, numofdays, Constants.YEAR_OUTDOORS_FILE);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
     //   System.out.println("LEN  "+base_loads.length);
		int xdim = Constants.xdim;   // The x dimension of the physical space
		int ydim = Constants.ydim;   // The y dimension of the physical space

		// The inputs to the
		// GridFactory include the grid name, the context in which to place the grid,
		// and the grid parameters.  Grid parameters include the border specification,
		// random adder for populating the grid with agents, boolean for multiple occupancy,
		// and the dimensions of the grid.
		final Grid<Object> grid = GridFactoryFinder.createGridFactory(null).createGrid(Constants.GRID_ID, context,
				new GridBuilderParameters<Object>(new repast.simphony.space.grid.WrapAroundBorders(),
						new RandomGridAdder<Object>(), true, xdim, ydim));

		//   The inputs to the Space Factory include the space name, 
		// the context in which to place the space, border specification,
		// random adder for populating the grid with agents,
		// and the dimensions of the grid.
		final ContinuousSpace<Object> space = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
				.createContinuousSpace(Constants.SPACE_ID, context, new RandomCartesianAdder<Object>(),
						new repast.simphony.space.continuous.WrapAroundBorders(), xdim, ydim, 1);

		NetworkBuilder <Object > netBuilder = new NetworkBuilder <Object >("network1", context , false);
		net =netBuilder.buildNetwork();


		
		bcontroller1= new  BuildingCentralController(); 
		Topology tp = new Topology(BuildingCentralController.network_name,numFlats, 2*numFlats,degree);
		//OutdoorConditionsData od = OutdoorConditionsData.getInstance("01/01");
		for (int m=0; m< Constants.TIME_INTERVAL*numOfdays; m++) {
			dryBulbAirTemp[m] =outdoor_conditions[m][0];
			infraredRadiation[m] = outdoor_conditions[m][1];
			diffuseSolarRadiation[m] =outdoor_conditions[m][2];
			directSolarRadiation[m] =outdoor_conditions[m][3];
		}
		bcontroller1.setDryBulbAirTemp(dryBulbAirTemp);
		bcontroller1.setDiffuseSolarRadiation(diffuseSolarRadiation);	
		bcontroller1.setDirectSolarRadiation(directSolarRadiation);
		bcontroller1.setInfraredRadiation(infraredRadiation);
		context.add(bcontroller1);
		bcontroller1.setTopology(tp);

		
		createFlats(context,space, grid, net, bcontroller1, tp, numFlats, numofrooms);
		bcontroller1.setFlats(flatList);
		for (Flat f: bcontroller1.getFlats()) {
			f.setSocialInfluences(NetworkUtils.getInfluences(bcontroller1, tp.getInfluenceNodes(f.getId())));

		}
		
		SimUtilities.shuffle(flatList, RandomHelper.getUniform());
		initAppliances(flatList,numFlats);
	    initOccupancyandConnections(flatList);
	    Collections.sort(flatList);
		NetworkUtils.writeNetworkData(flatList, "_" + netName+"_" + netName + "_"+ String.valueOf(degree)+"_" + String.valueOf(heat_time_interval) + "h_" + String.valueOf(time_interval) + "h_" + String.valueOf(alpha).replace(".", ""));
		NetworkUtils.writeCSVApplianceUseTimes(flatList,
				"_" + netName + "_" + String.valueOf(degree)+"_"+ String.valueOf(heat_time_interval) + "h_"+String.valueOf(time_interval) + "h_" + String.valueOf(alpha).replace(".", ""));
		NetworkUtils.writeNonShiftedData(flatList, start, base_loads.length, "_" + netName +"_"+String.valueOf(degree)+"_"  + String.valueOf(heat_time_interval) + "h_"+ String.valueOf(time_interval) + "h_" + String.valueOf(alpha).replace(".", ""));
		NetworkUtils.setApplianceStatusFalse(flatList);
		long start_time = System.nanoTime();
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		schedule.schedule(ScheduleParameters.createRepeating(1, 1), this,
				"activateAgents");

		// The model stopping rule is changed: the model stops after  time
		// steps have been executed... [*] (see activateAgents())
		RunEnvironment.getInstance().endAt(Constants.DEFAULT_END_AT);
		long end_time = System.nanoTime();
		double difference = (end_time - start_time) * 0.000001;
		System.out.println("time");
		System.out.println(difference);
		
		
		
		/*FMIModelDescription fmiModelDescription = null;
		String sharedLibrary;
		String fmuLocation = null;
		String mimeType= "application/x-fmu-sharedlibrary";
		 double timeout = 1000;
         // There is no simulator UI.
         byte visible = 0;
         // Run the simulator without user interaction.
         byte interactive = 0;
		
		 // Parse the .fmu file.
		try {
         fmiModelDescription = FMUFile
                .parseFMUFile(_fmuFileName);
         sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);
         _modelIdentifier = fmiModelDescription.modelIdentifier;
         System.out.println("AAAAAAAAAAA"); 
         // The URL of the fmu file.
         fmuLocation = new File(_fmuFileName).toURI().toURL().toString();
         // The tool to use if we have tool coupling.
         _nativeLibrary =  NativeLibrary.getInstance(sharedLibrary);
        
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		FMICallbackFunctions.ByValue callbacks = new FMICallbackFunctions.ByValue(
				new FMULibrary.FMULogger(), fmiModelDescription.getFMUAllocateMemory(),
		                new FMULibrary.FMUFreeMemory(),
		                new FMULibrary.FMUStepFinished());
		        // Logging tends to cause segfaults because of vararg callbacks.
		        byte loggingOn =  (byte) 1 ;
		        loggingOn = (byte) 0;
		        System.out.println("LOC:" + fmuLocation);
		        Function instantiateSlave = getFunction(_modelIdentifier,"_fmiInstantiateSlave");
		        fmiComponent = (Pointer) instantiateSlave.invoke(Pointer.class,
		                new Object[] { _modelIdentifier, fmiModelDescription.guid,
		                        "file:/C:/Users/ap647/AppData/Local/Temp/FMUFile115429109166530041.tmp/", mimeType, timeout, visible, interactive,
		                        callbacks, loggingOn });
		        if (fmiComponent.equals(Pointer.NULL)) {
		            throw new RuntimeException("Could not instantiate model.");
		        }

		        invoke(getFunction(_modelIdentifier,"_fmiInitializeSlave"), new Object[] { fmiComponent, 0,
		                (byte) 1, 259200.0 }, "Could not initialize slave: ");*/
	  // If running in batch mode, tell the scheduler when to end each run.
		if (RunEnvironment.getInstance().isBatch()){
			
		//	double endAt = (Double)p.getValue("runlength"); 
			
			RunEnvironment.getInstance().endAt(Constants.DEFAULT_END_AT);
		}
		return context;                       
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 0)
	public void activateAgents() {
		/* Function doStep = getFunction(_modelIdentifier,"_fmiDoStep");
		 invoke(doStep, new Object[] { fmiComponent, time, stepSize,
                 (byte) 1 }, "Could not simulate, time was " + time
                 + ": ");
		 time +=stepSize;*/
        Collections.shuffle(flatList);
		double [] out_temp=bcontroller1.getDryBulbAirTemp();
		double outdoor_temp;
		String flatid;
		int flatnum=0;
		//System.out.println("tick" +NetworkUtils.stepNumber );
		long start_time = System.nanoTime();
		
		if (NetworkUtils.stepNumber % 96 ==0) {
			NetworkUtils.stepDay +=1;
			NetworkUtils.stepTime = 0;
		}
	/*	if (NetworkUtils.stepNumber % 4 ==0) {
			outdoor_temp = out_temp[(int)(NetworkUtils.stepNumber / 4)];
		}*/
			
		for (Flat fl:flatList) {
			if (NetworkUtils.stepNumber > 0) {
				fl.step(NetworkUtils.stepNumber);
				flatid =  fl.getId().replaceAll("[^0-9]", "");
				flatnum = Integer.valueOf(flatid)-1;
				// check if energy demand is high
				//if (fl.getEnergyDemandTime(NetworkUtils.stepNumber - 1) >= fl.getAverageConsumption()) {
			//	if (fl.getEnergyDemandTime(NetworkUtils.stepNumber - 1) >= NetworkUtils.maxloadflats[flatnum]) {
				//System.out.println("NOT Included " + fl.getSumNeighbEnergyDemand(NetworkUtils.stepNumber, false));
				//System.out.println("Included " + fl.getSumNeighbEnergyDemand(NetworkUtils.stepNumber, true));
				//System.out.println("MAX " + NetworkUtils.maxsumloadflats[flatnum]);
				if (fl.getEnergyDemandTime(NetworkUtils.stepNumber - 1) >= 
						(alpha*NetworkUtils.maxsumloadflats[flatnum]- fl.getSumNeighbEnergyDemand(NetworkUtils.stepNumber, false))) {
					System.out.println("energy demand " + fl.getEnergyDemandTime(NetworkUtils.stepNumber - 1));
					System.out.println("NOT Included " + fl.getSumNeighbEnergyDemand(NetworkUtils.stepNumber, false));
					System.out.println("Included " + fl.getSumNeighbEnergyDemand(NetworkUtils.stepNumber, true));
					System.out.println("MAX " + NetworkUtils.maxsumloadflats[flatnum]);
				//if (fl.getEnergyDemandTime(NetworkUtils.stepNumber - 1) >= 0.7) {
					//System.out.println("HIGH " + fl.getProbDecrease());
					//System.out.println(" " + fl.getProbIncrease());
					// with probaility of decreasing loads depending on neighbours shift the loads
			//		if (Math.random() < fl.getProbDecrease()) {
						// select appliances that are on and candidates for load shifting
						// Shift the load;
						NetworkUtils.shiftLoad(fl, "decrease",time_interval*Constants.OBS_PER_HOUR,heat_time_interval*Constants.OBS_PER_HOUR, alpha);
						//fl.setAction("decrease");
				//	}

				} else {
					//if (Math.random() < fl.getProbIncrease()) {
						// select appliances that are on and candidates for load increase
						NetworkUtils.shiftLoad(fl, "increase", time_interval*Constants.OBS_PER_HOUR,heat_time_interval*Constants.OBS_PER_HOUR,alpha);
						//fl.setAction("increase");
				//	}

				}
			}
			NetworkUtils.executePlannedLoad(fl);
			
			
			
			//System.out.println("STARTTIME" +fl.getActivateTime());
		}
		NetworkUtils.writeCSVfile(flatList, cal, "_"+netName+"_"+String.valueOf(degree)+"_" + String.valueOf(heat_time_interval) + "h_" + String.valueOf(time_interval) + "h_" + String.valueOf(alpha).replace(".", ""));
		cal.add(Calendar.MINUTE, 15);
		NetworkUtils.stepNumber = NetworkUtils.stepNumber + 1;
		NetworkUtils.stepTime = NetworkUtils.stepTime + 1;
		
		
	}
	private void initAppliances(ArrayList<Flat> flatList,int numFlats ) {
		
		// initialise dishwasher
		int dishwasher_num = flatList.size() * Constants.dishwasher_uptake / numFlats;
		for (int i = 0; i <= dishwasher_num; i++) {
			Flat f = flatList.get(i);
			ShiftableAppliance dishWasher = new ShiftableAppliance("Dishwasher", Constants.dishwasher_mean_cycle_length,
					Constants.dishwasher_mean_demand);
			f.addAppliance(dishWasher);
		};
		Collections.shuffle(flatList);
		int washmachine_num = flatList.size() * Constants.washmachine_uptake/ numFlats;
		for (int i = 0; i <= washmachine_num; i++) {
			Flat f = flatList.get(i);
			ShiftableAppliance washMachine=new ShiftableAppliance("WashingMachine",Constants.washmachine_mean_cycle_length,Constants.washmachine_mean_demand);
			f.addAppliance(washMachine);
		};
		Collections.shuffle(flatList);
		int washdryer_num = flatList.size() * Constants.washerdryer_uptake/ numFlats;
		for (int i = 0; i <= washdryer_num; i++) {
			Flat f = flatList.get(i);
			ShiftableAppliance washerDryer=new ShiftableAppliance("WasherDryer",Constants.washerdryer_mean_cycle_length,Constants.washerdryer_mean_demand);
			f.addAppliance(washerDryer);
		};
		Collections.shuffle(flatList);
		int tumbledryer_num = flatList.size() * Constants.tumbledryer_uptake/ numFlats;
		for (int i = 0; i <= tumbledryer_num; i++) {
			Flat f = flatList.get(i);
			ShiftableAppliance tumbleDryer=new ShiftableAppliance("TumbleDryer",Constants.tumbledryer_mean_cycle_length,Constants.tumbledryer_mean_demand);
			f.addAppliance(tumbleDryer);
		};
		Collections.shuffle(flatList);
		int hvac_num = flatList.size() * Constants.hvac_uptake/ numFlats;
		for (int i = 0; i <= hvac_num; i++) {
			Flat f = flatList.get(i);
			Appliance hvac=new NonShiftableAppliance("hvac",Constants.hvac_mean_demand);
			f.addAppliance(hvac);
		};
		Collections.shuffle(flatList);
		
		for (int i = 0; i < numFlats; i++) {
			Flat f = flatList.get(i);
			ShiftableAppliance boiler=new ShiftableAppliance("boiler",Constants.boiler_mean_cycle_length,Constants.boiler_mean_demand);
			f.addAppliance(boiler);
			NetworkUtils.generateBoilerActivity(boiler, f);
			
		};
			
	
	}
	private void initOccupancyandConnections(ArrayList<Flat> flatList ) {
		for (int i=0; i<flatList.size();i++) {
			Flat f= flatList.get(i);
			if (i>=0 && i <20) {
				f.setOccupantNo(1);
				f.setOccupancyType("Employed");
			}
			if (i>=20 && i <30) {
				f.setOccupantNo(1);
				f.setOccupancyType("Retired");
			}
			if (i>=30 && i <80) {
				f.setOccupantNo(2);
				f.setOccupancyType("Employed");
			}
			if (i>=80 ) {
				f.setOccupantNo(2);
				f.setOccupancyType("Unemployed");

			}

			if (f.getOccupancyType().equals("Employed")) {
				f.setLeaveWorkTime(f.getActivateTime() + NetworkUtils.getTruncatedGauss(3*Constants.OBS_PER_HOUR, 1*Constants.OBS_PER_HOUR));
				f.setArrivebackTime(f.getLeaveWorkTime() + NetworkUtils.getTruncatedGauss(Constants.WORKING_HOURS*Constants.OBS_PER_HOUR, 2*Constants.OBS_PER_HOUR));
			}
			ArrayList<Appliance> shifta = f.getAppliances();
			for (Appliance a :shifta) {
				if (a instanceof ShiftableAppliance && !a.getType().equals("boiler")) {
					ShiftableAppliance sa=(ShiftableAppliance)a;
					//set activity depending on number of occupants etc
					if (f.getOccupantNo()>=1 && f.getOccupantNo()<3)
						NetworkUtils.generateApplianceActivity(sa, f, 2);
					else
						NetworkUtils.generateApplianceActivity(sa, f, 3);
				//	System.out.println("FlatID "+f.getId());
					//for (int j=0; j<sa.getUseTimes().length;j++) {
					//	System.out.print(sa.getUseTimes()[j]+"a ");
					//}
					//System.out.println();
				}
			}
			for (Flat connectedFlat: f.getSocialInfluences()) {
				DefaultEdgeCreator defEdge = new DefaultEdgeCreator();
				RepastEdge edge = defEdge.createEdge(f , connectedFlat, false,0);
				
				if (!net.containsEdge(edge))
					net.addEdge(f , connectedFlat);
				
			} 

		}
	}
	
	private void createFlats(final Context<Object> context,
			final ContinuousSpace<Object> space, final Grid<Object> grid, Network<Object>  net,  BuildingCentralController bcontroller, Topology tp, int numOfFlats,
			int numofrooms )
	{
		assert (context != null);
		assert (space != null);
		assert (grid != null);

		int tempIndex=0;
		int flatLoadIndex=0;
		double [] baseloads= new double[base_loads.length];
		for (int i = 0; i < numOfFlats; i++) {

			Flat flat = new Flat();
			flat.setId("F"+ (i+1));
		    Random rand=new Random();
		    

		    // nextInt is normally exclusive of the top value,
		    // so add 1 to make it inclusive
		    int occNum = rand.nextInt((Constants.maxOccupantNo - Constants.minOccupantNo) + 1) + Constants.minOccupantNo;
		    flat.setOccupantNo(occNum);
			/*
			 * could add distribution depending if flat has mashmachine or not.
			 * */
			flat.setActivateTime(NetworkUtils.getTruncatedGauss(7*Constants.OBS_PER_HOUR, 1*Constants.OBS_PER_HOUR));
			flat.setDeactivateTime(NetworkUtils.getTruncatedGauss(23*Constants.OBS_PER_HOUR, 2*Constants.OBS_PER_HOUR));
			
				
			if (i>=0 && i<4)
				flat.setFloorLevel(0);
			else if (i>=4 && i<8)
				flat.setFloorLevel(1);
			else if (i>=8 && i<12)
				flat.setFloorLevel(2);
			ArrayList<NodeController> controllers = new ArrayList<NodeController>();
			
			for (int t=0; t<base_loads.length; t++)
			{
				baseloads[t]=base_loads[t][flatLoadIndex];

			}
			//flat.setBaseLoads(baseloads);
			double [] energyDemand=new double[baseloads.length];
		    System.arraycopy(baseloads, 0, energyDemand, 0, baseloads.length);
		    flat.setEnergyDemand(energyDemand);
		    double [] energyDemandScheduled=new double[baseloads.length];
		    System.arraycopy(baseloads, 0, energyDemandScheduled, 0, baseloads.length);
		    flat.setEnergyDemandScheduled(energyDemandScheduled);
			//flat.setBaseLoads(baseloads);
			context.add(flat);
			//ArrayList<Room> rooms=new ArrayList<Room>();
			
			//Room room;
			//double leakage_rate;
			NdPoint pt = space.getLocation(flat);
			grid.moveTo(flat, (int) pt.getX(), (int) pt.getY());// create a new flat
			// add the new flat to the root context

			//ThermalComfortController tcc=new ThermalComfortController();
			//controllers.add(tcc);
			//flat.setControllers(controllers);
		//	context.add(tcc);
			NdPoint pttcc = space.getLocation(flat);
			grid.moveTo(flat, (int) pttcc.getX(), (int) pttcc.getY());// create a new controller
			// add the new controller to the root context

			DefaultEdgeCreator defEdge = new DefaultEdgeCreator();
			RepastEdge edge = defEdge.createEdge(bcontroller, flat, false,0);

			if (!net.containsEdge(edge))
				//net.addEdge(bcontroller, flat);
			
			
			
			//edge = defEdge.createEdge(flat, tcc, false,0);

		//	if (!net.containsEdge(edge))
			//	net.addEdge(flat, tcc);


			/*for (int j = 0; j < numofrooms; j++) {
				room = new Room();
				room.setFlat(flat);
				double [] insidetemps= new double[Constants.TIME_INTERVAL];
				context.add(room);

				if (j<2)
					room.setHasHVAC(false);
				else 
				{
					leakage_rate = NetworkUtils.calculateRoomLeakageRate(flat.getFloorLevel());
					room.setHasHVAC(true);
					room.setLeakage_rate(leakage_rate);
					for (int t=0; t<Constants.TIME_INTERVAL; t++)
					{
						insidetemps[t]=temps[t][tempIndex];
					}
					room.setInsideTemp(insidetemps);
					tempIndex++;
				}

				NdPoint pt1 = space.getLocation(room);
				grid.moveTo(room, (int) pt1.getX(), (int) pt1.getY());
				edge = defEdge.createEdge(flat,room, false,0);
				//System.out.println("GETCODE" +hsList.get(Integer.parseInt(splited[j]) - 1));
				if (!net.containsEdge(edge))
					net.addEdge(flat, room);
				rooms.add(room);
				



			}*/
		/*	rooms.get(0).setRoomType("Kitchen");
			rooms.get(0).setArea(5.11);
			rooms.get(1).setRoomType("LivingRoom");
			rooms.get(1).setArea(10.26);
			rooms.get(2).setRoomType("Bedroom");
			rooms.get(2).setArea(8.1);
			rooms.get(3).setRoomType("Bathroom");
			rooms.get(3).setArea(2.8);*/
			
			flat.setArea(26.27);
			
			flatLoadIndex ++;
          
			flatList.add(flat);
		}

	}
	public Function getFunction(String _modelIdentifier, String name) {
        // This is syntactic sugar.
        
            System.out.println("FMUModelExchange: about to get the " + _modelIdentifier + name
                    + " function.");
        
        return _nativeLibrary.getFunction(_modelIdentifier + name);
    }
	 public void invoke(Function function, Object[] arguments, String message) {
	       
	            System.out.println("About to call " + function.getName());
	        
	        int fmiFlag = ((Integer) function.invoke(Integer.class, arguments))
	                .intValue();
	        if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
	            throw new RuntimeException(message + fmiFlag);
	        }
	    }

	

}
