package vanetsim;

import vanetsim.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.jvnet.substance.SubstanceLookAndFeel;

import vanetsim.statistics.Statistics;
import vanetsim.debug.Debug;
import vanetsim.gui.DrawingArea;
import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.MainControlPanel;
import vanetsim.gui.controlpanels.SignalControlLabel;
import vanetsim.gui.controlpanels.SignalControlPanel;
import vanetsim.gui.controlpanels.TrafficLightPanel;
import vanetsim.gui.helpers.MouseClickManager;
import vanetsim.gui.helpers.ProgressOverlay;
import vanetsim.gui.helpers.ReRenderManager;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.TrafficLight;
import vanetsim.simulation.SimulationMaster;

/**
 * This is the main class for the VANet-Simulator which starts the GUI and all other components.
 */
public final class VanetSimStart implements Runnable {

	/** The master thread for simulation delegation. Stored here if any other class needs control over it. */
	private static SimulationMaster simulationMaster_;

	/** The controlpanel on the right side. */
	private static MainControlPanel controlPanel_;

	/** The <code>JFrame</code> which is the base of the application. */
	private static JFrame mainFrame_;
	
	private static JFrame trafficLightFrame_; public static JFrame getTrafficLightFrame(){return trafficLightFrame_;}
	private static ScrollPane trafficLightScrollPane_; public static ScrollPane getTrafficLightScrollPane(){return trafficLightScrollPane_;}
	private static JTextArea trafficLightTextArea_; public static JTextArea getTrafficLightTextArea(){return trafficLightTextArea_;}
	
	
	private static JFrame vehicleFrame_; public static JFrame getVehicleFrame(){return vehicleFrame_;}
	private static ScrollPane vehicleScrollPane_; public static ScrollPane getVehicleScrollPane(){return vehicleScrollPane_;}
	private static JTextArea vehicleTextArea_; public static JTextArea getVehicleTextArea(){return vehicleTextArea_;}
	
	private static JFrame subFrame_;  public static JFrame getSubFrame(){return subFrame_;}
	private static JTextArea textArea_ ; public static JTextArea getTextArea(){return textArea_;}
	private static JPanel signalPanel_ ; public static JPanel getSignalPanel_(){return signalPanel_;}
	private static JButton signalbtn_ ; public static JButton getSignalbtn_(){return signalbtn_;}
	private static ScrollPane leftScrollPane_ ;  public static ScrollPane getLeftScrollPane(){return leftScrollPane_;}
	private static ScrollPane rightScrollPane_ ;  public static ScrollPane getRightScrollPane(){return rightScrollPane_;}
	private static JScrollPane jrightScrollPane_ ;  public static JScrollPane getJRightScrollPane(){return jrightScrollPane_;}
	
	
	private static HashMap<String, TrafficLight> map = new HashMap<String,TrafficLight>();
	public static HashMap<String, TrafficLight> getHashMap(){return map;}
	
	private static JPanel leftPanel_ = new  JPanel(); public static JPanel getLeftPanel(){return leftPanel_;}
	private static JPanel rightPanel_ = new  JPanel(); public static JPanel getRightPanel(){return rightPanel_;}
	private static JButton[] leftBtn_ ; public static JButton[] getLeftBtns(){return leftBtn_;}
	private static JButton[] rightBtn_ ; public static JButton[] getRightBtns(){return rightBtn_;}
	
	private static JLabel[] leftlbl_ ; public static JLabel[] getLeftlbls(){return leftlbl_;}
	private static JTextArea[] leftTxt_ ; public static JTextArea[] getLeftText(){return leftTxt_;}
	private static JPanel[] rightPnl_ ; public static JPanel[] getRightPnls(){return rightPnl_;}
	private static SignalControlPanel[] signalPnl_ ; public static SignalControlPanel[] getSignalControlPanel(){return signalPnl_;}
	private static SignalControlLabel[] signallbl_ ; public static SignalControlLabel[] getSignalControlLabel(){return signallbl_;}
	
	
	private static JLabel initlbl_ ; public static JLabel getInitlbl(){return initlbl_;}
	private static JPanel initPnl_ ; public static JPanel getInitPnl(){return initPnl_;}
	
	/** <code>true</code> if double buffering shall be used on the drawing area. */
	private static boolean useDoubleBuffering_;
	
	/** <code>true</code> if a manual buffering shall be used on the drawing area. */
	private static boolean drawManualBuffered_;

	/** A reference to the progress bar. */
	private static ProgressOverlay progressBar_;
	
