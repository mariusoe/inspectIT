/**
 *
 */
package rocks.inspectit.server.anomaly.utils.processor;

/**
 * Interface for statistic processors.
 *
 * @author Marius Oehler
 *
 */
public interface IStatisticProcessor {

	/**
	 * Pushes a new value into the processor.
	 *
	 * @param time
	 *            the time related to the value
	 * @param value
	 *            the new value
	 */
	void push(long time, double value);

	/**
	 * Returns the current value of the processor.
	 *
	 * @return the current value
	 */
	double getValue();
}
