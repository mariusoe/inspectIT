package rocks.inspectit.server.anomaly;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.baseline.impl.ExponentialMovingAverageBaseline;
import rocks.inspectit.server.anomaly.baseline.impl.MovingAverageBaseline;
import rocks.inspectit.server.anomaly.definition.AbstractDefinition;
import rocks.inspectit.server.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.server.anomaly.definition.baseline.ExponentialMovingAverageBaselineDefinition;
import rocks.inspectit.server.anomaly.definition.baseline.MovingAverageBaselineDefinition;
import rocks.inspectit.server.anomaly.definition.metric.InfluxDBMetricDefinition;
import rocks.inspectit.server.anomaly.definition.threshold.StandardDeviationThresholdDefinition;
import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
import rocks.inspectit.server.anomaly.metric.impl.InfluxDBMetricProvider;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.server.anomaly.threshold.impl.StandardDeviationThreshold;

/**
 * @author Marius Oehler
 *
 */
@Component
public class DefinitionAwareFactory {

	@Autowired
	BeanFactory beanFactory;

	@SuppressWarnings("unchecked")
	private <E extends AbstractDefinitionAware<?>> E create(Class<?> clazz, AbstractDefinition definition) {
		AbstractDefinitionAware<AbstractDefinition> definitionAware = (AbstractDefinitionAware<AbstractDefinition>) beanFactory.getBean(clazz);
		definitionAware.setDefinition(definition);
		return (E) definitionAware;
	}

	public AbstractBaseline<?> createBaseline(AbstractDefinition definition) {
		if (definition instanceof MovingAverageBaselineDefinition) {
			return create(MovingAverageBaseline.class, definition);
		} else if (definition instanceof ExponentialMovingAverageBaselineDefinition) {
			return create(ExponentialMovingAverageBaseline.class, definition);
		} else {
			throw new UnsupportedBaselineException(definition.getClass());
		}
	}

	public AbstractThreshold<?> createClassifier(AbstractDefinition definition) {
		if (definition instanceof StandardDeviationThresholdDefinition) {
			return create(StandardDeviationThreshold.class, definition);
		} else {
			throw new UnsupportedThresholdException(definition.getClass());
		}
	}

	public AbstractMetricProvider<?> createMetricProvider(AbstractDefinition definition) {
		if (definition instanceof InfluxDBMetricDefinition) {
			return create(InfluxDBMetricProvider.class, definition);
		} else {
			throw new UnsupportedMetricProviderException(definition.getClass());
		}
	}

	public static class UnsupportedBaselineException extends RuntimeException {
		/**
		 * Generated UUID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param message
		 */
		public UnsupportedBaselineException(Class<?> clazz) {
			super("Unsupported baseline for definition of type '" + clazz.getSimpleName() + "'.");
		}
	}

	public static class UnsupportedThresholdException extends RuntimeException {
		/**
		 * Generated UUID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param message
		 */
		public UnsupportedThresholdException(Class<?> clazz) {
			super("Unsupported threshold for definition of type '" + clazz.getSimpleName() + "'.");
		}
	}

	public static class UnsupportedMetricProviderException extends RuntimeException {

		/**
		 * Generated UUID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param message
		 */
		public UnsupportedMetricProviderException(Class<?> clazz) {
			super("Unsupported metric provider for definition of type '" + clazz.getSimpleName() + "'.");
		}

	}
}
