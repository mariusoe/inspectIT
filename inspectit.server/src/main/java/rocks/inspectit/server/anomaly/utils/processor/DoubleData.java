/**
 *
 */
package rocks.inspectit.server.anomaly.utils.processor;

/**
 * @author Marius Oehler
 *
 */
public class DoubleData {

	private double data;

	private long time;

	/**
	 * @param data
	 * @param time
	 */
	public DoubleData(double data, long time) {
		super();
		this.data = data;
		this.time = time;
	}

	/**
	 * Gets {@link #data}.
	 *
	 * @return {@link #data}
	 */
	public double getData() {
		return data;
	}

	/**
	 * Sets {@link #data}.
	 *
	 * @param data
	 *            New value for {@link #data}
	 */
	protected void setData(double data) {
		this.data = data;
	}

	/**
	 * Gets {@link #time}.
	 *
	 * @return {@link #time}
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Sets {@link #time}.
	 *
	 * @param time
	 *            New value for {@link #time}
	 */
	protected void setTime(long time) {
		this.time = time;
	}

}
