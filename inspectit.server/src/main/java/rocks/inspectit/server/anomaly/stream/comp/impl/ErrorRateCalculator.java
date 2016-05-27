/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.impl;

import org.influxdb.dto.Point;

import rocks.inspectit.server.anomaly.stream.comp.AbstractResultProcessor;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class ErrorRateCalculator extends AbstractResultProcessor<InvocationSequenceData> {

	private int count = 0;

	private long lastCheck = 0;

	private final long interval = 5000;

	private final InfluxDBService influx;

	/**
	 * @param influx
	 */
	public ErrorRateCalculator(InfluxDBService influx) {
		this.influx = influx;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void problemImpl(InvocationSequenceData item) {
		count++;

		long currentTime = System.currentTimeMillis();
		if (lastCheck + interval < currentTime) {
			check(currentTime);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void okayImpl(InvocationSequenceData item) {
		long currentTime = System.currentTimeMillis();
		if (lastCheck + interval < currentTime) {
			check(currentTime);
		}
	}

	private void check(long currentTime) {
		double rate = count / (interval / 1000D);
		count = 0;
		lastCheck = currentTime;

		influx.insert(Point.measurement("status").addField("errorRate", rate).build());
	}

}
