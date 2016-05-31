/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class ItemRateComponent extends AbstractSingleStreamComponent<InvocationSequenceData> implements Runnable {

	private final AtomicLong counter = new AtomicLong(0L);

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
	protected EFlowControl processImpl(InvocationSequenceData item) {
		counter.incrementAndGet();

		return EFlowControl.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		double rate = counter.get() / (interval / 1000D);
		counter.set(0);

		System.out.println(prefix + ": " + rate + " items/second");
	}

}
