package info.novatec.inspectit.agent.sensor.method.timer;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;

import java.sql.Timestamp;
import java.util.List;

/**
 * The optimized timer storage instantly computes the new values and saves them in the
 * {@link TimerData}.
 * 
 * @author Patrice Bouillet
 * 
 */
public class OptimizedTimerStorage implements ITimerStorage {

	/**
	 * The used {@link TimerData}.
	 */
	private TimerData timerData;

	/**
	 * Default constructor which initializes a {@link TimerData} object.
	 * 
	 * @param timeStamp
	 *            The time stamp.
	 * @param platformIdent
	 *            The platform ID.
	 * @param sensorTypeIdent
	 *            The sensor type ID.
	 * @param methodIdent
	 *            The method ID.
	 * @param parameterContentData
	 *            The content of the parameter/fields.
	 */
	public OptimizedTimerStorage(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, List<ParameterContentData> parameterContentData) {
		timerData = new TimerData(timeStamp, platformIdent, sensorTypeIdent, methodIdent, parameterContentData);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addData(double time, double cpuTime) {
		timerData.increaseCount();
		timerData.addDuration(time);

		timerData.calculateMax(time);
		timerData.calculateMin(time);

		// only add the cpu time if it greater than zero
		if (cpuTime >= 0) {
			timerData.addCpuDuration(cpuTime);

			timerData.calculateCpuMax(cpuTime);
			timerData.calculateCpuMin(cpuTime);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public DefaultData finalizeDataObject() {
		// processing is done during data adding, so nothing to do here.
		return timerData;
	}

}
