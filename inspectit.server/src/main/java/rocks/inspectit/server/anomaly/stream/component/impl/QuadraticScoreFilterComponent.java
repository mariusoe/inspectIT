/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import org.influxdb.dto.Point;

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
		if (Double.isNaN(SharedStreamProperties.getConfidenceBandLower()) || Double.isNaN(SharedStreamProperties.getConfidenceBandUpper())) {
			return EFlowControl.CONTINUE_ONE;
		}

		if (item.getDuration() > SharedStreamProperties.getConfidenceBandLower() && item.getDuration() < SharedStreamProperties.getConfidenceBandUpper()) {
			return EFlowControl.CONTINUE_ONE;
		}

		if (!Double.isNaN(SharedStreamProperties.getStandardDeviation())) {
			double percentageError;
			if (item.getDuration() > SharedStreamProperties.getConfidenceBandUpper()) {
				percentageError = (item.getDuration() - SharedStreamProperties.getConfidenceBandUpper()) / SharedStreamProperties.getConfidenceBandUpper();
			} else {
				percentageError = (item.getDuration() - SharedStreamProperties.getConfidenceBandLower()) / SharedStreamProperties.getConfidenceBandLower();
			}

			double score = transferFunction.transfer(percentageError);

			SharedStreamProperties.getInfluxService().insert(Point.measurement("status").addField("pScore", score).build());

			if (score < 0.3D) {
				return EFlowControl.CONTINUE_ONE;
			} else {
				return EFlowControl.CONTINUE_TWO;
			}
		} else {
			return EFlowControl.CONTINUE_ONE;
		}

	}

}
