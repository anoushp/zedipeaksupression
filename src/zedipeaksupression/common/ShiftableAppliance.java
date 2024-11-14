package zedipeaksupression.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;;

public class ShiftableAppliance extends Appliance  {
	private double meanCycleLength;
	private int duration;
	private int[] useDays;
	private int[][] useTimes;
	private ArrayList <State> states;
	private ArrayList<ScheduleTime> scheduleTimes;

	public ShiftableAppliance(String type, double meanCycleLength, double energyHourDemand) {
		super(type,energyHourDemand);
		this.meanCycleLength=meanCycleLength;	
		this.setDuration((int) Math.round(meanCycleLength/15.0));
		this.states=new ArrayList<State>();
		
	}

		
	public double getMeanCycleLength() {
		return meanCycleLength;
	}

	public void setMeanCycleLength(double meanCycleLength) {
		this.meanCycleLength = meanCycleLength;
	}

	
	public int[] getUseDays() {
		return useDays;
	}

	public void setUseDays(int[] useDays) {
		this.useDays = useDays;
	}

	public int[][]getUseTimes() {
		return useTimes;
	}

	public void setUseTimes(int[][] useTimes) {
		this.useTimes = useTimes;
	}
	public void shiftUseTimes (int day, int t) {
		if (this.useTimes[day].length ==1)
		   this.useTimes[day][0] = t;
		
		
	}

	
	public String toString() { 
	    return this.getType();
	}

	public boolean isActiveState() {
		return this.getActiveState();
	}

	
	public void addState(int startTime, int startDay, int endTime, int endDay) {
		State s=new State(startTime, startDay, endTime, endDay);
		states.add(s);
	}
	public ArrayList <State> getStates() {
		return states;
	}

	public void setStates(ArrayList <State> states) {
		this.states = states;
	} 
	public void replaceUseDays(int value1, int value2) {
		for (int i=0; i<this.useDays.length; i++) {
			if (this.useDays[i]==value1) {
				this.useDays[i]=value2;
				return;
			}
		}
	}


	public int getDuration() {
		return duration;
	}


	public void setDuration(int duration) {
		this.duration = duration;
	}


	public ArrayList<ScheduleTime> getScheduleTimes() {
		return scheduleTimes;
	}


	public void setScheduleTimes(ArrayList<ScheduleTime> scheduleTimes) {
		this.scheduleTimes = scheduleTimes;
	}

