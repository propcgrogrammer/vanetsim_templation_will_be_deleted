package vanetsim.map;

import java.awt.Color;
import java.util.Random;

import vanetsim.VanetSimStart;
import vanetsim.debug.Debug;
import vanetsim.scenario.Vehicle;

/**
 * This class represents a traffic light at a junction.
 */
public class TrafficLight {
	
	private static final Random RANDOM = new Random(1L);
	private long ID_; public long getID(){return ID_;}

	/** Default time intervals for state switching. */
	//private static final double[] DEFAULT_SWITCH_INTERVALS = new double[] {5000, 1000, 5000};
	private static final double[] DEFAULT_SWITCH_INTERVALS = new double[] {10000, 500, 10000};
	
	/** A static time to free a junction after a change of phases in ms. */
	private static final double JUNCTION_FREE_TIME = 2000;
	
	/** Duration of the red phase in ms for the priority street. */
	private double redPhaseLength_;
	
	/** Duration of the yellow phase in ms for the priority street. */
	private double yellowPhaseLength_;
	
	/** Duration of the green phase in ms for the priority street. */
	private double greenPhaseLength_;
	
	/** status of traffic light: 0 green : 1 green-orange : 2 red 3: freephase */
	private int state = 0;
	
	public String getStateString()
	{
		if(state == 0) return "綠燈";
		else if(state == 1) return "黃燈";
		else if(state == 2) return "紅燈";
		return "無管制燈號";
	}
	public Color getStateColorSetting()
	{
		if(state == 0) return Color.green;
		else if(state == 1) return Color.yellow;
		else if(state == 2) return Color.red;
		return Color.gray;
	}
	public Color queryStateColorSetting(int state)
	{
		if(state == 0) return Color.green;
		else if(state == 1) return Color.yellow;
		else if(state == 2) return Color.red;
		return Color.gray;
	}
	public Color queryStateColorSetting(String state)
	{
		if(state.equals("綠燈") || state == "綠燈") return Color.green;
		else if(state.equals("黃燈") || state == "黃燈") return Color.yellow;
		else if(state.equals("紅燈") || state == "紅燈") return Color.red;
		return Color.gray;
	}
	
	public Color[] getStateColor(Color[] color)
	{
		if(state == 0) color = new Color[]{Color.green,Color.white,Color.white};
		else if(state == 1) color = new Color[]{Color.white,Color.yellow,Color.white};
		else if(state == 2) color = new Color[]{Color.white,Color.white,Color.red};
		return color;
	}
	
	/** Traffic Light Collections */
	private Street[] streets_;

	
	/** Stores if a street is a priority street or not; used to distinguish between times. */
	private boolean[] streetIsPriority_;
		
	/** Timer for this traffic light; because all traffic lights on a junction run synchronously just one timer is needed. */
	private double timer_;
	
	/** The <code>Junction</code> this traffic light. */
	private Junction junction_;

	public JunctionQueue getJunctionQueuePriority3(){return junction_.getJunctionQueuePriority3();}
	public JunctionQueue getJunctionQueuePriority4(){return junction_.getJunctionQueuePriority4();}

	public Vehicle[] getJunctionQueueVehicles(JunctionQueue queue){return queue.getVehicles();}

	/** switcher between long and short signal length */
	private boolean switcher = true;
	
	private double positionX = -1;
	private double positionY = -1;

	
	public Street[] getTrafficLightRuleStreet(){return streets_;}
	public double getTrafficLightPositionX(){return this.positionX;}
	public double getTrafficLightPositionY(){return this.positionY;}
	
	/**
	 * Constructor.
	 */
	public TrafficLight(Junction junction) {
		
		Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
		Debug.callFunctionInfo(this.getClass().getName(), "TrafficLight(Junction junction)", Debug.ISLOGGED);
		
		ID_ = RANDOM.nextLong();
		junction_ = junction;
		//set the phases with standard times
		junction_.getNode().setTrafficLight_(this);
		
//		Random ran = new Random();
//		String msg =  "TrafficLight ==> "+ran.nextInt(100000);
//		Debug.detailedInfo(msg, true);
		
		Random ran = new Random();
		ran.setSeed(System.currentTimeMillis());
		DEFAULT_SWITCH_INTERVALS[0] = ran.nextInt(20000);   
		DEFAULT_SWITCH_INTERVALS[1] = 500;
		DEFAULT_SWITCH_INTERVALS[2] = ran.nextInt(20000);
		
		redPhaseLength_ = DEFAULT_SWITCH_INTERVALS[0];
		yellowPhaseLength_ = DEFAULT_SWITCH_INTERVALS[1];
		greenPhaseLength_ = DEFAULT_SWITCH_INTERVALS[2];
		
//		Random rand = new Random();
//		rand.setSeed(System.currentTimeMillis());
//		
//		redPhaseLength_ = rand.nextInt(10)*1000;
//		yellowPhaseLength_ = rand.nextInt(10)*1000;
//		greenPhaseLength_ = rand.nextInt(10)*1000;
		
		initialiseTrafficLight();
	}
	
