package rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher;

import java.io.Serializable;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyContextMatcher;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractContextMatcherConfiguration implements Serializable {

	public abstract AnomalyContextMatcher getContextMatcher();

}
