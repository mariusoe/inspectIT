/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import org.influxdb.dto.Point;

import rocks.inspectit.server.anomaly.stream.ConfidenceBand;
import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractForkStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.IDoubleInputComponent;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.transfer.ITransferFunction;
import rocks.inspectit.server.anomaly.stream.transfer.TransferFunction;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class QuadraticScoreFilterComponent extends AbstractForkStreamComponent<InvocationSequenceData> {

	private final ITransferFunction transferFunction = TransferFunction.QUADRATIC;

	/**
	 * @param nextComponentOne
	 * @param nextComponentTwo
	 */
	public QuadraticScoreFilterComponent(ISingleInputComponent<InvocationSequenceData> nextComponentOne, ISingleInputComponent<InvocationSequenceData> nextComponentTwo) {
		super(nextComponentOne, nextComponentTwo);
	}

	/**
	 * @param nextComponent
	 */
	public QuadraticScoreFilterComponent(IDoubleInputComponent<InvocationSequenceData> nextComponent) {
		super(nextComponent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(InvocationSequenceData item) {
		ConfidenceBand confidenceBand = SharedStreamProperties.getConfidenceBand();
		if (confidenceBand == null) {
			return EFlowControl.CONTINUE_ONE;
		}

		if (confidenceBand.isInside(item.getDuration())) {
			return EFlowControl.CONTINUE_ONE;
		}

		double confidenceSize = SharedStreamProperties.getConfidenceBand().getWidth() / 2;
		if (confidenceSize <= 0) {
			return EFlowControl.CONTINUE_ONE;
		}

		double percentageError = confidenceBand.distanceToBand(item.getDuration()) / confidenceSize;

		double score = transferFunction.transfer(percentageError);

		SharedStreamProperties.getInfluxService().insert(Point.measurement("status").addField("pScore", score).build());

		if (score < .3D) {
			return EFlowControl.CONTINUE_ONE;
		} else {
			return EFlowControl.CONTINUE_TWO;
		}
	}

}
