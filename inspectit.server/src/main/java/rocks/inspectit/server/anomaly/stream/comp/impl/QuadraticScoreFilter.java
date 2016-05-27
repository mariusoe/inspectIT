/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.impl;

import rocks.inspectit.server.anomaly.stream.comp.AbstractResultProcessor;
import rocks.inspectit.server.anomaly.stream.comp.AbstractStreamProcessor;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class QuadraticScoreFilter extends AbstractStreamProcessor<InvocationSequenceData> {

	/**
	 * The result processor.
	 */
	private final AbstractResultProcessor<InvocationSequenceData> resultProcessor;

	/**
	 * @param resultProcessor
	 */
	public QuadraticScoreFilter(AbstractResultProcessor<InvocationSequenceData> resultProcessor) {
		this.resultProcessor = resultProcessor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processImpl(InvocationSequenceData item) {
		// TODO Auto-generated method stub

	}

}
