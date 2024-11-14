package zedipeaksupression.common;
/* this class will show the state of Appliance at time step t
 * 
 */
public class State {
	int startTime;
	int startDay;
	
	int endTime;
	int endDay;
	public State(int startTime, int startDay, int endTime, int endDay) {
		super();
		this.startTime = startTime;
		this.startDay = startDay;
		this.endTime = endTime;
		this.endDay=endDay;
	}
	public int getStartTime() {
		return startTime;
	}
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}
	public int getEndTime() {
		return endTime;
	}
	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}
	public int getStartDay() {
		return startDay;
	}
	public void setStartDay(int startDay) {
		this.startDay = startDay;
	}
	public int getEndDay() {
		return endDay;
	}
	public void setEndDay(int endDay) {
		this.endDay = endDay;
	}
	

}
