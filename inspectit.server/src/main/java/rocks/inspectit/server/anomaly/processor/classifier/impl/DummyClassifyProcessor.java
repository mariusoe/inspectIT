package rocks.inspectit.server.anomaly.processor.classifier.impl;

import org.apache.commons.lang.NotImplementedException;

import rocks.inspectit.server.anomaly.configuration.model.IClassifyProcessorConfiguration;
import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.data.HealthStatus;
import rocks.inspectit.server.anomaly.processor.classifier.AbstractClassifyProcessor;

/**
 * @author Marius Oehler
 *
 */
public class DummyClassifyProcessor extends AbstractClassifyProcessor<DummyClassifyProcessor.Configuration> {

	public static class Configuration implements IClassifyProcessorConfiguration<DummyClassifyProcessor> {

		private double threshold;

		/**
		 * @param threshold
		 */
		public Configuration(double threshold) {
			this.threshold = threshold;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<DummyClassifyProcessor> getProcessorClass() {
			return DummyClassifyProcessor.class;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void classify(AnomalyContext context, AnalyzableData<?> analyzable) {
		if (analyzable.getValue() < configuration.threshold) {
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
