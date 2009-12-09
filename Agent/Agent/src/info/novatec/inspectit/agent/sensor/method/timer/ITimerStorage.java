package info.novatec.inspectit.agent.sensor.method.timer;

import info.novatec.inspectit.agent.core.IObjectStorage;

/**
 * A {@link ITimerStorage} just accepts time data through
 * {@link #addData(double)}.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface ITimerStorage extends IObjectStorage {

	/**
	 * The only method, which is used to process the new time value.
	 * 
	 * @param time
	 *            The time value.
	 */
	void addData(double time);

}