	public String getStreetString()
	{
		StringBuilder sb = new StringBuilder();
		
		for(Street s : this.streets_)
		{
			sb.append("\r\n位於 ");
			sb.append(s.getName());
			sb.append("\r\n");
		}
		return sb.toString();
	}
	
	/**
	 * Constructor.
	 */
	public TrafficLight(double redPhaseLength, double yellowPhaseLength, double greenPhaseLength, Junction junction) {		
		
		Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
		Debug.callFunctionInfo(this.getClass().getName(), "TrafficLight(double redPhaseLength, double yellowPhaseLength, double greenPhaseLength, Junction junction)", Debug.ISLOGGED);
		
		junction_ = junction;
		ID_ = RANDOM.nextLong();
		
		junction_.getNode().setTrafficLight_(this);

		//set the phases from the given times
		redPhaseLength_ = redPhaseLength;
		yellowPhaseLength_ = yellowPhaseLength;
		greenPhaseLength_ = greenPhaseLength;
		
		initialiseTrafficLight();
	}
	
	/**
	 * This functions initializes the time arrays for each phase of the traffic light for each street;
	 * starts of with the priority streets at green and the crossing streets at red
	 * 
	 */
	private void initialiseTrafficLight(){			

		Debug.callFunctionInfo(this.getClass().getName(), "initialiseTrafficLight()", Debug.ISLOGGED);
		
		streetIsPriority_ = new boolean[junction_.getNode().getCrossingStreetsCount()];
		
		if(!junction_.getNode().hasNonDefaultSettings()){
			junction_.getNode().setStreetHasException_(new int[junction_.getNode().getCrossingStreetsCount()]);
			for(int m = 0; m < junction_.getNode().getStreetHasException_().length; m++) junction_.getNode().getStreetHasException_()[m] = 1;
		}

			
		streets_ = junction_.getNode().getCrossingStreets();
		Street[] tmpPriorityStreets = junction_.getPriorityStreets();
		
		//boolean priorityStreet = false;
		boolean isOneway = false;
		
		for(int i = 0; i < streets_.length; i++){
			streetIsPriority_[i] = false;
			for(int j = 0; j < tmpPriorityStreets.length; j++){
				if(streets_[i] == tmpPriorityStreets[j]) streetIsPriority_[i] = true;
			}
			if(streets_[i].isOneway() && streets_[i].getStartNode() == junction_.getNode()) isOneway = true;

			if(!isOneway){
				if(streetIsPriority_[i]){
					if(junction_.getNode() == streets_[i].getStartNode()){
						streets_[i].setStartNodeTrafficLightState(0);
						if(junction_.getNode().hasNonDefaultSettings()){
							streets_[i].setStartNodeTrafficLightState(junction_.getNode().getStreetHasException_()[i]);
						}
						streets_[i].setPriorityOnStartNode(true);
					}
					else{
						streets_[i].setEndNodeTrafficLightState(0);	
						if(junction_.getNode().hasNonDefaultSettings()){
							streets_[i].setEndNodeTrafficLightState(junction_.getNode().getStreetHasException_()[i]);
						}
						streets_[i].setPriorityOnEndNode(true);
					}
				}
				else{
					if(junction_.getNode() == streets_[i].getStartNode()){
						streets_[i].setStartNodeTrafficLightState(4);
						if(junction_.getNode().hasNonDefaultSettings()){
							streets_[i].setStartNodeTrafficLightState(junction_.getNode().getStreetHasException_()[i]);
						}
					}
					else {
						streets_[i].setEndNodeTrafficLightState(4);
						if(junction_.getNode().hasNonDefaultSettings()){
							streets_[i].setEndNodeTrafficLightState(junction_.getNode().getStreetHasException_()[i]);
						}
					}
				}
			}

			isOneway = false;
			
			//lets calculate the drawing positions of the traffic light ... now its better for the performance
			calculateTrafficLightPosition(streets_[i]);
		}		
		timer_ = greenPhaseLength_;
		
		//tell the node, that he now has a traffic light
		junction_.getNode().setHasTrafficSignal_(true);
	
	}
	
	/**
	 * This function should change the states of the traffic lights if necessary. Should be called after the first greenphase
	 */
	public void changePhases(int timePerStep){
		//if remaining time is smaller than the timerPerStep we have to change the states
		if(timer_ < timePerStep){
			state = (state +1) % 4;		

			//could be less code, but this way I get a better performanz
			//(non)priorties where green: Change to orange
			if(state == 1) timer_ = yellowPhaseLength_;
			//(non)priorties where green-orange: Change to red for a freephase
			else if(state == 2)timer_ = JUNCTION_FREE_TIME;
			//priorties where free: Change to red
			else if(state == 0 && switcher)timer_ = greenPhaseLength_;
			//non-priorties where free: Change to red
			else if(state == 0 && !switcher)timer_ = redPhaseLength_;
			//yellow
			else if(state == 3)timer_ = yellowPhaseLength_;	
			
			
			switcher = !switcher;
			//update all street + 1

			for(int i = 0; i < streets_.length; i++){
				if(streets_[i].getStartNode() == junction_.getNode() && streets_[i].getStartNodeTrafficLightState() != -1){
					streets_[i].updateStartNodeTrafficLightState();
				}
				else if(streets_[i].getEndNode() == junction_.getNode() && streets_[i].getEndNodeTrafficLightState() != -1){
					streets_[i].updateEndNodeTrafficLightState();
				}
			}
		}
		//else change timer
		else timer_ = timer_ - timePerStep;
	}
	
	
	
