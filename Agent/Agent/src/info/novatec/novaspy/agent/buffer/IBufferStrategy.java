package info.novatec.novaspy.agent.buffer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A BufferStrategy is used to define the behavior of the value objects once a
 * connection problem appears.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IBufferStrategy extends Iterator {

	/**
	 * Adds a list of measurements.
	 * 
	 * @param measurements
	 *            The measurements to add.
	 */
	void addMeasurements(List measurements);

	/**
	 * Initializes the buffer strategy with the given {@link Map}.
	 * 
	 * @param settings
	 *            The settings as a {@link Map}.
	 */
	void init(Map settings);

}
