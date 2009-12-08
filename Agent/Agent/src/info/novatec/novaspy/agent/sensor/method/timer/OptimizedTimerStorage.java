package info.novatec.novaspy.agent.sensor.method.timer;

import info.novatec.novaspy.communication.DefaultData;
import info.novatec.novaspy.communication.data.TimerData;

import java.sql.Timestamp;
import java.util.List;

/**
 * The optimized timer storage instantly computes the new values and saves them
 * in the {@link TimerData}.
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
	public OptimizedTimerStorage(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, List parameterContentData) {
		timerData = new TimerData(timeStamp, platformIdent, sensorTypeIdent, methodIdent, parameterContentData);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addData(double time) {
		timerData.increaseCount();
		timerData.addDuration(time);

		if (time < timerData.getMin()) {
			timerData.setMin(time);
		}

		if (time > timerData.getMax()) {
			timerData.setMax(time);
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
