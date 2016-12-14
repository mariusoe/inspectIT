package rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyContextMatcher;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.impl.BusinessTransactionMatcherConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.impl.MachineMatcherConfiguration;

/**
 * @author Marius Oehler
 *
 */
@XmlSeeAlso({ BusinessTransactionMatcherConfiguration.class, MachineMatcherConfiguration.class })
@XmlRootElement(name = "context-matcher-configuration")
public abstract class AbstractContextMatcherConfiguration implements Serializable {

	public abstract AnomalyContextMatcher getContextMatcher();

}
