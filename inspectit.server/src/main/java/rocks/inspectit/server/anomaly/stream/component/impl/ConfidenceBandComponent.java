/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.anomaly.stream.ConfidenceBand;
import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.object.StreamContext;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

public class ConfidenceBandComponent extends AbstractSingleStreamComponent<InvocationSequenceData> implements Runnable {

	/**
	 * {@link ExecutorService} for sending keep alive messages.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService executorService;

	@Autowired
	private InfluxDBService influxService;

	@Value("${anomaly.settings.confidenceBandUpdateInterval}")
	private long updateInterval;

	@Autowired
	private SharedStreamProperties streamProperties;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> item) {
		return EFlowControl.CONTINUE;
	}

	public void start() {
		executorService.scheduleAtFixedRate(this, updateInterval, updateInterval, TimeUnit.SECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		for (String businessTransaction : streamProperties.getBusinessTransactions()) {

			StreamContext context = streamProperties.getStreamContext(businessTransaction);

			double currentMean = context.getCurrentMean();
			if (!Double.isNaN(currentMean)) {
				double stdDeviation = context.getStandardDeviationStatistics().getStandardDeviation();

				double lowerConfidenceLevel = Math.max(0D, currentMean - 3 * stdDeviation);
				double upperConfidenceLevel = currentMean + 3 * stdDeviation;

				// store result in shared properties
				ConfidenceBand confidenceBand = new ConfidenceBand(currentMean, upperConfidenceLevel, lowerConfidenceLevel);
				context.setConfidenceBand(confidenceBand);

				// build influx point
				Builder builder = Point.measurement("statistics");
				builder.addField("mean", currentMean);
				builder.addField("lowerConfidenceLevel", lowerConfidenceLevel);
				builder.addField("upperConfidenceLevel", upperConfidenceLevel);
				builder.tag("businessTransaction", businessTransaction);

				influxService.insert(builder.build());
			}
		}
	}

}