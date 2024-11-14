package zedipeaksupression.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import zedipeaksupression.agents.BuildingCentralController;
import zedipeaksupression.agents.Flat;

public final strictfp class NetworkUtils {
	public static int stepNumber;
	public static int stepTime;
	public static int stepDay;

	public static double[] avgloadflats;
	public static double[] maxloadflats;
	public static double[] maxsumloadflats;

	/**
	 * Hidden constructor to ensure no instances are created.
	 */
	private NetworkUtils() {
		;
	}

	public static double calculateRoomLeakageRate(int floorLevel) {
		double leakage_rate = 0;
		if (floorLevel == 0) {
			leakage_rate = FlatIndoorConfigurations.floor_brick * FlatIndoorConfigurations.floor_brick_thickness
					+ FlatIndoorConfigurations.floor_earth * FlatIndoorConfigurations.floor_earth_thickness
					+ FlatIndoorConfigurations.floor_sand * FlatIndoorConfigurations.floor_sand_thickness
					+ FlatIndoorConfigurations.floor_brick * FlatIndoorConfigurations.floor_brick_thickness
					+ 4 * (FlatIndoorConfigurations.wall_outer_finish
							* FlatIndoorConfigurations.wall_outer_finish_thickness
							+ FlatIndoorConfigurations.wall_insulation
									* FlatIndoorConfigurations.wall_insulation_thickness
							+ FlatIndoorConfigurations.wall_concrete * FlatIndoorConfigurations.wall_concrete_thickness
							+ FlatIndoorConfigurations.wall_gypsum * FlatIndoorConfigurations.wall_gypsum_thickness);
		} else if (floorLevel == 2) {
			leakage_rate = FlatIndoorConfigurations.roof_asphalt * FlatIndoorConfigurations.roof_asphalt_thickness
					+ FlatIndoorConfigurations.roof_insulation * FlatIndoorConfigurations.roof_insulation_thickness
					+ FlatIndoorConfigurations.roof_plasterboard * FlatIndoorConfigurations.roof_plasterboard_thickness
					+ 4 * (FlatIndoorConfigurations.wall_outer_finish
							* FlatIndoorConfigurations.wall_outer_finish_thickness
							+ FlatIndoorConfigurations.wall_insulation
									* FlatIndoorConfigurations.wall_insulation_thickness
							+ FlatIndoorConfigurations.wall_concrete * FlatIndoorConfigurations.wall_concrete_thickness
							+ FlatIndoorConfigurations.wall_gypsum * FlatIndoorConfigurations.wall_gypsum_thickness);
		} else
			leakage_rate = 4
					* (FlatIndoorConfigurations.wall_outer_finish * FlatIndoorConfigurations.wall_outer_finish_thickness
							+ FlatIndoorConfigurations.wall_insulation
									* FlatIndoorConfigurations.wall_insulation_thickness
							+ FlatIndoorConfigurations.wall_concrete * FlatIndoorConfigurations.wall_concrete_thickness
							+ FlatIndoorConfigurations.wall_gypsum * FlatIndoorConfigurations.wall_gypsum_thickness);

		return leakage_rate;

	}

	public static ArrayList<Flat> getInfluences(BuildingCentralController bc, Set<String> inf) {
		ArrayList<Flat> infFlats = new ArrayList<Flat>();
		ArrayList<Flat> flats = bc.getFlats();
		for (String s : inf) {
			for (Flat f : flats) {
				if (f.getId().equals(s)) {
					infFlats.add(f);
					break;
				}
			}

		}

		return infFlats;

	}

	// gets a number from truncated gaussian distribution and rounds it up to given
	// resolution.
	public static int getTruncatedGauss(double mean, double deviation) {
		double value = 0;
		Random r = new Random();
		do {
			value = mean + r.nextGaussian() * deviation;

		} while (value < (mean - deviation) || value > (mean + deviation));
		// System.out.println("VALUE" +value);
		// System.out.println("Round" +(int) (Math.round(value)));
		return (int) (int) (Math.round(value));
	}

	public static void generateBoilerActivity(ShiftableAppliance a, Flat f) {
		// generate boiler activity for 7 days
		int[] activityDays = { 0, 1, 2, 3, 4, 5, 6 };
		ArrayList<ScheduleTime> as = new ArrayList<ScheduleTime>();
		int[][] activityTimes = new int[7][2];

		a.setUseDays(activityDays);
		for (int i = 0; i < activityDays.length; i++) {
			int day = activityDays[i];

			activityTimes[day][0] = ThreadLocalRandom.current().nextInt(6 * Constants.OBS_PER_HOUR,
					8 * Constants.OBS_PER_HOUR);
			activityTimes[day][1] = ThreadLocalRandom.current().nextInt(17 * Constants.OBS_PER_HOUR,
					19 * Constants.OBS_PER_HOUR);
			System.out.println("Activity1 " + day + " " + activityTimes[day][0]);
			System.out.println("Activity2 " + day + " " + activityTimes[day][1]);
			ScheduleTime sc = new ScheduleTime(day, activityTimes[day][0]);
			ScheduleTime sc1 = new ScheduleTime(day, activityTimes[day][1]);
			as.add(sc);
			as.add(sc1);

		}

		System.out.println("Rows " + activityTimes.length); // row
		System.out.println("coulmns " + activityTimes[0].length);
		a.setUseTimes(activityTimes);

		a.setScheduleTimes(as);

	}

	public static void generateApplianceActivity(ShiftableAppliance a, Flat f, int ndays) {
		// randomly generate sample of length n days from 0 to 7 range
		int[] activityDays = ThreadLocalRandom.current().ints(0, 7).distinct().limit(ndays).toArray();
		ArrayList<ScheduleTime> as = new ArrayList<ScheduleTime>();
		int[][] activityTimes = new int[7][1];
		double rand = 0;
		a.setUseDays(activityDays);
		for (int i = 0; i < activityDays.length; i++) {
			int day = activityDays[i];
			do {
				if (f.getOccupancyType().equals("Employed")) {
					rand = ThreadLocalRandom.current().nextDouble(1.0);
					// more likely in the evening than int the morning
					if (rand < 0.3)
						activityTimes[day][0] = ThreadLocalRandom.current().nextInt(f.getActivateTime(),
								f.getLeaveWorkTime());
					else if (rand < 0.8)
						activityTimes[day][0] = ThreadLocalRandom.current().nextInt(
								f.getArrivebackTime() + Constants.OBS_PER_HOUR,
								f.getArrivebackTime() + 3 * Constants.OBS_PER_HOUR);
					else
						activityTimes[day][0] = ThreadLocalRandom.current().nextInt(21 * Constants.OBS_PER_HOUR,
								24 * Constants.OBS_PER_HOUR);
				} else
					activityTimes[day][0] = ThreadLocalRandom.current().nextInt(f.getActivateTime(),
							f.getDeactivateTime());

			} while (((day == 6) && (activityTimes[day][0] + a.getDuration() >= 96)) || activityTimes[day][0] >= 96);
		}

		for (int i = 0; i < activityDays.length; i++) {
			int d = activityDays[i];
			ScheduleTime sc = new ScheduleTime(d, activityTimes[d][0]);
			as.add(sc);
		}
		a.setUseTimes(activityTimes);
		a.setScheduleTimes(as);

	}

	public static Date parseStringtoDate(String ds) {
		Date dt = new Date();
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat();
			sdf1.applyPattern("dd/MM/yyyy HH:mm");
			dt = sdf1.parse(ds);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return dt;

	}

	public static void writeCSVApplianceUseTimes(ArrayList<Flat> flats, String sc) {
		FileWriter writer1;
		int hour = 0;
		int min = 0;
		int rate = 60 / Constants.OBS_PER_HOUR;

		try {
			writer1 = new FileWriter("ABMFlatsApplianceUsageTimes" + sc + ".csv", true);
			for (Flat f : flats) {

				ArrayList<Appliance> applist = f.getAppliances();

				for (int i = 0; i < applist.size(); i++) {

					if (applist.get(i) instanceof ShiftableAppliance) {

						ShiftableAppliance shiftapp = (ShiftableAppliance) applist.get(i);
						for (int j = 0; j < shiftapp.getUseTimes().length; j++) {
							System.out.println(applist.get(i).getType());
							System.out.println("Rows " + shiftapp.getUseTimes().length); // row
							System.out.println("coulmns " + shiftapp.getUseTimes()[0].length);
							System.out.println("BBBBBBBBB +" + shiftapp.getUseTimes()[j].length);
							for (int k = 0; k < shiftapp.getUseTimes()[j].length; k++) {
								// System.out.println("TTTTTTTTTTTTTTTTTTT");

								if (shiftapp.getUseTimes()[j][k] != 0) {
									writer1.append(f.getId());
									writer1.append(',');
									writer1.append(applist.get(i).getType());
									writer1.append(',');
									writer1.append(String.valueOf(j));
									writer1.append(',');
									hour = shiftapp.getUseTimes()[j][k] / Constants.OBS_PER_HOUR;
									min = (shiftapp.getUseTimes()[j][k] % Constants.OBS_PER_HOUR) * rate;

									writer1.append(String.valueOf(shiftapp.getUseTimes()[j][k]));
									writer1.append(',');
									writer1.append(String.valueOf(hour));
									writer1.append(',');
									writer1.append(String.valueOf(min));
									writer1.append('\n');

								}
							}
						}
					} else {
						writer1.append(f.getId());
						writer1.append(',');
						writer1.append(applist.get(i).getType());

						writer1.append('\n');
					}

				}

			}
			writer1.flush();
			writer1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void writeCSVfile(ArrayList<Flat> flats, Calendar cal, String sc) {

		FileWriter writer1;
		FileWriter writer2;
		FileWriter writer3;
		FileWriter writer4;
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

		try {
			File f1 = new File("ABMLoad_" + sc + ".csv");

			writer2 = new FileWriter("ABMappliance_loads" + sc + ".csv", true);
			writer3 = new FileWriter("ABMagentStatesActions" + sc + ".csv", true);
			writer4 = new FileWriter("ABMheatingSchedules" + sc + ".csv", true);
			// then append headers for each csv

			Collections.sort(flats);
			// add heading if new file
			if (f1.createNewFile()) {
				writer1 = new FileWriter(f1, true);
				writer1.append("DateTime");
				writer1.append(',');
				for (Flat f : flats) {
					writer1.append(f.getId());
					writer1.append(',');
				}
				writer1.append('\n');

			} else
				writer1 = new FileWriter(f1, true);

			writer1.append(dateFormat.format(cal.getTime()));
			writer1.append(',');
			// writer1.append(String.valueOf(NetworkUtils.stepNumber));
			// writer1.append(',');
			for (Flat f : flats) {
				ArrayList<Appliance> applist = f.getAppliances();
				int shiftappnum = 0;
				int stateonappnum = 0;
				writer3.append(String.valueOf(NetworkUtils.stepNumber));
				writer3.append(',');
				writer3.append(f.getId());
				writer3.append(',');
				writer4.append(String.valueOf(NetworkUtils.stepNumber));
				writer4.append(',');
				writer4.append(String.valueOf(NetworkUtils.stepDay));
				writer4.append(',');
				writer4.append(String.valueOf(NetworkUtils.stepTime));
				writer4.append(',');
				writer4.append(f.getId());
				writer4.append(',');

				for (int i = 0; i < applist.size(); i++) {
					Appliance app = applist.get(i);
					if (app instanceof ShiftableAppliance) {
						shiftappnum++;
						if (app.getActiveState())
							stateonappnum++;
					    if (app.getType().equals("boiler")) {
					    	ShiftableAppliance boiler = (ShiftableAppliance)app;
					    	int[][] useTimes = boiler.getUseTimes();
					    	if (app.getActiveState())
					    		writer4.append(String.valueOf(1));
					    	else 
					    		writer4.append(String.valueOf(0));
					    	writer4.append(',');
					    		
					    	//check if the step time is within  original schedule time interval
							if (((NetworkUtils.stepTime >= useTimes[NetworkUtils.stepDay - 1][0])
									&& (NetworkUtils.stepTime < (useTimes[NetworkUtils.stepDay - 1][0]
											+ boiler.getMeanCycleLength()/15.0))) 
									|| ((NetworkUtils.stepTime >= useTimes[NetworkUtils.stepDay - 1][1])
									&& (NetworkUtils.stepTime < (useTimes[NetworkUtils.stepDay - 1][1]
											+ boiler.getMeanCycleLength()/15.0)))) {
								//System.out.println("StepTime"+NetworkUtils.stepTime);
								//System.out.println("USETIME0"+useTimes[NetworkUtils.stepDay - 1][0]);
								//System.out.println("USETIME00"+(useTimes[NetworkUtils.stepDay - 1][0]+boiler.getMeanCycleLength()/15.0));
								//System.out.println("USETIME1"+(useTimes[NetworkUtils.stepDay - 1][1]+boiler.getMeanCycleLength()/15.0));
								writer4.append(String.valueOf(1));
							}
							else  
								writer4.append(String.valueOf(0));
							
							writer4.append(',');
							
					    }
						
					}

					writer2.append(dateFormat.format(cal.getTime()));
					writer2.append(',');
					writer2.append(String.valueOf(NetworkUtils.stepNumber));
					writer2.append(',');
					writer2.append(f.getId());
					writer2.append(',');
					writer2.append(app.getType());
					writer2.append(',');

					writer2.append(String.valueOf(app.getDemand()[NetworkUtils.stepNumber]));
					writer2.append(',');
					writer2.append('\n');
				}

				writer1.append(String.valueOf(f.getEnergyDemand()[NetworkUtils.stepNumber]));
				writer1.append(',');
				writer3.append(String.valueOf(f.getEnergyDemand()[NetworkUtils.stepNumber]));
				writer3.append(',');
				writer3.append(String.valueOf(shiftappnum));
				writer3.append(',');
				writer3.append(String.valueOf(stateonappnum));
				writer3.append(',');
				// writer3.append(String.valueOf(f.getProbDecrease()) );
				// writer3.append(',');
				// writer3.append(String.valueOf(f.getProbIncrease()) );
				// writer3.append(',');
				writer3.append(f.getAction());
				writer3.append(',');
				writer3.append('\n');
				writer4.append('\n');
				
				

			}
			writer2.flush();
			writer2.close();
			writer1.append('\n');
			writer1.flush();
			writer1.close();
			writer3.flush();
			writer3.close();
			writer4.flush();
			writer4.close();


		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void writeNetworkData(ArrayList<Flat> flats, String sc) {
		FileWriter writer1;
		try {
			writer1 = new FileWriter("ABMFlatsNetwork" + sc + ".csv", true);

			for (Flat f : flats) {
				System.out.println("print2");
				List<String> strApp = f.getAppliances().stream().map(Object::toString).collect(Collectors.toList());

				List<String> strNeighb = f.getSocialInfluences().stream().map(Object::toString)
						.collect(Collectors.toList());
				writer1.append(f.getId());
				writer1.append(',');
				writer1.append(f.getOccupancyType());
				writer1.append(',');
				writer1.append(String.valueOf(f.getOccupantNo()));
				writer1.append(',');
				writer1.append(String.join(" ", strApp));
				writer1.append(',');
				writer1.append(String.join(" ", strNeighb));
				writer1.append(',');
				writer1.append('\n');

			}
			writer1.flush();
			writer1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// select shiftable appliances and shift time schedules according to direction
	// 'increase' or 'decrease'
	// interval could be 0 to 24
	public static void shiftLoad(Flat f, String direction, int interval, int heat_interval, double alpha) {
		// System.out.println("SHIFTLOAD");
		ArrayList<ShiftableAppliance> app = new ArrayList<ShiftableAppliance>();
		ArrayList<Appliance> shifta = f.getAppliances();
		ShiftableAppliance shiftapp;
		// create shiftable appliances list that potentially can be shifted at given
		// stepTime. These are the ones that are already on or
		// the ones that are about to start
		for (Appliance a : shifta) {
			if (a instanceof ShiftableAppliance) {
				shiftapp = (ShiftableAppliance) a;
				if (shiftapp.getActiveState() && direction.equals("decrease"))
					app.add(shiftapp);
				else if (!shiftapp.getActiveState()) {
					// int index = Arrays.binarySearch(shiftapp.getUseDays(), NetworkUtils.stepDay -
					// 1);
					// if (index >= 0) {
					// int time_active_app = shiftapp.getUseTimes()[NetworkUtils.stepDay - 1];

					if (direction.equals("decrease")) {
						if ((shiftapp.findUseTimeExists(NetworkUtils.stepDay - 1, NetworkUtils.stepTime).size() != 0)
								&& ((NetworkUtils.stepNumber + 1) < Constants.DEFAULT_END_AT))
							app.add(shiftapp);
					} else if (direction.equals("increase")) {
						if (shiftapp.findAppTimeExists(NetworkUtils.stepDay - 1, NetworkUtils.stepTime, interval)
								.size() != 0) {
							ScheduleTime sc = shiftapp
									.findAppTimeExists(NetworkUtils.stepDay - 1, NetworkUtils.stepTime, interval)
									.get(0);
							/*
							 * if (sc.getDay() !=(NetworkUtils.stepDay-1) &&
							 * (sc.getTime()>=(NetworkUtils.stepTime+interval-96))) {
							 * System.out.println("DAY " + sc.getDay()); System.out.println("Time " +
							 * sc.getTime()); System.out.println("StepDAY " +
							 * String.valueOf(NetworkUtils.stepDay-1)); System.out.println("StepTime " +
							 * String.valueOf(NetworkUtils.stepTime)); System.exit(0);
							 * 
							 * }
							 */
							app.add(shiftapp);
						}
					}

				}
			}

		}

		// pick a shiftable appliance randomly and reschedule but check if there is no
		// schedule clash.
		if (app.size() != 0) {
			// System.out.println("SHIFTLOAD");
			// System.out.println("APPsize" +app.size());

			if (direction.equals("decrease")) {
				System.out.println("Start Decrease");
				ShiftableAppliance select_a = app.get(0);
				double max_consumption_app = app.get(0).getEnergyHourDemand();
				for (int i = 1; i < app.size(); i++) {
					if (app.get(i).getEnergyHourDemand() > max_consumption_app) {
						max_consumption_app = app.get(i).getEnergyHourDemand();
						select_a = app.get(i);

					}

				}
				if (select_a.getDuration() != 0) {

					boolean decrease = false;
					// check if its interruptible device i.e boiler
					if (select_a.getType().equals("boiler")) {
						int boilershifttime = (int) (Math.random() * (heat_interval - 1)) + 1;
						int[][] useTimes = select_a.getUseTimes();
						int origStartTime = useTimes[NetworkUtils.stepDay - 1][1];
						if ((NetworkUtils.stepTime >= useTimes[NetworkUtils.stepDay - 1][0])
								&& (NetworkUtils.stepTime < (useTimes[NetworkUtils.stepDay - 1][0]
										+ select_a.getMeanCycleLength()/15.0))) {
							origStartTime = useTimes[NetworkUtils.stepDay - 1][0];
						} else if ((NetworkUtils.stepTime >= useTimes[NetworkUtils.stepDay - 1][1])
								&& (NetworkUtils.stepTime < (useTimes[NetworkUtils.stepDay - 1][1]
										+ select_a.getMeanCycleLength()/15.0))) {
							origStartTime = useTimes[NetworkUtils.stepDay - 1][1];
						}
						if ((NetworkUtils.stepTime + boilershifttime + select_a.getDuration()) <= (origStartTime
								+ select_a.getMeanCycleLength()/15.0 + heat_interval))

							decrease = select_a.ShiftScheduleforDecrease(NetworkUtils.stepDay - 1,
									NetworkUtils.stepTime, NetworkUtils.stepDay - 1,
									NetworkUtils.stepTime + boilershifttime);

					} else {
						int shifttime = (int) (Math.random() * (interval - 1)) + 1;
						if ((NetworkUtils.stepTime + shifttime) >= 96 && NetworkUtils.stepDay < 7) {
							// change useDay
							// replace useTime

							decrease = select_a.ShiftScheduleforDecrease(NetworkUtils.stepDay - 1,
									NetworkUtils.stepTime, NetworkUtils.stepDay,
									NetworkUtils.stepTime + shifttime - 96);

							System.out.println("Shifts next day");
						} else if ((NetworkUtils.stepTime + shifttime) < 96)
							if (((NetworkUtils.stepTime + shifttime + select_a.getDuration()) < 96)
									|| (((NetworkUtils.stepTime + shifttime + select_a.getDuration()) >= 96)
											&& NetworkUtils.stepDay < 7)) {

								decrease = select_a.ShiftScheduleforDecrease(NetworkUtils.stepDay - 1,
										NetworkUtils.stepTime, NetworkUtils.stepDay - 1,
										NetworkUtils.stepTime + shifttime);

							}
					}
					if (decrease) {
						System.out.println("DECREASE SUCCESSFUL");
						select_a.setActiveState(false);
						f.setAction("decrease");
					} else {
						f.setAction("none");
						System.out.println("NO DECREASE ACTION");
					}
				}

			} else if (direction.equals("increase")) {
				// System.out.println("INCREASE");
				ShiftableAppliance select_a = null;
				String flatid = f.getId().replaceAll("[^0-9]", "");
				int flatnum = Integer.valueOf(flatid) - 1;
				for (ShiftableAppliance a : app) {
					// System.out.println("INCREASE " +
					// (f.getEnergyDemandTime(NetworkUtils.stepTime)
					// + (a.getEnergyHourDemand() / 4.0)));
					// System.out.println("AVG " + NetworkUtils.maxloadflats[flatnum]);
					// if ((f.getEnergyDemandTime(NetworkUtils.stepTime)
					// + (a.getEnergyHourDemand() / 4.0)) <= NetworkUtils.maxloadflats[flatnum])
					if ((f.getEnergyDemandTime(NetworkUtils.stepTime)
							+ (a.getEnergyHourDemand() / 4.0)) < (alpha * NetworkUtils.maxsumloadflats[flatnum]
									- f.getSumNeighbEnergyDemand(NetworkUtils.stepNumber, false)))
						select_a = a;
				}
				if (select_a != null) {

					//System.out.println("Start Increase");
					boolean increase = false;
					ArrayList<ScheduleTime> arr = select_a.findUseDayExists(NetworkUtils.stepDay - 1);
					ScheduleTime a;
					int time_active_app;
					int day;
					if (arr.size() != 0) {
						a = ShiftableAppliance.findNearestTimeSchedule(arr, NetworkUtils.stepTime);

						time_active_app = a.getTime();
						day = a.getDay();
						if (select_a.getType().equals("boiler")) {
							int[][] useTimes = select_a.getUseTimes();
							int origStart = useTimes[day][0];
							if (NetworkUtils.stepTime >= useTimes[day][0])
								origStart = useTimes[day][1];

							if (NetworkUtils.stepTime >= Math.abs(origStart - heat_interval)) {
								increase = select_a.ShiftScheduleforIncrease(day, time_active_app,
										NetworkUtils.stepDay - 1, NetworkUtils.stepTime);
								System.out.println("BOILER INCREASE");
							}

						} else if (Math.abs(time_active_app - NetworkUtils.stepTime) < interval
								&& time_active_app > NetworkUtils.stepTime) {
							increase = select_a.ShiftScheduleforIncrease(day, time_active_app, NetworkUtils.stepDay - 1,
									NetworkUtils.stepTime);

						}
					}
					if (!increase && (NetworkUtils.stepTime + interval) >= 96 && NetworkUtils.stepDay < 7) {
						arr = select_a.findUseDayExists(NetworkUtils.stepDay);
						if (arr.size() != 0) {
							a = ShiftableAppliance.findNearestTimeSchedule(arr, -1);
							time_active_app = a.getTime();
							day = a.getDay();
							if (time_active_app < (interval - (96 - NetworkUtils.stepTime)))
								increase = select_a.ShiftScheduleforIncrease(day, time_active_app,
										NetworkUtils.stepDay - 1, NetworkUtils.stepTime);
						}

					}

					if (increase) {
						f.setAction("increase");
						//System.out.println("INCREASE SUCCESSFUL");
					} else {
						f.setAction("none");
						//System.out.println("NO INCREASE ACTION");
					}

					// int time_active_app =select_a.getUseTimes()[NetworkUtils.stepDay-1];

					/*
					 * if (Math.abs(time_active_app - NetworkUtils.stepTime) < interval &&
					 * time_active_app > NetworkUtils.stepTime ) {
					 * select_a.shiftUseTimes(NetworkUtils.stepDay - 1, NetworkUtils.stepTime); }
					 * else if ((NetworkUtils.stepTime +interval) >=96 && NetworkUtils.stepDay<7) {
					 * time_active_app =select_a.getUseTimes()[NetworkUtils.stepDay]; if
					 * (time_active_app < (interval -(96 -NetworkUtils.stepTime))) {
					 * 
					 * select_a.replaceUseDays(NetworkUtils.stepDay, NetworkUtils.stepDay-1);
					 * select_a.shiftUseTimes(NetworkUtils.stepDay-1, NetworkUtils.stepTime);
					 * 
					 * }
					 * 
					 * }
					 */
				}
			}
		}

		// select_a.addState(NetworkUtils.stepTime+1, endTime);

	}

	public static void writeNonShiftedData(ArrayList<Flat> flats, Date start, int base_load_len, String sc) {
		FileWriter writer1;
		FileWriter writer2;
		FileWriter writer3;
		FileWriter writer4;
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		int day = 0;
		int stepTime = 0;

		avgloadflats = new double[flats.size()];
		maxloadflats = new double[flats.size()];
		maxsumloadflats = new double[flats.size()];

		try {
			File f1 = new File("ABMLoadNonShifted" + sc + ".csv");
			// add heading if new file
			if (f1.createNewFile()) {
				writer1 = new FileWriter(f1, true);
				writer1.append("DateTime");
				writer1.append(',');
				for (Flat f : flats) {
					writer1.append(f.getId());
					writer1.append(',');
				}
				writer1.append('\n');

			} else
				writer1 = new FileWriter(f1, true);
			writer2 = new FileWriter("ABMAverageNonShifted" + sc + ".csv", true);
			writer3 = new FileWriter("ABMMaxNonShifted" + sc + ".csv", true);
			writer4 = new FileWriter("ABMFlatsMaxSumNonShifted" + sc + ".csv", true);
			for (int l = 0; l < base_load_len; l++) {
				System.out.println("Step" + l);
				writer1.append(dateFormat.format(cal.getTime()));
				writer1.append(',');
				// writer1.append(String.valueOf(l));
				// writer1.append(',');

				if (l % (24 * Constants.OBS_PER_HOUR) == 0) {
					day += 1;
					stepTime = 0;
				}
				// System.out.println(day);
				int fnum = 0;
				for (Flat f : flats) {

					// System.out.println("load1" +load);
					executePlannedLoad(f, fnum, stepTime, day, l, writer1);
					fnum++;
					// writer1.append(String.valueOf(f.getEnergyDemandTime(l)));
					// writer1.append(',');

				}
				writer1.append('\n');
				cal.add(Calendar.MINUTE, 15);
				stepTime++;
			}
			avgloadflats = DoubleStream.of(avgloadflats).map(p -> p / Constants.DEFAULT_END_AT).toArray();
			for (int j = 0; j < avgloadflats.length; j++) {
				writer2.append(String.valueOf(avgloadflats[j]));
				writer2.append(',');
				writer3.append(String.valueOf(maxloadflats[j]));
				writer3.append(',');
				writer4.append(String.valueOf(maxsumloadflats[j]));
				writer4.append(',');

			}
			writer2.append('\n');
			writer3.append('\n');
			writer4.append('\n');
			writer1.flush();
			writer1.close();
			writer2.flush();
			writer2.close();
			writer3.flush();
			writer3.close();
			writer4.flush();
			writer4.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void executePlannedLoad(Flat fl) {
		int endTime = 0;
		// if (fl.getActivateTime() <=NetworkUtils.stepTime + 1 ||
		// fl.getDeactivateTime() >NetworkUtils.stepTime + 1) {
		ArrayList<Appliance> shifta = fl.getAppliances();
		for (Appliance app : shifta) {
			if (app instanceof ShiftableAppliance) {
				ShiftableAppliance shiftapp = (ShiftableAppliance) app;
				if (shiftapp.getActiveState()) {
					State currentState = shiftapp.getStates().get(shiftapp.getStates().size() - 1);
					// System.out.println("STARTTIME: " +fl.getId() +" "+
					// currentState.getStartTime());
					// System.out.println("ENDTIME: " + currentState.getEndTime());
					if (shiftapp.getDuration() != 0 && ((NetworkUtils.stepDay - 1) <= currentState.getEndDay())) {
						fl.addEnergyDemand(NetworkUtils.stepNumber, shiftapp.getEnergyHourDemand() / 4.0);
						shiftapp.addEnergyDemand(NetworkUtils.stepNumber, shiftapp.getEnergyHourDemand() / 4.0);
						shiftapp.setDuration(shiftapp.getDuration() - 1);
						// System.out.println("ADD");
						// currentState.getEndTime()>NetworkUtils.stepTime

						continue;
					} else {
						// System.out.println("FALSE");
						shiftapp.setActiveState(false);
						shiftapp.setDuration((int) Math.round(shiftapp.getMeanCycleLength() / 15.0));
						// continue;
					}
				}

				// int index = Arrays.binarySearch(shiftapp.getUseDays(),
				// NetworkUtils.stepDay-1);
				// if (index>=0){
				// int time_active_app =shiftapp.getUseTimes()[NetworkUtils.stepDay-1];
				// System.out.println("match " + (NetworkUtils.stepDay-1) +" time " +
				// time_active_app);
				// System.out.println("stepTime "+ NetworkUtils.stepTime );
				// if (time_active_app==NetworkUtils.stepTime) {
				if (shiftapp.findUseTimeExists(NetworkUtils.stepDay - 1, NetworkUtils.stepTime).size() != 0) {

					shiftapp.setActiveState(true);
					endTime = NetworkUtils.stepTime + shiftapp.getDuration();

					if (endTime >= 96)
						shiftapp.addState(NetworkUtils.stepTime, NetworkUtils.stepDay - 1, endTime - 96,
								NetworkUtils.stepDay);
					else
						shiftapp.addState(NetworkUtils.stepTime, NetworkUtils.stepDay - 1, endTime,
								NetworkUtils.stepDay - 1);
					fl.addEnergyDemand(NetworkUtils.stepNumber, shiftapp.getEnergyHourDemand() / 4.0);
					shiftapp.addEnergyDemand(NetworkUtils.stepNumber, shiftapp.getEnergyHourDemand() / 4.0);
					shiftapp.setDuration(shiftapp.getDuration() - 1);
					// System.out.println("ADD");
					// System.out.println("WORKS");
				}
			}

		}
		// }
		// }
	}

	public static void executePlannedLoad(Flat fl, int fnum, int stepTime, int stepDay, int stepNumber,
			FileWriter writer1) {
		int endTime = 0;
		double load = fl.getEnergyDemandTime(stepNumber);
		double sumneighbflatsload = 0;
		// if (fl.getActivateTime() <=NetworkUtils.stepTime + 1 ||
		// fl.getDeactivateTime() >NetworkUtils.stepTime + 1) {
		try {
			ArrayList<Appliance> shifta = fl.getAppliances();
			for (Appliance app : shifta) {

				if (app instanceof ShiftableAppliance) {
					ShiftableAppliance shiftapp = (ShiftableAppliance) app;
					if (shiftapp.getActiveState()) {
						State currentState = shiftapp.getStates().get(shiftapp.getStates().size() - 1);

						if (shiftapp.getDuration() != 0 && ((stepDay - 1) <= currentState.getEndDay())) {
							load += shiftapp.getEnergyHourDemand() / 4.0;
							fl.addEnergyDemandScheduled(stepTime, shiftapp.getEnergyHourDemand() / 4.0);

							shiftapp.setDuration(shiftapp.getDuration() - 1);

							continue;
						} else {
							// System.out.println("FALSE");
							shiftapp.setActiveState(false);
							shiftapp.setDuration((int) Math.round(shiftapp.getMeanCycleLength() / 15.0));

							continue;
						}

					}

					if (shiftapp.findUseTimeExists(stepDay - 1, stepTime).size() != 0) {
						shiftapp.setActiveState(true);
						endTime = stepTime + shiftapp.getDuration();

						if (endTime >= 96)
							shiftapp.addState(stepTime, stepDay - 1, endTime - 96, stepDay);
						else
							shiftapp.addState(stepTime, stepDay - 1, endTime, stepDay - 1);
						fl.addEnergyDemandScheduled(stepTime, shiftapp.getEnergyHourDemand() / 4.0);
						load += shiftapp.getEnergyHourDemand() / 4.0;

						shiftapp.setDuration(shiftapp.getDuration() - 1);
					}

				}

			}
			avgloadflats[fnum] += load;
			if (maxloadflats[fnum] < load)
				maxloadflats[fnum] = load;
			if (stepTime != 0)
				sumneighbflatsload = fl.getSumNeighbScheduledEnergyDemand(stepTime, true);
			if (maxsumloadflats[fnum] < sumneighbflatsload)
				maxsumloadflats[fnum] = sumneighbflatsload;

			writer1.append(String.valueOf(load));
			writer1.append(',');

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setApplianceStatusFalse(ArrayList<Flat> flats) {
		ShiftableAppliance shiftapp;
		for (Flat f : flats) {
			ArrayList<Appliance> applist = f.getAppliances();

			for (int i = 0; i < applist.size(); i++) {
				Appliance app = applist.get(i);
				if (app instanceof ShiftableAppliance) {
					shiftapp = (ShiftableAppliance) app;
					app.setActiveState(false);
					shiftapp.setDuration((int) Math.round(shiftapp.getMeanCycleLength() / 15.0));

				}

			}
		}
	}

	/*
	 * find the index of max. element in an array
	 */
	public static int getIndexOfLargest(double[] array) {
		if (array == null || array.length == 0)
			return -1; // null or empty

		int largest = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] > array[largest])
				largest = i;
		}
		return largest; // position of the first largest found
	}

}