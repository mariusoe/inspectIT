/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.concurrent.atomic.AtomicLong;

import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class WarmUpFilterComponent extends AbstractSingleStreamComponent<InvocationSequenceData> {

	private final AtomicLong counter = new AtomicLong(0L);

	private final long warmupCount = 20;

	private boolean isWarmingUp = true;

	/**
	 * @param nextComponent
	 */
	public WarmUpFilterComponent(ISingleInputComponent<InvocationSequenceData> nextComponent) {
		super(nextComponent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> item) {
		if (isWarmingUp) {
			if (counter.incrementAndGet() > warmupCount) {
				isWarmingUp = false;
			}
			return EFlowControl.BREAK;
		} else {
			return EFlowControl.CONTINUE;
		}
	}

}
