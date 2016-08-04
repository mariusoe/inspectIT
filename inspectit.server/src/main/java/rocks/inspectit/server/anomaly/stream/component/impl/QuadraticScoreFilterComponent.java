/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.anomaly.stream.component.AbstractForkStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.object.ConfidenceBand;
import rocks.inspectit.server.anomaly.stream.object.StreamContext;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.server.anomaly.stream.transfer.ITransferFunction;
import rocks.inspectit.server.anomaly.stream.transfer.TransferFunction;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
public class QuadraticScoreFilterComponent extends AbstractForkStreamComponent<InvocationSequenceData> {

	@Log
	private Logger log;

	private final ITransferFunction transferFunction = TransferFunction.QUADRATIC;

	@Value("${anomaly.settings.scoreThreshold}")
	private double scoreThreshold;

	@Value("${anomaly.settings.warmupDuration}")
	private long warmupDuration;

	@Autowired
	private InfluxDBService influxService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> streamObject) {
		StreamContext context = streamObject.getContext();

		if (context.isWarmingUp()) {
			long warmupEndTime = context.getStartTime() + warmupDuration * 1000L;
			if (warmupEndTime < System.currentTimeMillis()) {
				context.setWarmingUp(false);
				if (log.isInfoEnabled()) {
					log.info("[{}] Warm-up finished.", context.getBusinessTransaction());
				}
			}

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
