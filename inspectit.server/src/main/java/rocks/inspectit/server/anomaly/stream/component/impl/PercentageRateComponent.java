/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.influxdb.dto.Point;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractDoubleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class PercentageRateComponent extends AbstractDoubleStreamComponent<InvocationSequenceData> implements Runnable {

	private final AtomicLong totalCounter = new AtomicLong(0);
	private final AtomicLong errorCounter = new AtomicLong(0);

	/**
	 * @param nextComponentOne
	 * @param nextComponentTwo
	 * @param executorService
	 */
	public PercentageRateComponent(ISingleInputComponent<InvocationSequenceData> nextComponentOne, ISingleInputComponent<InvocationSequenceData> nextComponentTwo,
			ScheduledExecutorService executorService) {
		super(nextComponentOne, nextComponentTwo);

		executorService.scheduleAtFixedRate(this, 5000, 5000, TimeUnit.MILLISECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processOneImpl(InvocationSequenceData item) {
		totalCounter.incrementAndGet();

		return EFlowControl.CONTINUE_ONE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processOneTwo(InvocationSequenceData item) {
		totalCounter.incrementAndGet();
		errorCounter.incrementAndGet();

		return EFlowControl.CONTINUE_TWO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		long errorCount = errorCounter.getAndSet(0);
		long totalCount = totalCounter.getAndSet(0);

		double rate = 0;
		if (totalCount > 0) {
			rate = 1D / totalCount * errorCount;
		}
		SharedStreamProperties.getInfluxService().insert(Point.measurement("status").addField("errorRate", rate).build());
	}

}
