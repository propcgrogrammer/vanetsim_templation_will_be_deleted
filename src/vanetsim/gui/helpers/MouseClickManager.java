package vanetsim.gui.helpers;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.ScrollPane;
import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import vanetsim.map.*;
import vanetsim.map.Map;
import vanetsim.statistics.PieChart;
import vanetsim.statistics.Statistics;
import vanetsim.statistics.StatisticsData;
import vanetsim.VanetSimStart;
import vanetsim.debug.Debug;
import vanetsim.gui.DrawingArea;
import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.EditControlPanel;
import vanetsim.gui.controlpanels.ReportingControlPanel;
import vanetsim.gui.controlpanels.SignalControlLabel;
import vanetsim.gui.controlpanels.SignalControlPanel;
import vanetsim.gui.controlpanels.SimulateControlPanel;
import vanetsim.localization.Messages;
import vanetsim.routing.WayPoint;
import vanetsim.scenario.Vehicle;


/**
 * A class to correctly handle mouseclicks and drags on the DrawingArea. Furthermore, this class also handles the display in the
 * information text area.
 */
public class MouseClickManager extends Thread{

	/** The only instance of this class (singleton). */
	private static final MouseClickManager INSTANCE = new MouseClickManager();
	
	private static final int DESTINATION_INTERVALS = 1000;
	
	/** How often the information panel is refreshed in milliseconds (only achieved approximately!). */
	private static final int INFORMATION_REFRESH_INTERVAL = 800;
	
	/** After which time dragging shall be activated (in milliseconds) */
	private static final int DRAG_ACTIVATION_INTERVAL = 140;
	
	/** A formatter for integers with fractions */
	private static final DecimalFormat INTEGER_FORMAT_FRACTION = new DecimalFormat(",##0.00"); //$NON-NLS-1$
	
	/** A reference to the edit control panel. */
	private final EditControlPanel editPanel_ = VanetSimStart.getMainControlPanel().getEditPanel();
	
	/** A reference to the reporting control panel. */
	private final ReportingControlPanel reportPanel_ = VanetSimStart.getMainControlPanel().getReportingPanel();

	/** A StringBuilder for the information text. Reused to prevent creating lots of garbage */
	private final StringBuilder informationText_ = new StringBuilder();
	private final StringBuilder informationTextCht_ = new StringBuilder();
	
	/** The default mouse cursor. */
	private Cursor defaultCursor_ = new Cursor(Cursor.DEFAULT_CURSOR);
	
	/** The move mouse cursor. */
	private Cursor moveCursor_ = new Cursor(Cursor.MOVE_CURSOR);

	/** The DrawingArea (needed to change mouse cursor). */
	private DrawingArea drawArea_ = null;

	/** <code>true</code> if this manager currently is active,<code>false</code> if it's inactive. */
	public boolean active_ = false;
	
	/** The time when mouse button was last pressed. */
	private long pressTime_ = 0;
	
	/** The x coordinate where mouse was pressed. */
	private int pressedX_ = -1;
	
	/** The y coordinate where mouse was pressed. */
	private int pressedY_ = -1;

	/** The time when mouse button was last released. */
	private long releaseTime_ = 0;

	/** The last x coordinate where mouse was released. */
	private int releasedX_ = -1;
	
	/** The last y coordinate where mouse was released. */
	private int releasedY_ = -1;	

	/** The time already waited to change from default cursor. If set to <code>-1</code> changing between the cursors is disabled. */
	private int waitingTime_ = -1;
	
	/** The marked node. */
	private Node markedNode_ = null;
	
	/** The marked street. */
	private Street markedStreet_ = null;
	
	/** The marked vehicle. */
	private Vehicle markedVehicle_ = null;
	
	/** The information about a street is cached here as it doesn't change that often. */
	private String cachedStreetInformation_ = ""; //$NON-NLS-1$
	
	/** Which street is currently cached */
	private Street cachedStreet_ = null;
	
	private String FILENAME = "";
	private String FILENAME_TL = "";
	
	private TrafficLight example = null;
	
	private static boolean isTrafficLightReady = false; public static boolean isTrafficLightReady(){return isTrafficLightReady;}
//	private static HashMap<String, TrafficLight> map = new HashMap<String,TrafficLight>();
	private static HashMap<String, TrafficLight> trafficLightMap = null;
	public static HashMap<String, TrafficLight> getHashMap(){return trafficLightMap;}
	
	
	
	private static int trafficLightsCounts = 0;

	private boolean isPanelReady = false;
	
	private static boolean isVehicleArrived_ = false;
	
	private static String report = "";
	
	private static java.util.Date startDate = null;
	private static java.util.Date endDate = null;
	
	private static String startRunTime = "";
	private static String endRunTime = "";
	
	private static HashMap<String, Integer> resultMap = new HashMap<String, Integer>();
	private ArrayList<StatisticsData> stat = new ArrayList<StatisticsData>();
	
	
	/**
	 * Gets the single instance of this manager.
	 * 
	 * @return single instance of this manager
	 */
	public static MouseClickManager getInstance(){
		return INSTANCE;
	}

	/**
	 * Empty, private constructor in order to disable instancing.
	 */
	private MouseClickManager(){
		Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
		Debug.callFunctionInfo(this.getClass().getName(), "MouseClickManager()", Debug.ISLOGGED);
	}

	/**
	 * Sets the value for the <code>isActive</code> variable.
	 * 
	 * @param active	<code>true</code> to signal this thread that the DrawingArea has been entered,<code>false</code> to signal that the area was left
	 */
	public void setActive(boolean active){
		active_ = active;
		if(active_ == false && drawArea_ != null){
			waitingTime_ = -1;
			drawArea_.setCursor(defaultCursor_);	//to be sure that cursor is right if leaving the area
		}
	}

	/**
	 * Sets the {@link vanetsim.gui.DrawingArea} this MouseClickManager is associated with.
	 * 
	 * @param drawArea	the area on which this MouseClickManager operates
	 */
	public void setDrawArea(DrawingArea drawArea){
		drawArea_ = drawArea;
	}

