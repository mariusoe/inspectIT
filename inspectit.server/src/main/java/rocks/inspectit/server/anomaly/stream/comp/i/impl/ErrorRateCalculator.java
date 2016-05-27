/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.i.impl;

import org.influxdb.dto.Point;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.comp.i.AbstractDualStream;
import rocks.inspectit.server.anomaly.stream.comp.i.IDoubleInputStream;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class ErrorRateCalculator extends AbstractDualStream<InvocationSequenceData, InvocationSequenceData> {

	/**
	 * @param nextStream
	 */
	public ErrorRateCalculator(IDoubleInputStream<InvocationSequenceData> nextStream) {
		super(nextStream);
	}

	private double count = 0;
	private double errorCount = 0;

	private long lastCheck = 0;

	private final long interval = 5000;

	private void check(long currentTime) {
		double rate = errorCount / count;
		errorCount = 0;
		count = 0;
		lastCheck = currentTime;

		SharedStreamProperties.getInfluxService().insert(Point.measurement("status").addField("errorRate", rate).build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processA(InvocationSequenceData item) {
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
	public void processB(InvocationSequenceData item) {
		count++;
		errorCount++;

		long currentTime = System.currentTimeMillis();
		if (lastCheck + interval < currentTime) {
			check(currentTime);
		}
	}

}
