package rocks.inspectit.server.anomaly.processing;

import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.classification.AbstractClassifier;
import rocks.inspectit.server.anomaly.classification.HealthStatus;
import rocks.inspectit.server.anomaly.configuration.AnomalyDetectionConfiguration;
import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyProcessingContext {

	private AnomalyDetectionConfiguration configuration;

	private AbstractBaseline<?> baseline;

	private AbstractMetricProvider<?> metricProvider;

	private int iterationCounter = 0;

	private AbstractThreshold<?> threshold;

	private AbstractClassifier<?> classifier;

	private HealthStatus healthStatus = HealthStatus.UNKNOWN;

	/**
	 * Gets {@link #healthStatus}.
	 *
	 * @return {@link #healthStatus}
	 */
	public HealthStatus getHealthStatus() {
		return this.healthStatus;
	}

	/**
	 * Sets {@link #healthStatus}.
	 *
	 * @param healthStatus
	 *            New value for {@link #healthStatus}
	 */
	public void setHealthStatus(HealthStatus healthStatus) {
		this.healthStatus = healthStatus;
	}

	/**
	 * Gets {@link #classifier}.
	 *
	 * @return {@link #classifier}
	 */
	public AbstractClassifier<?> getClassifier() {
		return this.classifier;
	}

	/**
	 * Gets {@link #threshold}.
	 *
	 * @return {@link #threshold}
	 */
	public AbstractThreshold<?> getThreshold() {
		return this.threshold;
	}

	/**
	 * Gets {@link #configuration}.
	 *
	 * @return {@link #configuration}
	 */
	public AnomalyDetectionConfiguration getConfiguration() {
		return this.configuration;
	}

	/**
	 * Sets {@link #configuration}.
	 *
	 * @param configuration
	 *            New value for {@link #configuration}
	 */
	public void setConfiguration(AnomalyDetectionConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Gets {@link #baseline}.
	 *
	 * @return {@link #baseline}
	 */
	public AbstractBaseline<?> getBaseline() {
		return this.baseline;
	}

	/**
	 * Sets {@link #baseline}.
	 *
	 * @param baseline
	 *            New value for {@link #baseline}
	 */
	public void setBaseline(AbstractBaseline<?> baseline) {
		this.baseline = baseline;
	}

	/**
	 * Gets {@link #metricProvider}.
	 *
	 * @return {@link #metricProvider}
	 */
	public AbstractMetricProvider<?> getMetricProvider() {
		return this.metricProvider;
	}

	/**
	 * Sets {@link #metricProvider}.
	 *
	 * @param metricProvider
	 *            New value for {@link #metricProvider}
	 */
	public void setMetricProvider(AbstractMetricProvider<?> metricProvider) {
		this.metricProvider = metricProvider;
	}

	/**
	 * Gets {@link #iterationCounter}.
	 *
	 * @return {@link #iterationCounter}
	 */
	public int getIterationCounter() {
		return this.iterationCounter;
	}

	public void incrementInterationCounter() {
		iterationCounter++;
	}

	/**
	 * @param threshold
	 */
	public void setThreshold(AbstractThreshold<?> threshold) {
		this.threshold = threshold;
	}

	public boolean isWarmedUp() {
		return configuration.getWarmupLength() < iterationCounter;
	}

	/**
	 * @param classifier
	 */
	public void setClassifier(AbstractClassifier<?> classifier) {
		this.classifier = classifier;
	}

	// private double mean;
	//
	// private DescriptiveStatistics stats = new DescriptiveStatistics();
	//
	// private double standardDeviation;
	//
	// /**
	// * Gets {@link #standardDeviation}.
	// *
	// * @return {@link #standardDeviation}
	// */
	// public double getStandardDeviation() {
	// return this.standardDeviation;
	// }
	//
	// /**
	// * Sets {@link #standardDeviation}.
	// *
	// * @param standardDeviation
	// * New value for {@link #standardDeviation}
	// */
	// public void setStandardDeviation(double standardDeviation) {
	// this.standardDeviation = standardDeviation;
	// }
	//
	// /**
	// * Gets {@link #mean}.
	// *
	// * @return {@link #mean}
	// */
	// public double getMean() {
	// return this.mean;
	// }
	//
	// /**
	// * Sets {@link #mean}.
	// *
	// * @param mean
	// * New value for {@link #mean}
	// */
	// public void setMean(double mean) {
	// this.mean = mean;
	// }
	//
	// /**
	// * Gets {@link #stats}.
	// *
	// * @return {@link #stats}
	// */
	// public DescriptiveStatistics getStats() {
	// return this.stats;
	// }
	//
	// /**
	// * Sets {@link #stats}.
	// *
	// * @param stats
	// * New value for {@link #stats}
	// */
	// public void setStats(DescriptiveStatistics stats) {
	// this.stats = stats;
	// }

}