	/**
	 * Signals this manager that the mouse was pressed. If the edit mode is currently active, the click is forwarded to the edit panel.
	 * 
	 * @param x	the x coordinate where mouse was pressed
	 * @param y	the y coordinate where mouse was pressed
	 */
	public synchronized void signalPressed(int x, int y){
		try{
			Point2D.Double mapposition_source = new Point2D.Double(0,0);
			Renderer.getInstance().getTransform().inverseTransform(new Point2D.Double(x,y), mapposition_source);
			boolean onEditingTab;
			if(VanetSimStart.getMainControlPanel().getSelectedTabComponent() instanceof EditControlPanel) onEditingTab = true;
			else onEditingTab = false;
			if(editPanel_.getEditMode() && onEditingTab){	//editing enabled? then forward the transformed coordinates to the editing panel
				editPanel_.receiveMouseEvent((int)Math.round(mapposition_source.getX()),(int)Math.round(mapposition_source.getY()));
			} else if(reportPanel_.isInMonitoredMixZoneEditMode()){
				reportPanel_.receiveMouseEvent((int)Math.round(mapposition_source.getX()),(int)Math.round(mapposition_source.getY()));
			} else {
				waitingTime_ = 0;	//to enable cursor change (cursor changes to indicate dragging)
				pressedX_ = (int)StrictMath.floor(0.5 + mapposition_source.getX());
				pressedY_ = (int)StrictMath.floor(0.5 + mapposition_source.getY());
				pressTime_ = System.currentTimeMillis();
				releaseTime_ = pressTime_;
			}
		} catch (Exception e){}		
	}

	/**
	 * Signals this manager that the mouse was released (used for dragging).
	 * 
	 * @param x	the x coordinate where mouse was released
	 * @param y	the y coordinate where mouse was released
	 */
	public synchronized void signalReleased(int x, int y){
		boolean onEditingTab;
		if(VanetSimStart.getMainControlPanel().getSelectedTabComponent() instanceof EditControlPanel) onEditingTab = true;
		else onEditingTab = false;
		if((!editPanel_.getEditMode() || !onEditingTab) && !reportPanel_.isInMonitoredMixZoneEditMode()){	//dragging only enabled when not editing!
			try{
				Point2D.Double mapposition_source = new Point2D.Double(0,0);
				Renderer.getInstance().getTransform().inverseTransform(new Point2D.Double(x,y), mapposition_source);
				waitingTime_ = -1;
				releasedX_ = (int)StrictMath.floor(0.5 + mapposition_source.getX());
				releasedY_ = (int)StrictMath.floor(0.5 + mapposition_source.getY());
				releaseTime_ = System.currentTimeMillis();
				if(drawArea_ != null) drawArea_.setCursor(defaultCursor_);
			} catch (Exception e){}	
		}
	}
	
	/**
	 * Cleans markings so that objects can be deleted through garbage collector.
	 */
	public void cleanMarkings(){
		markedVehicle_ = null;
		markedStreet_ = null;
		markedNode_ = null;
	}
	
	private String createTrafficLightInfo()
	{
		StringBuilder sb = new StringBuilder();
		
		int index = 0;
		
		Map map = Map.getInstance();
		
		ArrayList<TrafficLight> trafficLightArrayList = map.getInstance().getTrafficLightArray();
		
		for(TrafficLight t : trafficLightArrayList)
		{
			if(t != null)
			{
				ArrayList<String> check = new ArrayList<String>();
				
				sb.append("序號[");
				sb.append(String.valueOf(++index));
				sb.append("]");
				sb.append(" 燈號識別號：");
				sb.append(String.valueOf(t.getID()));
				sb.append("\r\n");
				sb.append("管轄路段：");
				
				Street[] streets = t.getStreets_();
				Junction junction = t.getJunction_();
				sb.append("端點 <----> ");
				for(Street s : streets )
				{
					String name = s.getName().toString();
					if(!check.contains(name))
					{
						sb.append(s.getName().toString());
						sb.append(" [ ");
						sb.append(s.getQueue_().size());
						sb.append(" ] ");
						sb.append(" <----> ");
						check.add(name);
					}
				}

				sb.append("端點 \r\n");
				
			    
				
				sb.append("目前燈號：");
				sb.append(t.getStateString());
				sb.append("\r\n");

//				Vehicle[] queue3 = t.getJunctionQueueVehicles(t.getJunctionQueuePriority3());
//				Vehicle[] queue4 = t.getJunctionQueueVehicles(t.getJunctionQueuePriority4());
//				sb.append("Ｑ3等待車輛數：");
//				sb.append(queue3.length);
//				sb.append("\r\n");
//				sb.append("Ｑ4等待車輛數：");
//				sb.append(queue4.length);
//				sb.append("\r\n");

				sb.append("----\r\n");
			}
		}
		
		return sb.toString();
	}
	
	/*
	private String writeInformationString()
	{
		StringBuilder infobuilder = new StringBuilder();
		String info = "";
		
	//	VanetSimStart.getTextArea().setText("");
		
		try {
		
		 if(FILENAME == "" || FILENAME.equals("")) FILENAME = "VehiclesInfo_" + new java.util.Date().getTime() + ".txt";
		 
		 info = "";
		 FileWriter fw;
		 
		 fw = new FileWriter(FILENAME,Debug.ISLOGGED);
		 
		 
			 
			 Map map = Map.getInstance();
			 Region[][] regions = map.getRegions();
			 
			 Node[] nodes ;
			 Vehicle[] vehicles ;
			 Street[] streets ;
			 
			 ArrayList<TrafficLight> trafficLightArrayList = map.getInstance().getTrafficLightArray();
			 
			 
			 TrafficLight trafficlight = null;
		
			 
			
			 
	//		 infobuilder.append("######################################\r\n");
	//		 infobuilder.append("#  Console Log \r\n");
	//		 infobuilder.append("######################################\r\n");
			 
			 ArrayList<Street> st = map.getAllMapStreets();
			 /*
			 for(Street s : st)
			 {
				 infobuilder.append("+ 道路名稱： ");
				 infobuilder.append(s.getName());
				 infobuilder.append("\r\n");
				 infobuilder.append("+ 道路型態： ");
				 infobuilder.append(s.getStreetType_());
				 infobuilder.append("\r\n");
				 infobuilder.append("+ 道路起點座標： ");
				 infobuilder.append(s.getStartNode().getX());
				 infobuilder.append(" (x) , ");
				 infobuilder.append(s.getStartNode().getY());
				 infobuilder.append(" (y)\r\n");
				 infobuilder.append("+ 道路終點座標： ");
				 infobuilder.append(s.getEndNode().getX());
				 infobuilder.append(" (x) , ");
				 infobuilder.append(s.getEndNode().getY());
				 infobuilder.append(" (y)\r\n");
				 infobuilder.append("\r\n----------------------\r\n");
				 
				 if(s.getStartNode().isHasTrafficSignal_())
				 {
					 infobuilder.append("+ 紅綠燈座標： ");
					 infobuilder.append(s.getTrafficLightStartX_());
					 infobuilder.append(" (x), ");
					 infobuilder.append(s.getTrafficLightStartY_());
					 infobuilder.append(" (y)\r\n");
	//				 infobuilder.append("+ 現在燈號： ");
	//				 infobuilder.append(s.getStartNode().getTrafficLight_().getStateString());
				 }
				 else
				 {
					 infobuilder.append("該地段無號誌燈號");
				 }
				 infobuilder.append(" \r\n**********************\r\n");
				 
			 }
			 */
			 
