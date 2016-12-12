package rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.classify;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyProcessors;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractClassifyProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
public class DummyClassifyProcessorConfiguration extends AbstractClassifyProcessorConfiguration {

	private double threshold;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnomalyProcessors getAnomalyProcessor() {
		return AnomalyProcessors.CLASSIFIER_DUMMY;
	}

	/**
	 * Gets {@link #threshold}.
	 *
	 * @return {@link #threshold}
	 */
	public double getThreshold() {
		return this.threshold;
	}

	/**
	 * Sets {@link #threshold}.
	 *
	 * @param threshold
	 *            New value for {@link #threshold}
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

}
