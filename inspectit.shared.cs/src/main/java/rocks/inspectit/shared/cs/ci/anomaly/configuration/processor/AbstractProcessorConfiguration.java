package rocks.inspectit.shared.cs.ci.anomaly.configuration.processor;

import java.io.Serializable;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyProcessors;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractProcessorConfiguration implements Serializable {

	public abstract AnomalyProcessors getAnomalyProcessor();

}
