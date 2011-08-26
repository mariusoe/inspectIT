package info.novatec.inspectit.communication.data;

import java.sql.Timestamp;

/**
 * Sub-class of TimerData that has better performance when aggregating values from other TimerData
 * objects. This class is only ment to be used for purpose of aggregation of objects that will be
 * persisted in the database.
 * 
 * @author Ivan Senic
 * 
 */
public class DatabaseAggregatedTimerData extends TimerData {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 3139731190115609664L;

	/**
	 * Default no-args constructor.
	 */
	public DatabaseAggregatedTimerData() {
		super();
	}

	public DatabaseAggregatedTimerData(Timestamp timestamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timestamp, platformIdent, sensorTypeIdent, methodIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void aggregateTimerData(TimerData timerData) {
		this.setCount(this.getCount() + timerData.getCount());
		this.setDuration(this.getDuration() + timerData.getDuration());
		this.calculateMax(timerData.getMax());
		this.calculateMin(timerData.getMin());

		if (timerData.isCpuMetricDataAvailable()) {
			this.setCpuDuration(this.getCpuDuration() + timerData.getCpuDuration());
			this.calculateCpuMin(timerData.getCpuMin());
			this.calculateCpuMax(timerData.getCpuMax());
		}

		if (timerData.isExclusiveTimeDataAvailable()) {
			this.addExclusiveDuration(timerData.getExclusiveDuration());
			this.setExclusiveCount(this.getExclusiveCount() + timerData.getExclusiveCount());
			this.calculateExclusiveMin(timerData.getExclusiveMin());
			this.calculateExclusiveMax(timerData.getExclusiveMax());
		}
	}
}
