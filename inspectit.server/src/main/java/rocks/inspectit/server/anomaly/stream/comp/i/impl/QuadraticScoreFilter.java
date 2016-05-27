/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.i.impl;

import org.influxdb.dto.Point;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.comp.i.AbstractForkStream;
import rocks.inspectit.server.anomaly.stream.comp.i.IDoubleInputStream;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class QuadraticScoreFilter extends AbstractForkStream<InvocationSequenceData, InvocationSequenceData> {

	/**
	 * @param nextProcessorA
	 * @param nextProcessorB
	 */
	public QuadraticScoreFilter(IDoubleInputStream<InvocationSequenceData> nextStream) {
		super(nextStream);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(InvocationSequenceData item) {
		if (!SharedStreamProperties.isBaselineAvailable()) {
			nextA(item);
			return;
		}

		double upperThreshold = SharedStreamProperties.getUpperThreeSigmaThreshold();
		double lowerThreshold = SharedStreamProperties.getLowerThreeSigmaThreshold();

		if (item.getDuration() > lowerThreshold && item.getDuration() < upperThreshold) {
			nextA(item);
			return;
		}

		if (SharedStreamProperties.getStddev() != 0) {
			double percentageError;
			if (item.getDuration() > upperThreshold) {
				percentageError = (item.getDuration() - upperThreshold) / upperThreshold;
			} else {
				percentageError = (item.getDuration() - lowerThreshold) / lowerThreshold;
			}

			double score = percentageError * percentageError;

			SharedStreamProperties.getInfluxService().insert(Point.measurement("status").addField("pScore", score).build());

			if (score < 0.3D) {
				nextA(item);
			} else {
				nextB(item);
			}
		} else {
			nextA(item);
		}
	}

}
