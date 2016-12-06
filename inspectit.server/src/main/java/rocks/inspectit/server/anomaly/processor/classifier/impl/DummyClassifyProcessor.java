package rocks.inspectit.server.anomaly.processor.classifier.impl;

import org.apache.commons.lang.NotImplementedException;

import rocks.inspectit.server.anomaly.configuration.model.IClassifyProcessorConfiguration;
import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.data.HealthStatus;
import rocks.inspectit.server.anomaly.processor.classifier.IClassifyProcessor;

/**
 * @author Marius Oehler
 *
 */
public class DummyClassifyProcessor implements IClassifyProcessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setConfiguration(IClassifyProcessorConfiguration<?> configuration) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void classify(AnomalyContext context, AnalyzableData<?> analyzable) {
		if (analyzable.getValue() > 1) {
			analyzable.setHealthStatus(HealthStatus.GOOD);
		} else {
			analyzable.setHealthStatus(HealthStatus.CRITICAL);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] getBoundaries(AnomalyContext context) {
		// TODO
		throw new NotImplementedException("TODO");
	}

}
