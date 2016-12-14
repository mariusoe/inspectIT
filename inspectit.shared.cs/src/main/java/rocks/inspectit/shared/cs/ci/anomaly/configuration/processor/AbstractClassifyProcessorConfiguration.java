package rocks.inspectit.shared.cs.ci.anomaly.configuration.processor;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.classify.DummyClassifyProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
@XmlSeeAlso(DummyClassifyProcessorConfiguration.class)
@XmlRootElement(name = "classify-processor-configuration")
public abstract class AbstractClassifyProcessorConfiguration extends AbstractProcessorConfiguration {

}
