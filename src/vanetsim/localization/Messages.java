package vanetsim.localization;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import vanetsim.debug.Debug;

/**
 * Helper class for internationalization support.
 */
public final class Messages {
	
	/** The <code>ResourceBundle</code> where to get the messages from. */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("vanetsim.localization.messages");	//$NON-NLS-1$

	/**
	 * Instantiates a new instance.
	 */
	private Messages() {
		Debug.whereru(this.getClass().getName(), true);
		Debug.callFunctionInfo(this.getClass().getName(), "Messages()", true);
	}

	/**
	 * Returns a localized string.
	 * 
	 * @param key	key to find the string in the language file
	 * 
	 * @return the localized string
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