	private static Color[] drawColor = new Color[]{Color.white,Color.white,Color.white};
	private static Color[] color = new Color[]{Color.red,Color.yellow,Color.green};
	public static Color[] getColor(){return drawColor;}
	
	private static TrafficLightPanel trafficLightPanel ; public static TrafficLightPanel getTrafficLightPanel(){return trafficLightPanel;}
	
	public VanetSimStart(){
		
		Debug.whereru("VanetSimStart", Debug.ISLOGGED);
		Debug.callFunctionInfo("VanetSimStart", "VanetSimStart()", Debug.ISLOGGED);
		
	//	new Statistics("行車速率表").createResultFrame("time vs velocity");
		
	//	readconfig("./config.txt"); //$NON-NLS-1$
		
	}

	/**
	 * Thread which creates the GUI.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		Debug.callFunctionInfo("VanetSimStart", "run()", Debug.ISLOGGED);
		
		Debug.detailedInfo("Thread which creates the GUI.", Debug.ISLOGGED);
		
		Debug.detailedInfo("creating MainFrame ...", Debug.ISLOGGED);
		mainFrame_ = new JFrame();
		mainFrame_.setTitle(Messages.getString("StartGUI.applicationtitle")); //$NON-NLS-1$
		mainFrame_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Debug.detailedInfo("create MainFrame finish", true);
		
		
		
		
	//	subFrame_ = new JFrame();
	//	subFrame_.setTitle("紅綠燈資訊"); //$NON-NLS-1$
	//	subFrame_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		vehicleFrame_ = new JFrame("車輛資訊");
		vehicleFrame_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		
		
		progressBar_ = new ProgressOverlay();
		if(Runtime.getRuntime().maxMemory() < 120000000) ErrorLog.log(Messages.getString("StartGUI.detectedLowMemory"), 6, VanetSimStart.class.getName(), "run", null); //$NON-NLS-1$ //$NON-NLS-2$
		URL appicon = ClassLoader.getSystemResource("vanetsim/images/appicon.gif"); //$NON-NLS-1$
		if (appicon != null){
			mainFrame_.setIconImage(Toolkit.getDefaultToolkit().getImage(appicon));
		} else ErrorLog.log(Messages.getString("StartGUI.noAppIcon"), 6, VanetSimStart.class.getName(), "run", null); //$NON-NLS-1$ //$NON-NLS-2$

		DrawingArea drawarea = addComponentsToPane(mainFrame_.getContentPane());
		Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds(); 
		mainFrame_.pack();
		mainFrame_.setSize((int) bounds.getWidth(), (int) bounds.getHeight());
		mainFrame_.setLocationRelativeTo(null); // center on screen
		mainFrame_.setResizable(true);
		mainFrame_.setVisible(true);
		
		vehicleFrame_.pack();
		vehicleFrame_.setBounds(0, 0, 500, 500);
		vehicleFrame_.setResizable(true);
		vehicleScrollPane_ = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
		vehicleTextArea_ = new JTextArea();
		vehicleTextArea_.setBackground(Color.black);
		vehicleTextArea_.setForeground(Color.white);
		vehicleTextArea_.setFont(new Font("標楷體",Font.BOLD,20));
	//	vehicleTextArea_.setEditable(false);
		vehicleScrollPane_.add(vehicleTextArea_);
		vehicleFrame_.add(vehicleScrollPane_);
		vehicleTextArea_.setVisible(true);
		vehicleScrollPane_.setVisible(true);
		vehicleFrame_.setVisible(true);
		
		
		trafficLightFrame_ = new JFrame("紅綠燈資訊");
		trafficLightFrame_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		trafficLightFrame_.pack();
		trafficLightFrame_.setBounds(0, 0, 800, 800);
		trafficLightFrame_.setResizable(true);
		trafficLightScrollPane_ = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
		trafficLightTextArea_ = new JTextArea();
		trafficLightTextArea_.setBackground(Color.black);
		trafficLightTextArea_.setForeground(Color.white);
		trafficLightTextArea_.setFont(new Font("標楷體",Font.BOLD,20));
		trafficLightScrollPane_.add(trafficLightTextArea_);
		trafficLightFrame_.add(trafficLightScrollPane_);
		trafficLightTextArea_.setVisible(true);
		trafficLightScrollPane_.setVisible(true);
		trafficLightFrame_.setVisible(true);
		
		
		
	//	subFrame_.pack();
	//	subFrame_.setSize(1500,900);
	//	subFrame_.setBounds(0, 0, 1500, 900);
	//	subFrame_.setLocationRelativeTo(null); // center on screen
	//	subFrame_.setResizable(true);
	//	subFrame_.getContentPane().setLayout(new GridLayout(0,2));
	//	leftScrollPane_ = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
	//	leftScrollPane_.setBackground(Color.black);	
	//	leftScrollPane_.setForeground(Color.white);
	//	leftPanel_ = new JPanel();
	//	leftPanel_.setLayout(new GridLayout(0, 1));
	//	leftPanel_.setBackground(Color.black);
	//	leftPanel_.setForeground(Color.white);
		

	
//		leftScrollPane_.add(leftPanel_);
//		leftPanel_.setVisible(true);
//		
//		rightPanel_ = new JPanel();
//		rightPanel_.setLayout(new GridLayout(0, 2));
//		rightPanel_.setBackground(Color.black);
//		
//		rightScrollPane_ = new ScrollPane();
//		rightScrollPane_.add(rightPanel_);
//		rightPanel_.setVisible(true);
	
		
	   
		
	//	scrollPane_ = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
	//	scrollPane_.setLayout(null);
//		leftPanel_ = new JPanel();
//		leftPanel_.setBackground(Color.blue);
//		scrollPane_.add(leftPanel_);
//		leftPanel_.setVisible(true);
//		
//		scrollPane_.setVisible(true);
//		subFrame_.add(scrollPane_, BorderLayout.CENTER);
		
		
		
		
//		
//		rightPanel_ = new JPanel();
//		rightPanel_.setLayout(null);
//		rightPanel_.setBackground(Color.red);
//		rightPanel_.setVisible(true);
		
	
		
	//	textArea_ = new JTextArea();
	//	trafficLightPanel = new TrafficLightPanel();
	//	trafficLightPanel.setVisible(true);
	//	textArea_.setVisible(true);
	//	textArea_.setFont(new Font(Font.DIALOG, Font.BOLD, 25));
	//	scrollPane_.add(textArea_);
	//	scrollPane_.add(trafficLightPanel);
	//	scrollPane_.setVisible(true);
		
		
		
//		signalPanel_ = new JPanel()
//		{
//            public void paint(Graphics g)
//            {    
//                g.setColor(drawColor[0]);
//                g.fillOval(0,0,20,20);
//                g.setColor(drawColor[1]);
//                g.fillOval(25,0,20,20);
//                g.setColor(drawColor[2]);
//                g.fillOval(50,0,20,20);
//              }
//        };
//		signalPanel_.setSize((int) bounds.getWidth()/2, (int) bounds.getHeight());
//        signalPanel_.setVisible(true);
        
        
	//	subFrame_.getContentPane().add(leftPanel_, BorderLayout.CENTER);
	
		
		controlPanel_.getEditPanel().setEditMode(false);
		
		Debug.detailedInfo("initiailze JFrame, JAVA Swing finish", Debug.ISLOGGED);

		simulationMaster_ = new SimulationMaster();
		simulationMaster_.start();
		Map.getInstance().initNewMap(100000, 100000, 10000, 10000);
		Map.getInstance().signalMapLoaded();
		ReRenderManager.getInstance().start();
		
		
		MouseClickManager.getInstance().setDrawArea(drawarea);
		MouseClickManager.getInstance().start();
		
		Debug.detailedInfo("use SimulationMaster、Map、ReRenderManager、MouseClickManager", Debug.ISLOGGED);
		
		
	//	subFrame_.add(leftScrollPane_);
	//	subFrame_.add(rightScrollPane_);
		
	//	leftScrollPane_.setVisible(true);
	//	rightScrollPane_.setVisible(true);
		
		
	//	subFrame_.setVisible(true);
	}

	
	/**
	 * Function to add the control elements to a container.
	 * 
	 * @param container	the container on which to add the elements
	 * 
	 * @return the constructed <code>DrawingArea</code>
	 */
	public static DrawingArea addComponentsToPane(Container container) {
		
		Debug.callFunctionInfo("VanetSimStart", "addComponentsToPane(Container container)", Debug.ISLOGGED);
		
		container.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		DrawingArea drawarea = new DrawingArea(useDoubleBuffering_, drawManualBuffered_);
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		container.add(drawarea, c);
		Renderer.getInstance().setDrawArea(drawarea);

		controlPanel_ = new MainControlPanel();
		controlPanel_.setPreferredSize(new Dimension(200, 100000));
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.gridx = 1;
		c.gridy = 0;
		container.add(controlPanel_, c);

		return drawarea;
	}

