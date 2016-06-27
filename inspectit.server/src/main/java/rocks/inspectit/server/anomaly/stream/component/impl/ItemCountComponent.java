/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class ItemCountComponent extends AbstractSingleStreamComponent<InvocationSequenceData> {

	private long counter = 0;

	/**
	 * @param nextComponent
	 */
	public ItemCountComponent(ISingleInputComponent<InvocationSequenceData> nextComponent) {
		super(nextComponent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> item) {
		counter++;

		return EFlowControl.CONTINUE;
	}

	/**
	 * Gets {@link #counter}.
	 *
	 * @return {@link #counter}
	 */
	public long getCounter() {
		return counter;
	}

}
