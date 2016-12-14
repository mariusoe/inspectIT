package rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.analyze;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyProcessors;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractAnalyzeProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dummy-analyze-processor-configuration")
public class DummyAnalyzeProcessorConfiguration extends AbstractAnalyzeProcessorConfiguration {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnomalyProcessors getAnomalyProcessor() {
		return AnomalyProcessors.ANALYZER_DUMMY;
	}

}
