package rocks.inspectit.server.anomaly.processor.classifier.impl;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.context.model.ConfidenceBand;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.data.HealthStatus;
import rocks.inspectit.server.anomaly.processor.classifier.AbstractClassifyProcessor;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.classify.DummyClassifyProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class DummyClassifyProcessor extends AbstractClassifyProcessor<DummyClassifyProcessorConfiguration> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void classify(AnomalyContext context, AnalyzableData<?> analyzable) {
		ConfidenceBand confidenceBand = context.getConfidenceBand();
		if (confidenceBand == null) {
			analyzable.setHealthStatus(HealthStatus.UKNOWN);
			System.out.println("Dunno");
			return;
		}

		if (confidenceBand.isInside(analyzable.getValue())) {
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