	/**
	 * Sets the display state of the progress bar.
	 * 
	 * @param state	<code>true</code> to display the progress bar, <code>false</code> to disable it
	 */
	public static void setProgressBar(boolean state){
		progressBar_.setVisible(state);
	}
	
	/**
	 * Gets the control panel on the right side.
	 * 
	 * @return the control panel
	 */
	public static MainControlPanel getMainControlPanel(){
		return controlPanel_;
	}

	/**
	 * Gets the initial <code>JFrame</code> of the application.
	 * 
	 * @return the <code>JFrame</code>
	 */
	public static JFrame getMainFrame(){
		return mainFrame_;
	}

	/**
	 * Returns the simulation master (for example in order to stop or start simulation).
	 * 
	 * @return the simulation master
	 */
	public static SimulationMaster getSimulationMaster(){
		return simulationMaster_;
	}

	/**
	 * Reads the parameters from the configuration file.
	 * 
	 * @param configFilePath	path to the configuration file
	 */
	private static void readconfig(String configFilePath) {
		
		Debug.callFunctionInfo("VanetSimStart", "readconfig(String configFilePath)", Debug.ISLOGGED);
		
		String loggerFormat, loggerDir;
		Integer loggerLevel;
		Long loggerTrashtime;
		boolean loggerFormatError = false;
		Properties configFile = new Properties();
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		
		
		try {
			configFile.load(new FileInputStream(configFilePath));

			String guiTheme = configFile.getProperty("gui_theme", ""); //$NON-NLS-1$ //$NON-NLS-2$
			// set substance theme
			if (!guiTheme.equals("")) { //$NON-NLS-1$
				try {
					JFrame.setDefaultLookAndFeelDecorated(true);
					JDialog.setDefaultLookAndFeelDecorated(true);
					UIManager.setLookAndFeel("org.jvnet.substance.skin." + guiTheme); //$NON-NLS-1$
					SubstanceLookAndFeel.setToUseConstantThemesOnDialogs(true);
				} catch (Exception e) {
					ErrorLog.log(Messages.getString("StartGUI.substanceThemeError"), 3, VanetSimStart.class.getName(), "readconfig", e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			// read parameters for logfile
			loggerTrashtime = Long.parseLong(configFile.getProperty("logger_trashtime", "365000")); //$NON-NLS-1$ //$NON-NLS-2$
			loggerDir = configFile.getProperty("logger_dir", "./"); //$NON-NLS-1$ //$NON-NLS-2$
			loggerFormat = configFile.getProperty("logger_format", "txt"); //$NON-NLS-1$ //$NON-NLS-2$
			loggerLevel = Integer.parseInt(configFile.getProperty("logger_level", "1")); //$NON-NLS-1$ //$NON-NLS-2$

			if (!loggerFormat.equals("txt") && !loggerFormat.equals("xml")) { //$NON-NLS-1$ //$NON-NLS-2$
				loggerFormatError = true;
				loggerFormat = "txt"; //$NON-NLS-1$
			}

			ErrorLog.setParameters(loggerLevel, loggerDir, loggerFormat);

			if (loggerTrashtime < 0 || loggerTrashtime > 365000) {
				loggerTrashtime = (long) 365000;
				ErrorLog.log("", 4, VanetSimStart.class.getName(), "readconfig", null); //$NON-NLS-1$ //$NON-NLS-2$
			}
			ErrorLog.deleteOld(loggerTrashtime, loggerDir);

			if (loggerFormatError) ErrorLog.log(Messages.getString("StartGUI.wrongLogformat"), 4, VanetSimStart.class.getName(), "readconfig", null); //$NON-NLS-1$ //$NON-NLS-2$
			if (loggerLevel < 1 || loggerLevel > 7) ErrorLog.log(Messages.getString("StartGUI.wrongLoglevel"), 4, VanetSimStart.class.getName(), "readconfig", null); //$NON-NLS-1$ //$NON-NLS-2$
		
			useDoubleBuffering_ = Boolean.parseBoolean(configFile.getProperty("double_buffer", "true")); //$NON-NLS-1$ //$NON-NLS-2$
			drawManualBuffered_ = Boolean.parseBoolean(configFile.getProperty("draw_manual_buffered", "false")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
			ErrorLog.log(Messages.getString("StartGUI.whileConfigreading"), 7, VanetSimStart.class.getName(), "readconfig",  e); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit(1);
		}
		
	}

}