package info.novatec.inspectit.util;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides methods for string manipulation purposes as cropping a string to
 * a specified length.
 * 
 * @author Patrick Eschenbach
 */
public class StringConstraint {
	
	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(StringConstraint.class.getName());
	
	/**
	 * Boolean indicating whether to use three trailing dots or not.
	 */
	private static final boolean USE_TRAILING_DOTS = true;
	
	/**
	 * Global definition of the maximal string length.
	 */
	private static final int MAX_STRING_LENGTH = Integer.MAX_VALUE;
	
	/**
	 * The effective string length which is defined as the smaller one of the sensor's
	 * configuration or MAX_STRING_LENGTH.
	 */
	private int effectiveStringLength;
	
	/**
	 * The default constructor which needs one parameter for initialization.
	 * 
	 * @param parameter
	 *            Parameter map to extract the constrain information.
	 */
	public StringConstraint(Map parameter) {
		effectiveStringLength = MAX_STRING_LENGTH;
		
		String value = (String) parameter.get("stringLength");
		if (value != null) {
			try {
				int configStringLength = Integer.parseInt(value);
				
				// only use the given length if smaller than the max length and not smaller than 0
				if (configStringLength < MAX_STRING_LENGTH && configStringLength >= 0) {
					effectiveStringLength = configStringLength;
				}
			} catch (NumberFormatException e) {
				if (LOGGER.isLoggable(Level.WARNING)) {
					LOGGER.warning("Property 'stringLength' is not defined correctly. Using unlimited string length.");
				}
			}
		}
	}
	
	/**
	 * Crops the given string based on this instance's configuration. If the string is
	 * shorter than the specified string length the given string is not altered.
	 * 
	 * @param string
	 *            The string to crop.
	 * @return The cropped string.
	 */
	public String crop(String string) {
		if (null == string || string.length() <= effectiveStringLength) {
			return string;
		}
		
		if (effectiveStringLength == 0) {
			return "";
		}
		
		String cropped = string.substring(0, effectiveStringLength);
		if (USE_TRAILING_DOTS) {
			cropped = appendTrailingDots(cropped);
		}
		return cropped;
	}
	
	/**
	 * Crops the given string and adds the given final character to the string's end. If the
	 * string is shorter than the specified string length the given string is not altered.
	 * 
	 * @param string
	 *            The string to crop.
	 * @param finalChar
	 *            The character to append at the end.
	 * @return A cropped string ending with the given final character.
	 */
	public String cropKeepFinalCharacter(String string, char finalChar) {
		String cropped = crop(string);
		
		if (null == string || string.equals(cropped)) {
			return string;
		}
		
		if (cropped.length() == 0) {
			return cropped;
		}
		return cropped + finalChar;
	}
	
	/**
	 * Appends three dots to the given string to indicate that the string was cropped.
	 * 
	 * @param string
	 *            The string to append the dots to.
	 * @return A new string equal to the given one + three dots.
	 */
	private String appendTrailingDots(String string) {
		return string + "...";
	}
}
