package info.novatec.inspectit.cmr.anomaly.strategy;

import info.novatec.inspectit.cmr.influxdb.InfluxDbService;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAnomalyDetectionStrategy {

	/**
	 * Instance of the {@link InfluxDbService}.
	 */
	protected InfluxDbService influxDb;

	public AbstractAnomalyDetectionStrategy(InfluxDbService influxDb) {
		super();
		this.influxDb = influxDb;
	}

	public abstract void detect();
}