    public ArrayList<ScheduleTime> findUseTimeExists(int day, int time) {
    	Predicate<ScheduleTime> equalDay =   e -> e.getDay() ==day;
    	Predicate<ScheduleTime> equalTime =   e -> e.getTime() ==time;
    	ArrayList<ScheduleTime> schedules = this.scheduleTimes.stream()
    			.filter(equalDay.and(equalTime)).collect(Collectors.toCollection(ArrayList::new));
    	return schedules;
    	
    }
    public ArrayList<ScheduleTime> findUseDayExists(int day) {
    	Predicate<ScheduleTime> equalDay =   e -> e.getDay() ==day;
    	
    	ArrayList<ScheduleTime> schedules = this.scheduleTimes.stream()
    			.filter(equalDay).collect(Collectors.toCollection(ArrayList::new));
    	return schedules;
    	
    }
   /*
    * the modification of schedule will only happen if there are no clashes.
    */
	public boolean ShiftScheduleforDecrease(int day, int time, int day1, int time1) {
		ArrayList<ScheduleTime> sc ;
		if (day != day1) {
			sc = this.findUseDayExists(day);
			for (ScheduleTime s : sc) {
				if (s.getTime() > time) {
					System.out.println("Time1: " + time1);
					System.out.println("TIMES " + s.getTime());
					System.out.println("TIME " + time);
					System.out.println("DUration " + duration);
					System.out.println("SIZE" + sc.size());
					System.out.println("NODECREASE");
					return false;
				}
			}
			sc = this.findUseDayExists(day1);
			for (ScheduleTime s : sc) {
				if ((time1 + this.duration > s.getTime())) {
					System.out.println("Time1: " + time1);
					System.out.println("TIMES " + s.getTime());
					System.out.println("TIME " + time);
					System.out.println("DUration " + duration);
					System.out.println("SIZE" + sc.size());
					System.out.println("NODECREASE");
					return false;
				}
			}

		} else {
			sc = this.findUseDayExists(day1);
			for (ScheduleTime s : sc) {
				if ((time1 + this.duration > s.getTime()) && s.getTime() > time) {
					System.out.println("Time1: " + time1);
					System.out.println("TIMES " + s.getTime());
					System.out.println("TIME " + time);
					System.out.println("DUration " + duration);
					System.out.println("SIZE" + sc.size());
					System.out.println("NODECREASE");
					return false;
				}
			}
		}
		for (ScheduleTime s : this.scheduleTimes) {
			
			int t =0;
			int d=0;
    		if (this.getActiveState())
    		{
    			State currentState = (this.states.get(this.states.size()-1));
    			d=currentState.startDay;
    			t=currentState.startTime;
    			
    		}
    		else {
    			t=time;
    			d=day;
    		}
			if ((s.getDay() == d) && (s.getTime() == t)) {
				
				s.setDay(day1);
				s.setTime(time1);
				return true;

			}

		}
		return false;
	}
    public boolean ShiftScheduleforIncrease(int day, int time, int day1, int time1) {
		ArrayList<ScheduleTime> sc;
		if (day != day1) {
			sc = this.findUseDayExists(day);

			for (ScheduleTime s : sc) {
				if (s.getTime() < time) {
					System.out.println("Time1: " + time1);
					System.out.println("TIMES " + s.getTime());
					System.out.println("TIME " + time);
					System.out.println("DUration " + duration);
					System.out.println("SIZE" + sc.size());
					System.out.println("NOINCREASE");
					return false;
				}
			}
			sc = this.findUseDayExists(day1);
			for (ScheduleTime s : sc) {
				if (s.getTime() > time1) {
					System.out.println("Time1: " + time1);
					System.out.println("TIMES " + s.getTime());
					System.out.println("TIME " + time);
					System.out.println("DUration " + duration);
					System.out.println("SIZE" + sc.size());
					System.out.println("NODECREASE");
					return false;
				}
			}
		} else {
			sc = this.findUseDayExists(day);
			for (ScheduleTime s : sc) {
				if (s.getTime() > time1 && s.getTime() < time) {
					System.out.println("Time1: " + time1);
					System.out.println("TIMES " + s.getTime());
					System.out.println("TIME " + time);
					System.out.println("DUration " + duration);
					System.out.println("SIZE" + sc.size());
					System.out.println("NODECREASE");
					return false;
				}
			}

		}
    	for (ScheduleTime s : this.scheduleTimes) {
    		if ((s.getDay()==day) && (s.getTime()==time)) {
    			s.setDay(day1);
    			s.setTime(time1);
    			return true;
    		
    		}
    	
    	}
    	return false;
    }
    
    public ArrayList<ScheduleTime> findAppTimeExists(int day, int time, int interval) {
    	
        Predicate<ScheduleTime> sameday =   e -> e.getDay() ==day;
        Predicate<ScheduleTime> latertime =e -> e.getTime() > time;
        Predicate<ScheduleTime> inInterval =e -> e.getTime() <= (time +interval);
    	ArrayList<ScheduleTime> schedules = this.scheduleTimes.stream()
    			.filter(sameday.and(latertime).and(inInterval)).collect(Collectors.toCollection(ArrayList::new));
    	if (schedules.size()!=0)
    		  return schedules;
    	else {
    		Predicate<ScheduleTime> nextday =   e -> e.getDay() == (day+1);
    		Predicate<ScheduleTime> timeconstraint =   e -> e.getTime() <=(time +interval-96);
    		schedules = this.scheduleTimes.stream()
        			.filter(nextday.and(timeconstraint)).collect(Collectors.toCollection(ArrayList::new));
    	}
    		
    	
    	return schedules;
        	
    }
    public static ScheduleTime findNearestTimeSchedule(ArrayList<ScheduleTime> ar,int stepTime) {
		ScheduleTime min;
		if (ar.size() == 1)
			return ar.get(0);
		else {
			min = ar.get(0);
			for (int i = 1; i < ar.size(); i++)
				if (ar.get(i).getDay() < min.getDay())
					min = ar.get(i);
				else if  ((ar.get(i).getTime() > stepTime) && (ar.get(i).getTime() < min.getTime()))
					min = ar.get(i);

		}
		return min;

    }

}
