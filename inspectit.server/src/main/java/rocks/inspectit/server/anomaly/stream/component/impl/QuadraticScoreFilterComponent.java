/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.concurrent.atomic.AtomicLong;

import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.anomaly.stream.ConfidenceBand;
import rocks.inspectit.server.anomaly.stream.component.AbstractForkStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.object.StreamContext;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.server.anomaly.stream.transfer.ITransferFunction;
import rocks.inspectit.server.anomaly.stream.transfer.TransferFunction;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class QuadraticScoreFilterComponent extends AbstractForkStreamComponent<InvocationSequenceData> {

	private final ITransferFunction transferFunction = TransferFunction.QUADRATIC;

	@Value("${anomaly.settings.scoreThreshold}")
	private double scoreThreshold;

	@Value("${anomaly.settings.warmupCount}")
	private long warmupCount;

	private final AtomicLong counter = new AtomicLong(0L);

	@Autowired
	private InfluxDBService influxService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> streamObject) {
		StreamContext context = streamObject.getContext();

		if (context.getRequestCount() < warmupCount) {
			return EFlowControl.CONTINUE_ONE;
		}

		ConfidenceBand confidenceBand = context.getConfidenceBand();
		if (confidenceBand == null) {
			return EFlowControl.CONTINUE_ONE;
		}

		if (confidenceBand.isInside(streamObject.getData().getDuration())) {
			return EFlowControl.CONTINUE_ONE;
		}

		double confidenceSize = context.getConfidenceBand().getWidth() / 2;
		if (confidenceSize <= 0) {
			return EFlowControl.CONTINUE_ONE;
		}

		double percentageError = confidenceBand.distanceToBand(streamObject.getData().getDuration()) / confidenceSize;

		double score = transferFunction.transfer(percentageError);

		influxService.insert(Point.measurement("status").addField("pScore", score).build());

		if (score <= scoreThreshold) {
			return EFlowControl.CONTINUE_ONE;
		} else {
			return EFlowControl.CONTINUE_TWO;
		}
	}

}
