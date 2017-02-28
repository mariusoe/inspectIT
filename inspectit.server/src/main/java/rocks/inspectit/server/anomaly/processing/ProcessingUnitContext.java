package rocks.inspectit.server.anomaly.processing;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.classification.AbstractClassifier;
import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionConfiguration;

/**
 * @author Marius Oehler
 *
 */
public class ProcessingUnitContext {

	private AnomalyDetectionConfiguration configuration;

	private AbstractBaseline<?> baseline;

	private AbstractMetricProvider<?> metricProvider;

	private int iterationCounter = 0;

	private AbstractThreshold<?> threshold;

	private AbstractClassifier<?> classifier;

	private HealthStatus healthStatus = HealthStatus.UNKNOWN;

	private ProcessingUnitGroupContext processingGroupContext;

	/**
	 * Gets {@link #processingGroupContext}.
	 * 
	 * @return {@link #processingGroupContext}
	 */
	public ProcessingUnitGroupContext getGroupContext() {
		return this.processingGroupContext;
	}

	/**
	 * Sets {@link #processingGroupContext}.
	 * 
	 * @param groupContext
	 *            New value for {@link #processingGroupContext}
	 */
	public void setGroupContext(ProcessingUnitGroupContext groupContext) {
		this.processingGroupContext = groupContext;
	}

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
}
