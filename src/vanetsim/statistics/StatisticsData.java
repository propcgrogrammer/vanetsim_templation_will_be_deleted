package vanetsim.statistics;

public class StatisticsData {
	
	private String time = "";
	private int curSpeed = 0;
	
	private double travelTime = 0;
	private double waitingTime = 0;
	private double blockingTime = 0;
	
	public void setTime(String time){this.time = time;}
	public void setSpeed(int speed){this.curSpeed = speed;}
	public void setTravelTime(double travel){this.travelTime = travel;}
	public void setWaitingTime(double wait){this.waitingTime = wait;}
	public void setBlockingTime(double block){this.blockingTime = block;}
	
	
	public String getTime(){return this.time;}
	public int getSpeed(){return this.curSpeed;}
	public double getTravelTime(){return this.travelTime;}
	public double getWaitingTime(){return this.waitingTime;}
	public double getBlockingTime(){return this.blockingTime;}

}
