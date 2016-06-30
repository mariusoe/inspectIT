/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.shared.all.util.Pair;

/**
 * @author Marius Oehler
 *
 */
public class BusinessTransactionAlertingComponent extends AbstractSingleStreamComponent<Pair<String, Double>> {

	private final Map<String, AnomalyInformation> anomalyMap;

	private final double alertThreshold = 0.5D;

	private final long alertDuration = TimeUnit.SECONDS.toMillis(30);

	private final long anomalyAlertInterval = TimeUnit.SECONDS.toMillis(60);

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");

	/**
	 * @param nextComponent
	 */
	public BusinessTransactionAlertingComponent(ISingleInputComponent<Pair<String, Double>> nextComponent) {
		super(nextComponent);

		anomalyMap = new HashMap<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<Pair<String, Double>> item) {
		String businessTransaction = item.getData().getFirst();
		double errorRate = item.getData().getSecond();

		long currentTime = System.currentTimeMillis();

		if (errorRate > alertThreshold) {
			if (!anomalyMap.containsKey(businessTransaction)) {
				anomalyMap.put(businessTransaction, new AnomalyInformation());
			} else {
				AnomalyInformation anomalyInformation = anomalyMap.get(businessTransaction);

				if (currentTime - anomalyInformation.startTime > alertDuration) {
					anomalyDetected(businessTransaction, errorRate);
				}
			}
		} else {
			if (anomalyMap.containsKey(businessTransaction)) {
				AnomalyInformation anomalyInformation = anomalyMap.get(businessTransaction);

				if (anomalyInformation.isActive) {
					anomalyEnded(businessTransaction);
				}

				anomalyMap.remove(businessTransaction);
			}
		}

		return EFlowControl.CONTINUE;
	}

	/**
	 * @param businessTransaction
	 */
	private void anomalyEnded(String businessTransaction) {
		StringBuilder builder = new StringBuilder();
		builder.append("Anomaly in business transaction '");
		builder.append(businessTransaction);
		builder.append("' has ended.");

		SharedStreamProperties.getAlertingComponent().sendMessage(builder.toString());
	}

	private void anomalyDetected(String businessTransaction, double errorRate) {
		AnomalyInformation anomalyInformation = anomalyMap.get(businessTransaction);

		long currentTime = System.currentTimeMillis();
		double errorRatePercentage = ((int) (errorRate * 10000)) / 100D;

		if (anomalyInformation.isActive) {
			if (currentTime - anomalyInformation.lastAlert > anomalyAlertInterval) {
				StringBuilder builder = new StringBuilder();
				builder.append("Anomaly in business transaction '");
				builder.append(businessTransaction);
				builder.append("' is still present. Current error rate is ");
				builder.append(errorRatePercentage);
				builder.append(" percent.");

				SharedStreamProperties.getAlertingComponent().sendMessage(builder.toString());

				anomalyInformation.lastAlert = currentTime;
			}
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append("Business transaction '");
			builder.append(businessTransaction);
			builder.append("' shows an anomalous behavior. Anomaly started at ");
			builder.append(dateFormat.format(anomalyInformation.startTime));
			builder.append(" at has an error rate of ");
			builder.append(errorRatePercentage);
			builder.append(" percent.");

			SharedStreamProperties.getAlertingComponent().sendMessage(builder.toString());

			anomalyInformation.isActive = true;
			anomalyInformation.lastAlert = currentTime;
		}
	}

	private class AnomalyInformation {
		long startTime = System.currentTimeMillis();

		boolean isActive = false;

		long lastAlert = 0L;
	}
}