			 /*
			 
			 for(int i=0;i<regions.length;i++){
				 for(int j=0;j<regions[i].length;j++){
					 
					 trafficlight = regions[i][j].getTrafficLight();
					 nodes = regions[i][j].getNodes();
					 vehicles = regions[i][j].getVehicleArray();
					 streets = regions[i][j].getStreets();
			
					 
					 for(int k=0;k<nodes.length;k++)
					 {
							if(nodes[k].getJunction() != null)
							{
								Street[] streets_ = nodes[k].getJunction().getPriorityStreets();
								infobuilder.append("+------------------------------------+\n");
								
								for(Street s : streets_)
								{
									
									infobuilder.append("| 管轄道路 "+s.getName()+"\n");
								}
								infobuilder.append("+------------------------------------+\n");
							}
							if(nodes[k].getTrafficLight_() != null)
							{
								
								
								TrafficLight tmp = nodes[k].getTrafficLight_();
								String trafficLightID = String.valueOf(tmp.getID());
								
								
								infobuilder.append("| 紅綠燈識別號 "+trafficLightID+"\n");
								infobuilder.append("| 紅綠燈時脈 \n | ●紅燈："+nodes[k].getTrafficLight_().getRedPhaseLength()+" (ms) "
										+ "\n | ●黃燈："+nodes[k].getTrafficLight_().getYellowPhaseLength()+" (ms) "
										+ "\n | ●綠燈："+nodes[k].getTrafficLight_().getGreenPhaseLength()+" (ms) "+"\n");
								infobuilder.append("| 紅綠燈目前燈號 "+nodes[k].getTrafficLight_().getStateString()+"\n");
								
								
								
								
								
								infobuilder.append("+------------------------------------+\n");
							}
							
							
					 }
				
					 
	//				 infobuilder.append("######################################\r\n");
	//				 infobuilder.append("紅綠燈資訊 \r\n");
					 
	//				 if(trafficLights_ != null)
	//				 {
	//					// if(tmpLight == null) tmpLight = trafficlight;
	//					
	//					 for(TrafficLight trafficLight : trafficLights_)
	//					 {
	//						 infobuilder.append("+ 紅綠燈位置： ");
	//						 infobuilder.append(trafficlight.getTrafficLightPositionX());
	//						 infobuilder.append(" (x) , ");
	//						 infobuilder.append(trafficlight.getTrafficLightPositionY());
	//						 infobuilder.append(" (y)\r\n");
	//						 infobuilder.append("+ 管轄道路： \r\n");
	//						 Street[] ruleStreets = trafficlight.getTrafficLightRuleStreet();
	//						 for(Street s : ruleStreets)
	//						 {
	//							 infobuilder.append("\t● ");
	//							 infobuilder.append(s.getName());
	//							 infobuilder.append("\r\n");
	//						 }
	//						 infobuilder.append("\r\n----------------------\r\n");
	//					 }
	//				 }
	
					 
					 /*
					 
					 for(int k=0;k<streets.length;k++)
					 {
						 infobuilder.append("----------------------------------------------------\r\n");
						 infobuilder.append("街道資訊 \r\n");
						 infobuilder.append("+ 道路名稱： ");
						 infobuilder.append(streets[k].getName());
						 infobuilder.append("\r\n");
						 infobuilder.append("+ 道路型態： ");
						 infobuilder.append(streets[k].getStreetType_());
						 infobuilder.append("\r\n");
						 infobuilder.append("+ 紅綠燈座標： ");
						 infobuilder.append(streets[k].getTrafficLightStartX_());
						 infobuilder.append(" (x), ");
						 infobuilder.append(streets[k].getTrafficLightStartY_());
						 infobuilder.append(" (y)\r\n");
						 
						 
						 
						 
					 }
					 /*
					 for(int k=0;k<vehicles.length;k++)
					 {
						 infobuilder.append("+++++++++++++++++++++++++++++++++++++++++++++++++++++++\r\n");
					 
						// infobuilder.append(Messages.getString("MouseClickManager.vehicleInformation")); //$NON-NLS-1$
						 infobuilder.append("行車資訊 \r\n");
						 infobuilder.append("\r\n");
						// infobuilder.append(Messages.getString("MouseClickManager.vehicleID")); //$NON-NLS-1$
						 infobuilder.append("車輛ID：");
						 infobuilder.append(vehicles[k].getHexID());
						 infobuilder.append("\r\n");	//$NON-NLS-1$
						// infobuilder.append(Messages.getString("MouseClickManager.vehicleStart")); //$NON-NLS-1$
						 infobuilder.append("車輛起點座標：");
						 infobuilder.append(vehicles[k].getStartPoint().getX());
						 infobuilder.append(" (x), ");	//$NON-NLS-1$
						 infobuilder.append(vehicles[k].getStartPoint().getY());
						 infobuilder.append(" (y) \r\n");	//$NON-NLS-1$
						 infobuilder.append("起點道路名稱：");
						 infobuilder.append(vehicles[k].getStartPoint().getStreet().getName());
						 infobuilder.append(" \r\n ");
						 infobuilder.append("車輛目的座標：");
						 infobuilder.append(vehicles[k].getDestinationPoint().getX());
						 infobuilder.append(" (x), ");	//$NON-NLS-1$
						 infobuilder.append(vehicles[k].getDestinationPoint().getY());
						 infobuilder.append(" (y) \r\n");	//$NON-NLS-1$
						 infobuilder.append("目的道路名稱：");
						 infobuilder.append(vehicles[k].getDestinationPoint().getStreet().getName());
						 infobuilder.append(" \r\n ");
						 infobuilder.append("行車路線：");
						 Street[] tmp = vehicles[k].getRouteStreets();
						 for(Street s : tmp)
						 {
							 
							 infobuilder.append(s.getName());
							 infobuilder.append(" ----> ");
						 }
						 infobuilder.append(" \r\n ");
						 infobuilder.append("目前所在位置：");
						 infobuilder.append(vehicles[k].getCurStreet().getName());
						 infobuilder.append(" \r\n ");	//$NON-NLS-1$
						 infobuilder.append("等待時間：");
						 infobuilder.append(INTEGER_FORMAT_FRACTION.format(vehicles[k].getCurWaitTime()/1000.0));
						 infobuilder.append(" s \r\n ");
						 
						 infobuilder.append("等待紅綠燈：");
						 infobuilder.append(vehicles[k].isWaitingForSignal_());
						 infobuilder.append(" \r\n ");
						 
						 infobuilder.append("累積等待紅綠燈時間：");
						 infobuilder.append(INTEGER_FORMAT_FRACTION.format(vehicles[k].getAccuWaitTime()/1000.0));
						 infobuilder.append(" s \r\n ");
						 
						 infobuilder.append("車輛最高速限：");
						 infobuilder.append(INTEGER_FORMAT_FRACTION.format(vehicles[k].getMaxSpeed()/100000.0*3600));
						 infobuilder.append(" km/h \r\n ");
						 
						// infobuilder.append(Messages.getString("MouseClickManager.vehiclesCurrentSpeed")); //$NON-NLS-1$
						 infobuilder.append("目前車速：");
						 infobuilder.append(INTEGER_FORMAT_FRACTION.format(vehicles[k].getCurSpeed()/100000.0*3600));
						 infobuilder.append(" km/h \r\n");	//$NON-NLS-1$
						// infobuilder.append(Messages.getString("MouseClickManager.travelTime")); //$NON-NLS-1$
						 infobuilder.append("旅行時間：");
						 infobuilder.append(INTEGER_FORMAT_FRACTION.format(vehicles[k].getTotalTravelTime()/1000.0));
						 infobuilder.append(" s \r\n");	//$NON-NLS-1$
						// infobuilder.append(Messages.getString("MouseClickManager.travelDistance")); //$NON-NLS-1$
						 infobuilder.append("旅行距離：");
						 infobuilder.append(INTEGER_FORMAT_FRACTION.format(vehicles[k].getTotalTravelDistance()/100));
						 infobuilder.append(" m \r\n");	//$NON-NLS-1$
						 
						 
					 }
					 
					 
					 }
				 }
			  
			 
			 info = infobuilder.toString();
			 fw.write(info);
		//	 VanetSimStart.getTextArea().setText(info);
			 
			 fw.flush();
			 fw.close();
			 
			 isTrafficLightReady = true;
		    
			 
			 
			 int size = trafficLightArrayList.size();
			 
			 Debug.debugInfo("ArrayList",String.valueOf(size) , Debug.ISLOGGED);
			 
			 System.out.println("size =>"+size);
			 
			 
			 JLabel[] lbls = VanetSimStart.getLeftlbls();
			 JTextArea[] txts = VanetSimStart.getLeftText();

			 JPanel pnl = VanetSimStart.getLeftPanel();
			 
			 ScrollPane scrollPane = VanetSimStart.getLeftScrollPane();
			 
			 
			 lbls = new JLabel[size];
			 txts = new JTextArea[size];
			 
			 SignalControlPanel[] signalPnls = VanetSimStart.getSignalControlPanel();
			 SignalControlLabel[] signallbls = VanetSimStart.getSignalControlLabel();
			 
			 JPanel pnl2 = VanetSimStart.getRightPanel();
			 ScrollPane scrollPane2 = VanetSimStart.getRightScrollPane();
			 JScrollPane jsp = VanetSimStart.getJRightScrollPane();
			
			// signalPnls = new SignalControlPanel[size];
			 
			 
			// pnl.removeAll();
			 pnl2.removeAll();
			 int index = 0;
				
			 for(TrafficLight trafficLight : trafficLightArrayList)   
			 {
				 
				 
				 
				 String msg = "[ "+(index+1)+ "] 紅綠燈位置：\r\n"+trafficLight.getStreetString();
				
				 
				 System.out.println(msg);
				 int state = trafficLight.getState();
				 
				 if(index >= 0 && index <=45)
				 {
					 
				 
				 if(isPanelReady == false)
				 {
					 
					 lbls[index] = new JLabel(msg);
					 pnl.add(lbls[index]);
					 lbls[index].setVisible(true);
				 
					 signalPnls = new SignalControlPanel[size];
					 signalPnls[index] = new SignalControlPanel().getSignalControlPanel(state);
					 JLabel lbl = new JLabel();
					 lbl.setForeground(Color.white);
					 lbl.setText("[ " + String.valueOf(index+1) + " ]");
					 pnl2.add(lbl);
					 pnl2.add(signalPnls[index]);
					 signalPnls[index].setVisible(true);
					 
			     }
				 else
				 {
					 signalPnls = new SignalControlPanel[size];
					 signalPnls[index] = new SignalControlPanel().getSignalControlPanel(state);
					 JLabel lbl = new JLabel();
					 lbl.setForeground(Color.white);
					 lbl.setText("[ " + String.valueOf(index+1) + " ]");
					 pnl2.add(lbl);
					 pnl2.add(signalPnls[index]);
					 signalPnls[index].setVisible(true);
					 pnl2.updateUI();
				 }
				 
				 }
					 
				 
				 
//				    int state = trafficLightMap.get(key).getState();
//				 
//					if(state == 0) 
//					{
//						VanetSimStart.getColor()[0] = Color.green;
//						VanetSimStart.getColor()[1] = Color.white;
//						VanetSimStart.getColor()[2] = Color.white;
//					}
//					else if(state == 1) 
//					{
//						VanetSimStart.getColor()[0] = Color.white;
//						VanetSimStart.getColor()[1] = Color.yellow;
//						VanetSimStart.getColor()[2] = Color.white;
//					}
//					else if(state == 2) 
//					{
//						VanetSimStart.getColor()[0] = Color.white;
//						VanetSimStart.getColor()[1] = Color.white;
//						VanetSimStart.getColor()[2] = Color.red;
//					}
				 
				 index++;
				 
			 
				 
			 }
			 
			 if(isPanelReady == false)
			 {
				 pnl.setVisible(true);
				 scrollPane.add(pnl);
				 scrollPane.setVisible(true);
				 pnl.setVisible(true);
				 scrollPane2.add(pnl2);
				 scrollPane2.setVisible(true);
				 pnl2.setVisible(true); 
			 }
			 
			 isPanelReady = true;
			
		//	 VanetSimStart.getSubFrame().repaint();
			 
				//VanetSimStart.getSubFrame().repaint();
		 
		 
		// showTrafficLight(trafficLights);
		 
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return info;	
	}
	
	*/
	
