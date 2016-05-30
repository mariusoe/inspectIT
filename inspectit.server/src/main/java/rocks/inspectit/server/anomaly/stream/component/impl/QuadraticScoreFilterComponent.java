/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractForkStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class QuadraticScoreFilterComponent extends AbstractForkStreamComponent<InvocationSequenceData> {

	/**
	 * {@inheritDoc}
	 */
	// @Override
	// public void process(InvocationSequenceData item) {
	// if (Double.isNaN(SharedStreamProperties.getConfidenceBandLower()) ||
	// Double.isNaN(SharedStreamProperties.getConfidenceBandUpper())) {
	// return;
	// }
	//
	// double upperThreshold = SharedStreamProperties.getUpperThreeSigmaThreshold();
	// double lowerThreshold = SharedStreamProperties.getLowerThreeSigmaThreshold();
	//
	// if (item.getDuration() > lowerThreshold && item.getDuration() < upperThreshold) {
	// nextA(item);
	// return;
	// }
	//
	// if (SharedStreamProperties.getStddev() != 0) {
	// double percentageError;
	// if (item.getDuration() > upperThreshold) {
	// percentageError = (item.getDuration() - upperThreshold) / upperThreshold;
	// } else {
	// percentageError = (item.getDuration() - lowerThreshold) / lowerThreshold;
	// }
	//
	// double score = percentageError * percentageError;
	//
	// SharedStreamProperties.getInfluxService().insert(Point.measurement("status").addField("pScore",
	// score).build());
	//
	// if (score < 0.3D) {
	// nextA(item);
	// } else {
	// nextB(item);
	// }
	// } else {
	// nextA(item);
	// }
	// }

	/**
	 * @param nextComponentOne
	 * @param nextComponentTwo
	 */
	public QuadraticScoreFilterComponent(ISingleInputComponent<InvocationSequenceData> nextComponentOne, ISingleInputComponent<InvocationSequenceData> nextComponentTwo) {
		super(nextComponentOne, nextComponentTwo);
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

		return null;
	}

}
