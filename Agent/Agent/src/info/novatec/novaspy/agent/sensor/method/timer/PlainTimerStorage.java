package info.novatec.novaspy.agent.sensor.method.timer;

import info.novatec.novaspy.communication.DefaultData;
import info.novatec.novaspy.communication.valueobject.TimerRawVO;

import java.sql.Timestamp;
import java.util.List;

/**
 * Class which stores the data as they arrive without further processing. This
 * will increase memory usage by a high amount but should reduces CPU usage.
 * 
 * @author Patrice Bouillet
 * 
 */
public class PlainTimerStorage implements ITimerStorage {

	/**
	 * The raw value object.
	 */
	private TimerRawVO timerRawVO;

	/**
	 * Default constructor which initializes a {@link TimerRawVO} object.
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
	public PlainTimerStorage(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, List parameterContentData) {
		timerRawVO = new TimerRawVO(timeStamp, platformIdent, sensorTypeIdent, methodIdent, parameterContentData);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addData(double time) {
		timerRawVO.add(time);
	}

	/**
	 * {@inheritDoc}
	 */
	public DefaultData finalizeDataObject() {
		return timerRawVO;
	}

}
