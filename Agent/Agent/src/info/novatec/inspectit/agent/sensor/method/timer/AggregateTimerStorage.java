package info.novatec.inspectit.agent.sensor.method.timer;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.valueobject.TimerRawVO;

import java.sql.Timestamp;
import java.util.List;

/**
 * This timer storage just stores the passed value in the a
 * {@link PlainTimerValueObject}, without computing anything. When
 * {@link #finalizeValueObject()} is called, it computes all the values (min,
 * max, variance etc.) and stores it in a {@link VarianceTimerValueObject}.
 * 
 * @author Patrice Bouillet
 * 
 */
public class AggregateTimerStorage implements ITimerStorage {

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
	public AggregateTimerStorage(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, List parameterContentData) {
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
		return timerRawVO.finalizeData();
	}

}
