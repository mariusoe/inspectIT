/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

/**
 * @author Marius Oehler
 *
 */
public class SharedStreamProperties {

	private static double threeSigmaThreshold;

	private SharedStreamProperties() {
	}

	/**
	 * Gets {@link #threeSigmaThreshold}.
	 * 
	 * @return {@link #threeSigmaThreshold}
	 */
	public static double getThreeSigmaThreshold() {
		return threeSigmaThreshold;
	}

	/**
	 * Sets {@link #threeSigmaThreshold}.
	 * 
	 * @param threeSigmaThreshold
	 *            New value for {@link #threeSigmaThreshold}
	 */
	public static void setThreeSigmaThreshold(double threeSigmaThreshold) {
		SharedStreamProperties.threeSigmaThreshold = threeSigmaThreshold;
	}

}
