package rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.baseline;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyProcessors;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractBaselineProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
public class DummyBaselineProcessorConfiguration extends AbstractBaselineProcessorConfiguration {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnomalyProcessors getAnomalyProcessor() {
		return AnomalyProcessors.BASELINE_DUMMY;
	}

}
