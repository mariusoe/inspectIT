/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractDoubleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.object.InvocationStreamObject;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class PercentageRateComponent extends AbstractDoubleStreamComponent<InvocationSequenceData> implements Runnable {

	private final Map<String, CounterPair> counterMap = new HashMap<>();

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
	protected EFlowControl processOneImpl(StreamObject<InvocationSequenceData> streamObject) {
		InvocationStreamObject invocationStreamObject = (InvocationStreamObject) streamObject;

		if (!counterMap.containsKey(invocationStreamObject.getBusinessTransaction())) {
			counterMap.put(invocationStreamObject.getBusinessTransaction(), new CounterPair());
		}

		counterMap.get(invocationStreamObject.getBusinessTransaction()).totalCounter.incrementAndGet();

		return EFlowControl.CONTINUE_ONE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processOneTwo(StreamObject<InvocationSequenceData> streamObject) {
		InvocationStreamObject invocationStreamObject = (InvocationStreamObject) streamObject;

		if (!counterMap.containsKey(invocationStreamObject.getBusinessTransaction())) {
			counterMap.put(invocationStreamObject.getBusinessTransaction(), new CounterPair());
		}

		counterMap.get(invocationStreamObject.getBusinessTransaction()).totalCounter.incrementAndGet();
		counterMap.get(invocationStreamObject.getBusinessTransaction()).errorCounter.incrementAndGet();

		return EFlowControl.CONTINUE_TWO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		for (String businessTransaction : counterMap.keySet()) {

			CounterPair counterPair = counterMap.get(businessTransaction);
			counterMap.put(businessTransaction, new CounterPair());

			double rate = 0;
			if (counterPair.totalCounter.get() > 0) {
				rate = 1D / counterPair.totalCounter.get() * counterPair.errorCounter.get();
			}

			Builder builder = Point.measurement("statistics");
			builder.addField("errorRate", rate);
			builder.tag("buisnessTransaction", businessTransaction);

			SharedStreamProperties.getInfluxService().insert(builder.build());
		}
	}

	class CounterPair {
		AtomicLong totalCounter = new AtomicLong(0L);
		AtomicLong errorCounter = new AtomicLong(0L);
	}
}