	private final ArrayList<String> createEnhancedInformationString(){
		double dx, dy;
		WayPoint tmpWayPoint;
		informationText_.setLength(0);	//clear without constructing a new one. Very efficient as max capacity of StringBuilder stays allocated, only the length variable gets changed (=> doesn't need to expand through array copys!)
	   // informationTextCht_.setLength(0);
		
		ArrayList<String> tmp = new ArrayList<String>();
		
		if(startRunTime.equals("") || startRunTime == "")   
		{
			startDate = new java.util.Date();
			startRunTime = startDate.toString();
		}
		
		if(markedStreet_ == cachedStreet_){
			informationText_.append(cachedStreetInformation_);
		} else {		
			if(markedStreet_ != null){
				informationText_.append(Messages.getString("MouseClickManager.streetInformation")); //$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetName"));	//using append on our own as otherwise (using "+") lots of new temporary StringBuilders are created. //$NON-NLS-1$
				informationText_.append(markedStreet_.getName());
				informationText_.append("\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetLength")); //$NON-NLS-1$
				informationText_.append(INTEGER_FORMAT_FRACTION.format(markedStreet_.getLength()/100));
				informationText_.append(" m\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetSpeed")); //$NON-NLS-1$
				informationText_.append(INTEGER_FORMAT_FRACTION.format(markedStreet_.getSpeed()/100000.0*3600));
				informationText_.append(" km/h\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.lanesPerDirection")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getLanesCount());
				informationText_.append("\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetStart")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getStartNode().getX());
				informationText_.append(" (x), ");	//$NON-NLS-1$
				informationText_.append(markedStreet_.getStartNode().getY());
				informationText_.append(" (y)");	//$NON-NLS-1$
				if(markedStreet_.getStartNode().getJunction() != null) informationText_.append(Messages.getString("MouseClickManager.junction")); //$NON-NLS-1$
				informationText_.append("\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.crossingsOutgoings")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getStartNode().getCrossingStreetsCount());
				informationText_.append("/"); //$NON-NLS-1$
				informationText_.append(markedStreet_.getStartNode().getOutgoingStreetsCount());
				informationText_.append("\n"); //$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetEnd")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getEndNode().getX());
				informationText_.append(" (x),");	//$NON-NLS-1$
				informationText_.append(markedStreet_.getEndNode().getY());
				informationText_.append(" (y)");	//$NON-NLS-1$
				if(markedStreet_.getEndNode().getJunction() != null) informationText_.append(Messages.getString("MouseClickManager.junction")); //$NON-NLS-1$
				informationText_.append("\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.crossingsOutgoings")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getEndNode().getCrossingStreetsCount());
				informationText_.append("/"); //$NON-NLS-1$
				informationText_.append(markedStreet_.getEndNode().getOutgoingStreetsCount());
				informationText_.append("\n"); //$NON-NLS-1$
			}
			cachedStreet_ = markedStreet_;
			cachedStreetInformation_ = informationText_.toString();			
		}
		if(isVehicleArrived_ == false){
			
			
			if(markedVehicle_ != null){
			if(informationText_.length() != 0) informationText_.append("\n"); //$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.vehicleInformation")); //$NON-NLS-1$
			informationTextCht_.append("車輛資訊如下：");
			informationTextCht_.append("\r\n");
			
			informationText_.append(Messages.getString("MouseClickManager.vehicleID")); //$NON-NLS-1$
			informationTextCht_.append("車牌號碼：");
			informationText_.append(markedVehicle_.getHexID());
		//	informationTextCht_.append(markedVehicle_.getHexID());
			informationTextCht_.append("ANE-2210");
			informationText_.append("\n");	//$NON-NLS-1$
			informationTextCht_.append("\r\n");
			informationTextCht_.append("車輛起點：");
			informationTextCht_.append(markedVehicle_.getStartPoint().getStreet().getName());
			informationTextCht_.append("\r\n");
			informationTextCht_.append("車輛終點：");
			informationTextCht_.append(markedVehicle_.getDestinationPoint().getStreet().getName());
			informationTextCht_.append("\r\n");
			
			int dstX = markedVehicle_.getDestinationPoint().getX();
			int dstY = markedVehicle_.getDestinationPoint().getY();
			int curX = markedVehicle_.getX();
			int curY = markedVehicle_.getY();
			
			informationTextCht_.append("車輛起點座標：");
			informationTextCht_.append(markedVehicle_.getStartPoint().getX()+" (x) , "+markedVehicle_.getStartPoint().getY()+" (y)");
			informationTextCht_.append("\r\n");
			informationTextCht_.append("車輛終點座標：");
			informationTextCht_.append(markedVehicle_.getDestinationPoint().getX()+" (x) , "+markedVehicle_.getDestinationPoint().getY()+" (y)");
			informationTextCht_.append("\r\n");
			
			
			
			informationTextCht_.append("車輛現在座標：");
			informationTextCht_.append(markedVehicle_.getX()+" (x) , "+markedVehicle_.getY()+" (y)");
			informationTextCht_.append("\r\n");
			
			
			
			informationTextCht_.append("行車路線規劃：\r\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			informationTextCht_.append("\r\n");
		
			Street[] streets = markedVehicle_.getRouteStreets();
			int newline = 0;
			informationTextCht_.append(" 起點 ----> ");
			for(Street s : streets)
			{	 
				newline++;
				 informationTextCht_.append(s.getName());
				 informationTextCht_.append(" ----> ");
				 if(newline % 2 == 0) informationTextCht_.append("\r\n");
			}
			informationTextCht_.append(" 終點 \r\n");
			informationTextCht_.append(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>\r\n");

			//markedVehicle_.getCurStreet().getQueue_().addVehicle(markedVehicle_);

			informationTextCht_.append("車輛目前位置：");
			informationTextCht_.append(markedVehicle_.getCurStreet().getName());
			informationTextCht_.append("\r\n");
			
			informationTextCht_.append("車輛目前優先權：");
			informationTextCht_.append(markedVehicle_.getPriority());
			informationTextCht_.append("\r\n");
			
			
			
			informationTextCht_.append("目前行車狀態：");
			if(markedVehicle_.isWaitingForSignal_())
				informationTextCht_.append("等待紅燈中");
			else if( !markedVehicle_.isWaitingForSignal_() && markedVehicle_.isTrafficJam())
				informationTextCht_.append("道路壅塞中");
			else
				informationTextCht_.append("行進中");
			
			informationTextCht_.append("\r\n");
			
			
			informationText_.append(Messages.getString("MouseClickManager.vehicleStart")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getStartPoint().getX());
			informationText_.append(" (x), ");	//$NON-NLS-1$
			informationText_.append(markedVehicle_.getStartPoint().getY());
			informationText_.append(" (y)\n");	//$NON-NLS-1$				
			tmpWayPoint = markedVehicle_.getDestinations().peekFirst();	//needs to be cached to prevent threading issues (if WayPoint is removed by simulation after the check against null!)
			if(tmpWayPoint != null){
				informationText_.append(Messages.getString("MouseClickManager.vehicleNextDestination")); //$NON-NLS-1$
				
				
				
				informationText_.append(tmpWayPoint.getX());
				informationText_.append(" (x), ");	//$NON-NLS-1$
				informationText_.append(tmpWayPoint.getY());
				informationText_.append(" (y)\n");	//$NON-NLS-1$
				dx = markedVehicle_.getX() - tmpWayPoint.getX();
				dy = markedVehicle_.getY() - tmpWayPoint.getY();
				informationText_.append(Messages.getString("MouseClickManager.linearDistance")); //$NON-NLS-1$
				informationText_.append(INTEGER_FORMAT_FRACTION.format(Math.sqrt(dx * dx + dy * dy)/100));
				informationText_.append(" m\n");	//$NON-NLS-1$
				
				
				informationTextCht_.append("距離終點：");
				informationTextCht_.append(INTEGER_FORMAT_FRACTION.format(Math.sqrt(dx * dx + dy * dy)/100));
				informationTextCht_.append("m \r\n");
				
				if(dx == 0 && dy == 0)  isVehicleArrived_ = true;
				
			}
			String speed = INTEGER_FORMAT_FRACTION.format(markedVehicle_.getCurSpeed()/100000.0*3600);
			double sp = markedVehicle_.getCurSpeed()/100000.0*3600;
			informationText_.append(Messages.getString("MouseClickManager.vehiclesCurrentSpeed")); //$NON-NLS-1$
			informationTextCht_.append("目前車速：");
			informationTextCht_.append(speed);
			informationTextCht_.append(" km/h");
			informationTextCht_.append("\r\n");
			
			
			
			informationTextCht_.append("目前等待紅燈時間：");
			informationTextCht_.append(INTEGER_FORMAT_FRACTION.format(markedVehicle_.getCurWaitTime()/1000.0));
			informationTextCht_.append(" s");
			informationTextCht_.append("\r\n");
			
			informationTextCht_.append("累積等待紅燈時間：");
			informationTextCht_.append(INTEGER_FORMAT_FRACTION.format(markedVehicle_.getAccuWaitTime()/1000.0));
			informationTextCht_.append(" s");
			informationTextCht_.append("\r\n");
			
			informationTextCht_.append("累積塞車時間：");
			informationTextCht_.append(INTEGER_FORMAT_FRACTION.format(markedVehicle_.getAccuBlockingTime()/1000.0));
			informationTextCht_.append(" s");
			informationTextCht_.append("\r\n");
			
			String totalTime = INTEGER_FORMAT_FRACTION.format(markedVehicle_.getTotalTravelTime()/1000.0);
			
			DecimalFormat df=new DecimalFormat("##.#");
			String totT=df.format(markedVehicle_.getTotalTravelTime()/1000.0);   
			
			
			informationText_.append(speed);
			informationText_.append(" km/h\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.travelTime")); //$NON-NLS-1$
			informationText_.append(totalTime);
			
			
			informationTextCht_.append("總行車時間：");
			informationTextCht_.append(totalTime);
			informationTextCht_.append(" s");
			informationTextCht_.append("\r\n");
			
			StatisticsData data = new StatisticsData();
			data.setTime(totT);
			data.setSpeed((int)sp);
			stat.add(data);
			data = null;
			
//			resultMap.put(String.valueOf(INTEGER_FORMAT_FRACTION.format(markedVehicle_.getTotalTravelTime()/1000.0)),
//					Integer.parseInt(INTEGER_FORMAT_FRACTION.format(markedVehicle_.getCurSpeed()/100000.0*3600)));
			
			informationText_.append(" s\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.travelDistance")); //$NON-NLS-1$
			
			informationTextCht_.append("總行車距離：");
			informationTextCht_.append(INTEGER_FORMAT_FRACTION.format(markedVehicle_.getTotalTravelDistance()/100));
			informationTextCht_.append(" m");
			informationTextCht_.append("\r\n");
			
			if( Math.abs(curX-dstX) <= DESTINATION_INTERVALS &&   Math.abs(curY-dstY) <= DESTINATION_INTERVALS)
			{
				
				
				
				isVehicleArrived_ = true;
				informationTextCht_.append("========================\r\n");
				informationTextCht_.append("已抵達終點\r\n");
				informationTextCht_.append("========================\r\n");
				
				endDate = new java.util.Date();
				endRunTime = endDate.toString();
				
				
				informationTextCht_.append("開始模擬時間：");
				informationTextCht_.append(startRunTime);
				informationTextCht_.append("\r\n");
				informationTextCht_.append("結束模擬時間：");
				informationTextCht_.append(endRunTime);
				informationTextCht_.append("\r\n");
				informationTextCht_.append("共耗時：");
				long passTime = endDate.getTime() - startDate.getTime() ;
				
				long passTimeHour = passTime/1000/60/60;
				long passTimeMinute = (passTime - (passTimeHour*60*60*1000))/1000/60;
				long passTimeSecond = (passTime - (passTimeHour*60*60*1000) - (passTimeMinute*60*1000))/1000;
				
				informationTextCht_.append(String.valueOf(passTimeHour));
				informationTextCht_.append(" (時) ");
				informationTextCht_.append(String.valueOf(passTimeMinute));
				informationTextCht_.append(" (分) ");
				informationTextCht_.append(String.valueOf(passTimeSecond));
				informationTextCht_.append(" (秒) ");
				informationTextCht_.append("\r\n");
				
				
				informationTextCht_.append("+---------------+\r\n");
				informationTextCht_.append("|  統計結果                   |\r\n");
				informationTextCht_.append("+---------------+\r\n");
				informationTextCht_.append("| 時間      |   速度     |\r\n");
				informationTextCht_.append("+---------------+\r\n");
				
				for(StatisticsData d : stat)
				{
					
					informationTextCht_.append(d.getTime());
					informationTextCht_.append("            ");
					informationTextCht_.append(d.getSpeed());
					informationTextCht_.append("\r\n");
				}
				
				
				Statistics st = new Statistics("行車速率表");
				st.createResultFrame("time vs velocity",stat);
				st.createResultImage(1024, 1080);
				
				
				StatisticsData st1 = new StatisticsData();
				st1.setTravelTime((markedVehicle_.getTotalTravelTime()/1000.0)-(markedVehicle_.getAccuWaitTime()/1000.0)-(markedVehicle_.getAccuBlockingTime()/1000.0));
				st1.setWaitingTime(markedVehicle_.getAccuWaitTime()/1000.0);
				st1.setBlockingTime(markedVehicle_.getAccuBlockingTime()/1000.0);
				
				ArrayList<StatisticsData> al = new ArrayList<StatisticsData>();
				al.add(st1);
				
				PieChart pc = new PieChart("行車時間佔比");
				pc.createResultFrame("行車時間佔比",al);
				pc.createResultImage(500, 400);
				
				
				
//				Statistics stat = new Statistics("行車速率表");
//				stat.setDataMap(resultMap);
//				stat.createResultFrame("velocity vs time");
//				
//				resultMap.clear();
				
			//	new Statistics("行車速率表").createResultFrame("time vs velocity");
				
				
				report = informationTextCht_.toString();
			}
			
			informationText_.append(INTEGER_FORMAT_FRACTION.format(markedVehicle_.getTotalTravelDistance()/100));
			informationText_.append(" m\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.knownVehicles")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownVehiclesList().getSize());
			informationText_.append("\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.knownMessages")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownMessages().getSize());
			informationText_.append(" (+ "); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownMessages().getOldMessagesSize());
			informationText_.append(Messages.getString("MouseClickManager.old")); //$NON-NLS-1$
			informationText_.append("\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.failedForwardMessages")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownMessages().getFailedForwardCount());
			informationText_.append("\n"); //$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.knownPenalties")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownPenalties().getSize());
			informationText_.append("\n"); //$NON-NLS-1$
			}
		}
		else  // isVehicleArrived_ == true
		{
			tmp.add(informationText_.toString());
			tmp.add(report);
			return tmp;
		}
		
		if(markedNode_ != null){
		}
		
		tmp.add(informationText_.toString());
		tmp.add(informationTextCht_.toString());
		
	    informationTextCht_.setLength(0);
		
		
		return tmp;
		
		
	}
	
	/**
	 * Creates the information text from the marked street and vehicle. The function recycles a StringBuilder and appends
	 * strings so that practically no garbage collection is needed.
	 * 
	 * @return	the text
	 */
	private final String createInformationString(){
		double dx, dy;
		WayPoint tmpWayPoint;
		informationText_.setLength(0);	//clear without constructing a new one. Very efficient as max capacity of StringBuilder stays allocated, only the length variable gets changed (=> doesn't need to expand through array copys!) 
		if(markedStreet_ == cachedStreet_){
			informationText_.append(cachedStreetInformation_);
		} else {		
			if(markedStreet_ != null){
				informationText_.append(Messages.getString("MouseClickManager.streetInformation")); //$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetName"));	//using append on our own as otherwise (using "+") lots of new temporary StringBuilders are created. //$NON-NLS-1$
				informationText_.append(markedStreet_.getName());
				informationText_.append("\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetLength")); //$NON-NLS-1$
				informationText_.append(INTEGER_FORMAT_FRACTION.format(markedStreet_.getLength()/100));
				informationText_.append(" m\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetSpeed")); //$NON-NLS-1$
				informationText_.append(INTEGER_FORMAT_FRACTION.format(markedStreet_.getSpeed()/100000.0*3600));
				informationText_.append(" km/h\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.lanesPerDirection")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getLanesCount());
				informationText_.append("\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetStart")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getStartNode().getX());
				informationText_.append(" (x), ");	//$NON-NLS-1$
				informationText_.append(markedStreet_.getStartNode().getY());
				informationText_.append(" (y)");	//$NON-NLS-1$
				if(markedStreet_.getStartNode().getJunction() != null) informationText_.append(Messages.getString("MouseClickManager.junction")); //$NON-NLS-1$
				informationText_.append("\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.crossingsOutgoings")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getStartNode().getCrossingStreetsCount());
				informationText_.append("/"); //$NON-NLS-1$
				informationText_.append(markedStreet_.getStartNode().getOutgoingStreetsCount());
				informationText_.append("\n"); //$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetEnd")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getEndNode().getX());
				informationText_.append(" (x),");	//$NON-NLS-1$
				informationText_.append(markedStreet_.getEndNode().getY());
				informationText_.append(" (y)");	//$NON-NLS-1$
				if(markedStreet_.getEndNode().getJunction() != null) informationText_.append(Messages.getString("MouseClickManager.junction")); //$NON-NLS-1$
				informationText_.append("\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.crossingsOutgoings")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getEndNode().getCrossingStreetsCount());
				informationText_.append("/"); //$NON-NLS-1$
				informationText_.append(markedStreet_.getEndNode().getOutgoingStreetsCount());
				informationText_.append("\n"); //$NON-NLS-1$
			}
			cachedStreet_ = markedStreet_;
			cachedStreetInformation_ = informationText_.toString();			
		}
		if(markedVehicle_ != null){
			if(informationText_.length() != 0) informationText_.append("\n"); //$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.vehicleInformation")); //$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.vehicleID")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getHexID());
			informationText_.append("\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.vehicleStart")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getStartPoint().getX());
			informationText_.append(" (x), ");	//$NON-NLS-1$
			informationText_.append(markedVehicle_.getStartPoint().getY());
			informationText_.append(" (y)\n");	//$NON-NLS-1$				
			tmpWayPoint = markedVehicle_.getDestinations().peekFirst();	//needs to be cached to prevent threading issues (if WayPoint is removed by simulation after the check against null!)
			if(tmpWayPoint != null){
				informationText_.append(Messages.getString("MouseClickManager.vehicleNextDestination")); //$NON-NLS-1$
				informationText_.append(tmpWayPoint.getX());
				informationText_.append(" (x), ");	//$NON-NLS-1$
				informationText_.append(tmpWayPoint.getY());
				informationText_.append(" (y)\n");	//$NON-NLS-1$
				dx = markedVehicle_.getX() - tmpWayPoint.getX();
				dy = markedVehicle_.getY() - tmpWayPoint.getY();
				informationText_.append(Messages.getString("MouseClickManager.linearDistance")); //$NON-NLS-1$
				informationText_.append(INTEGER_FORMAT_FRACTION.format(Math.sqrt(dx * dx + dy * dy)/100));
				informationText_.append(" m\n");	//$NON-NLS-1$
			}
			informationText_.append(Messages.getString("MouseClickManager.vehiclesCurrentSpeed")); //$NON-NLS-1$
			informationText_.append(INTEGER_FORMAT_FRACTION.format(markedVehicle_.getCurSpeed()/100000.0*3600));
			informationText_.append(" km/h\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.travelTime")); //$NON-NLS-1$
			informationText_.append(INTEGER_FORMAT_FRACTION.format(markedVehicle_.getTotalTravelTime()/1000.0));
			informationText_.append(" s\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.travelDistance")); //$NON-NLS-1$
			informationText_.append(INTEGER_FORMAT_FRACTION.format(markedVehicle_.getTotalTravelDistance()/100));
			informationText_.append(" m\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.knownVehicles")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownVehiclesList().getSize());
			informationText_.append("\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.knownMessages")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownMessages().getSize());
			informationText_.append(" (+ "); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownMessages().getOldMessagesSize());
			informationText_.append(Messages.getString("MouseClickManager.old")); //$NON-NLS-1$
			informationText_.append("\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.failedForwardMessages")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownMessages().getFailedForwardCount());
			informationText_.append("\n"); //$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.knownPenalties")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownPenalties().getSize());
			informationText_.append("\n"); //$NON-NLS-1$
		}
		if(markedNode_ != null){
		}
		return informationText_.toString();
	}
	
	/**
	 * The thread which handles mouse drags and information display.
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run(){
		setName("MouseClickManager"); //$NON-NLS-1$
		Renderer renderer = Renderer.getInstance();
		SimulateControlPanel simulatePanel = VanetSimStart.getMainControlPanel().getSimulatePanel();
	
		int lastInformationRefresh = 0;
		int sleepTime;
		long time = 0;
		setPriority(Thread.MIN_PRIORITY);
		
		while(true){
			markedVehicle_ = Renderer.getInstance().getMarkedVehicle();
			if(Map.isLoadFinish)
			//	writeInformationString();
			
			
	//		writeTrafficLightString();
			
			time = System.nanoTime();
			if(active_ && drawArea_ != null){	//check for mouse clicks if drawArea exists and we're on it
				synchronized(this){	//to prevent incoming mouse events changing variables during this code block!
					if(waitingTime_ > -1){
						if(waitingTime_ > DRAG_ACTIVATION_INTERVAL){	//change mousecursor after specified time
							drawArea_.setCursor(moveCursor_);
							waitingTime_ = -1;
						}else waitingTime_ += 2;	//it's not guaranteed that the sleep below really lasts 2ms but this should be fine as it doesn't need to be that precise...
					}
					if(releaseTime_ - pressTime_ > DRAG_ACTIVATION_INTERVAL){	//pan the map if mouse was pressed longer than specified time
						renderer.pan((pressedX_ - releasedX_)*2, (pressedY_ - releasedY_)*2);
						ReRenderManager.getInstance().doReRender();
						releaseTime_ = pressTime_;
					} else if (releaseTime_ != pressTime_){	//get information about a point if click was shorter
						int distance = (int)Math.round(Math.min(80/renderer.getMapZoom(), 1000000));
						if(distance < 3000) distance = 3000;
						markedStreet_ = MapHelper.findNearestStreet(pressedX_, pressedY_, distance, new double[2], new int[2]);
						renderer.setMarkedStreet(markedStreet_);
						markedVehicle_ = MapHelper.findNearestVehicle(pressedX_, pressedY_, distance, new long[1]);
						renderer.setMarkedVehicle(markedVehicle_);
						markedNode_ = MapHelper.findNearestNode(pressedX_, pressedY_, distance, new long[1]);
						renderer.ReRender(false, false);
						releaseTime_ = pressTime_;
					}
				}
			}
			//it's rather costly to create this text and set the text area so it's only done periodically
			if(lastInformationRefresh < 0){
				lastInformationRefresh = INFORMATION_REFRESH_INTERVAL;
				
				//String info = createInformationString();
				//simulatePanel.setInformation(info);
				//VanetSimStart.getVehicleTextArea().setText(info);
				
				ArrayList<String> info = createEnhancedInformationString();
				simulatePanel.setInformation(info.get(0));
				VanetSimStart.getVehicleTextArea().setText(info.get(1));
				
			//	createTrafficLightInfo();
				VanetSimStart.getTrafficLightTextArea().setText(createTrafficLightInfo());
				
			}
			if(active_)sleepTime = 2;
			else sleepTime = 50;
			lastInformationRefresh -= sleepTime;
			time = ((System.nanoTime() - time)/1000000);			
			if(time > 0) time = sleepTime - time;
			else time = sleepTime + time;	//nanoTime might overflow			
			//sleep
			if(time > 0 && time <= sleepTime){
				try{
					sleep(time);
				} catch (Exception e){};
			}			
		}
	}
}