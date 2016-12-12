package rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.analyze;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyProcessors;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractAnalyzeProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
public class DummyAnalyzeProcessorConfiguration extends AbstractAnalyzeProcessorConfiguration {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnomalyProcessors getAnomalyProcessor() {
		return AnomalyProcessors.ANALYZER_DUMMY;
	}

}
