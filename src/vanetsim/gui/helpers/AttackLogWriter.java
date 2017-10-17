package vanetsim.gui.helpers;

import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.XMLFormatter;
import java.util.logging.Logger;

//import java16.util.logging.Logger;


import vanetsim.ErrorLog;
import vanetsim.localization.Messages;

/**
 * Helper Class for error logging.
 */
public final class AttackLogWriter {

	/** The <code>java.util.logging.Logger</code> instance. */
	private static Logger logger = Logger.getLogger("attackerLog"); //$NON-NLS-1$
	
	/** Path of log */
	private static String logPath = "";
	
	
	/** Old path of log */
	private static String logOldPath = "";
	
	/** file handler */
	private static FileHandler handler = null;

	/**
	 * Sets the parameters for the static class.
	 *
	 * @param dir		the directory where the error log files are located
	 * @param format	the format of the log files (<code>txt</code> or <code>xml</code>)
	 */
	public static void setParameters(String dir, String format) {
		logger.setLevel(Level.FINEST);
		logPath = dir;
		
		java.util.Date dt = new java.util.Date();
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss"); //$NON-NLS-1$
		try {
			if(!dir.equals(logOldPath)){
				if(handler != null)logger.removeHandler(handler);
				handler = new FileHandler(dir + "log_" + df.format(dt) + "." + format, true);//$NON-NLS-1$ //$NON-NLS-2$
				logOldPath = dir;
				logger.setUseParentHandlers(false); // don't log to console
				logger.addHandler(handler);
				if (format.equals("txt")) //$NON-NLS-1$
					handler.setFormatter(new LogFormatter());
				else
					handler.setFormatter(new XMLFormatter());
				}
		} catch (Exception e) {
			ErrorLog.log(Messages.getString("ErrorLog.whileSetting"), 7, ErrorLog.class.getName(), "setParameters",  e); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit(1);
		}
	}

	/**
	 * Logs the attacker data.
	 *
	 * @param message	data to log
	 */
	public static synchronized void log(String message) {
		try {
			logger.log(Level.FINEST, message);
		} catch (Exception new_e) {
			System.out.println(Messages.getString("ErrorLog.whileLogging") + message + ")! " + new_e.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			new_e.printStackTrace();
		}
	}
	
	
	public static void setLogPath(String logPath) {
		setParameters(logPath + "/", "txt");
		AttackLogWriter.logPath = logPath;
	}

	public static String getLogPath() {
		return logPath;
	}
}