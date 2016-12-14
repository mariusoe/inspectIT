package rocks.inspectit.shared.cs.ci.anomaly.configuration.processor;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.baseline.DummyBaselineProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
@XmlSeeAlso(DummyBaselineProcessorConfiguration.class)
@XmlRootElement(name = "baseline-processor-configuration")
public abstract class AbstractBaselineProcessorConfiguration extends AbstractProcessorConfiguration{

}
