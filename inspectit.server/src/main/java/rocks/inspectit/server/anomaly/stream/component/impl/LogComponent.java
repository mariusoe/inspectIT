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
public class LogComponent extends AbstractSingleStreamComponent<InvocationSequenceData> {

	/**
	 * @param nextComponent
	 */
	public LogComponent(ISingleInputComponent<InvocationSequenceData> nextComponent) {
		super(nextComponent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(InvocationSequenceData item) {
		System.out.println(item);

		return EFlowControl.CONTINUE;
	}

}
