package rocks.inspectit.shared.cs.ci.anomaly.configuration.processor;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.analyze.DummyAnalyzeProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
@XmlSeeAlso(DummyAnalyzeProcessorConfiguration.class)
@XmlRootElement(name = "analyze-processor-configuration")
public abstract class AbstractAnalyzeProcessorConfiguration extends AbstractProcessorConfiguration {

}
