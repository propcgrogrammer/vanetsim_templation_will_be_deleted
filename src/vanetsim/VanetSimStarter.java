package vanetsim;

import javax.swing.SwingUtilities;

import vanetsim.debug.Debug;


public class VanetSimStarter {
	/**
	 * ®
	 * 
	 * @param args	command line arguments. No argument is given the simulator will start in GUI-Mode.
	 * if 3 arguments are given the simulator will start without GUI in console mode. args[0] = map path; args[1] = scenario path args[2] = time until the simulation stops 
	 * example for console mode: java -jar VanetSimStarter.jar /Users/Max_Mustermann/rgb-1.xml /Users/Max_Mustermann/rgb-1_scen.xml 50000 
	 */
	public static void main(String[] args) {
		
		Debug.whereru("VanetSimStarter", true);
		Debug.callFunctionInfo("VanetSimStarter", "main()", true);
		
	
		if(args.length < 3) SwingUtilities.invokeLater(new VanetSimStart());
		else SwingUtilities.invokeLater(new ConsoleStart(args[0], args[1], args[2]));

	}


}
