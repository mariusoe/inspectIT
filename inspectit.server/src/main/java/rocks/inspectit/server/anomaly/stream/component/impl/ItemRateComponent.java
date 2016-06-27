/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.object.InvocationStreamObject;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class ItemRateComponent extends AbstractSingleStreamComponent<InvocationSequenceData> implements Runnable {

	private final Map<String, AtomicLong> counterMap = new HashMap<>();

	private final long interval = 5000L;

	private final String prefix;

	/**
	 * @param nextComponent
	 */
	public ItemRateComponent(ISingleInputComponent<InvocationSequenceData> nextComponent, String prefix) {
		super(nextComponent);
		this.prefix = prefix;

		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this, interval, interval, TimeUnit.MILLISECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> streamObject) {
		InvocationStreamObject invocationStreamObject = (InvocationStreamObject) streamObject;

		if (!counterMap.containsKey(invocationStreamObject.getBusinessTransaction())) {
			counterMap.put(invocationStreamObject.getBusinessTransaction(), new AtomicLong(0L));
		}

		counterMap.get(invocationStreamObject.getBusinessTransaction()).incrementAndGet();

		return EFlowControl.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		System.out.println(prefix);

		for (Entry<String, AtomicLong> entry : counterMap.entrySet()) {
			long count = entry.getValue().getAndSet(0L);

			double rate = count / (interval / 1000D);

			Builder builder = Point.measurement("status");
			builder.addField("requestRate", rate);

			SharedStreamProperties.getInfluxService().insert(builder.build());

			System.out.println("|-" + entry.getKey() + ": " + rate + " items/second");
		}
	}

}