	/* Calculates Traffic light position */
	/**
	 * Calculates Traffic light position for drawing
	 */
	public void calculateTrafficLightPosition(Street tmpStreet){	
		double junctionX = junction_.getNode().getX();
		double junctionY = junction_.getNode().getY();
		
		double nodeX;
		double nodeY;
		
		if(junction_.getNode().equals(tmpStreet.getStartNode())){
			nodeX = tmpStreet.getEndNode().getX();
			nodeY = tmpStreet.getEndNode().getY();
		}
		else{
			nodeX = tmpStreet.getStartNode().getX();
			nodeY = tmpStreet.getStartNode().getY();		
		}
		
		this.positionX = nodeX;
		this.positionY = nodeY;
		
		String s = "nodeX --> "+ positionX + "nodeY --> "+ positionY;
		
		Debug.detailedInfo(s, Debug.ISLOGGED);
		
		//calculate the linear function (y = mx + n) between junction node an the other node
		double m = (nodeY-junctionY)/(nodeX-junctionX);
		double n = nodeY - m*nodeX;
		
		double a = 1 + (m * m);
		double b = (2 * m * n) - (2 * m * junctionY) - (2 * junctionX);
		double c = (n * n) - (2 * n * junctionY) + (junctionY * junctionY) - (700 * 700) + (junctionX * junctionX);

		if(junction_.getNode().equals(tmpStreet.getStartNode())){
			if(nodeX < junctionX){
				tmpStreet.setTrafficLightStartX_((int)Math.round((-b - Math.sqrt((b*b) - (4*a*c))) / (2*a))); 
				tmpStreet.setTrafficLightStartY_((int)Math.round((m * tmpStreet.getTrafficLightStartX_()) + n));
			 }
			 else{
				tmpStreet.setTrafficLightStartX_((int)Math.round((-b + Math.sqrt((b*b) - (4*a*c))) / (2*a)));
				tmpStreet.setTrafficLightStartY_((int)Math.round((m * tmpStreet.getTrafficLightStartX_()) + n));
			 }
		}
		else{
			if(nodeX < junctionX){
				tmpStreet.setTrafficLightEndX_((int)Math.round((-b - Math.sqrt((b*b) - (4*a*c))) / (2*a))); 
				tmpStreet.setTrafficLightEndY_((int)Math.round((m * tmpStreet.getTrafficLightEndX_()) + n));
			 }
			 else{
				tmpStreet.setTrafficLightEndX_((int)Math.round((-b + Math.sqrt((b*b) - (4*a*c))) / (2*a)));
				tmpStreet.setTrafficLightEndY_((int)Math.round((m * tmpStreet.getTrafficLightEndX_()) + n));
			 }	
		}

	}


	/**
	 * Gets the length of the green phase.
	 * 
	 * @return the length of the green phase
	 */
	public double getGreenPhaseLength() {
		return greenPhaseLength_;
	}

	
	/**
	 * Sets the length for the green Phase.
	 * 
	 * @param greenPhaseLength the length for the green phase
	 */
	public void setGreenPhaseLength(double greenPhaseLength) {
		this.greenPhaseLength_ = greenPhaseLength;
	}
	
	/**
	 * Sets the length of the yellow phase.
	 * 
	 * @param yellowPhaseLength the length of the yellow phase
	 */
	public void setYellowPhaseLength(double yellowPhaseLength) {
		this.yellowPhaseLength_ = yellowPhaseLength;
	}

	/**
	 * Gets the length of the yellow phase.
	 * 
	 * @return the length of the yellow phase
	 */
	public double getYellowPhaseLength() {
		return yellowPhaseLength_;
	}

	/**
	 * Sets the length of the red phase.
	 * 
	 * @param redPhaseLength the length of the red phase
	 */
	public void setRedPhaseLength(double redPhaseLength) {
		this.redPhaseLength_ = redPhaseLength;
	}

	/**
	 * Gets the length of the red phase.
	 * 
	 * @return the length of the red phase
	 */
	public double getRedPhaseLength() {
		return redPhaseLength_;
	}


	/**
	 * @param state the state to set
	 */
	public void setState(int state) {
		this.state = state;
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * @return the streets_
	 */
	public Street[] getStreets_() {
		return streets_;
	}

	/**
	 * @param streets_ the streets_ to set
	 */
	public void setStreets_(Street[] streets_) {
		this.streets_ = streets_;
	}




}