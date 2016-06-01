/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class InvocationSequenceFilterComponent extends AbstractSingleStreamComponent<InvocationSequenceData> {

	/**
	 * @param nextComponent
	 */
	public InvocationSequenceFilterComponent(ISingleInputComponent<InvocationSequenceData> nextComponent) {
		super(nextComponent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(InvocationSequenceData item) {
		if (item.getDuration() <= 1) {
			return EFlowControl.BREAK;
		} else {
			return EFlowControl.CONTINUE;
		}
	}

}
