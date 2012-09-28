package info.novatec.inspectit.indexing.aggregation.impl;

import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

import java.io.Serializable;

/**
 * {@link IAggregator} for {@link TimerData}.
 * 
 * @author Ivan Senic
 * 
 */
public class TimerDataAggregator implements IAggregator<TimerData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 8176969641431206899L;

	/**
	 * Is cloning active.
	 */
	private boolean cloning;

	/**
	 * No-arg constructor.
	 */
	public TimerDataAggregator() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param cloning
	 *            Should cloning be used or not.
	 */
	public TimerDataAggregator(boolean cloning) {
		this.cloning = cloning;
	}

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(TimerData aggregatedObject, TimerData objectToAdd) {
		aggregatedObject.aggregateTimerData(objectToAdd);
	}

	/**
	 * {@inheritDoc}
	 */
	public TimerData getClone(TimerData timerData) {
		TimerData clone = new TimerData();
		clone.setPlatformIdent(timerData.getPlatformIdent());
		clone.setSensorTypeIdent(timerData.getSensorTypeIdent());
		clone.setMethodIdent(timerData.getMethodIdent());
		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCloning() {
		return cloning;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAggregationKey(TimerData object) {
		return object.getMethodIdent();
	}

}
