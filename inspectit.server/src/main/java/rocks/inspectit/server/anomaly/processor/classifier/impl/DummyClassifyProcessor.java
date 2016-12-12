package rocks.inspectit.server.anomaly.processor.classifier.impl;

import org.apache.commons.lang.NotImplementedException;

import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.data.HealthStatus;
import rocks.inspectit.server.anomaly.processor.classifier.AbstractClassifyProcessor;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.classify.DummyClassifyProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
public class DummyClassifyProcessor extends AbstractClassifyProcessor<DummyClassifyProcessorConfiguration> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void classify(AnomalyContext context, AnalyzableData<?> analyzable) {
		if (analyzable.getValue() < configuration.getThreshold()) {
			analyzable.setHealthStatus(HealthStatus.GOOD);
		} else {
			analyzable.setHealthStatus(HealthStatus.CRITICAL);
		}

		System.out.println("classified " + analyzable.getValue() + " as " + analyzable.getHealthStatus());
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
