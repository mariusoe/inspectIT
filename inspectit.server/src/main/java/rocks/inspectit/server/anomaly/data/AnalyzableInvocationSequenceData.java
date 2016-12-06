package rocks.inspectit.server.anomaly.data;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class AnalyzableInvocationSequenceData extends AnalyzableData<InvocationSequenceData> {

	/**
	 * @param invocationSequenceData
	 */
	public AnalyzableInvocationSequenceData(InvocationSequenceData invocationSequenceData) {
		super(invocationSequenceData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getValue() {
		return data.getDuration();
	}
}
