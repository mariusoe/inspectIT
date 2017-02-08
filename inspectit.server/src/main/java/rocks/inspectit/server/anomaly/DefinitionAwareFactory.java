package rocks.inspectit.server.anomaly;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.baseline.impl.ExponentialMovingAverageBaseline;
import rocks.inspectit.server.anomaly.baseline.impl.HistoricalBaseline;
import rocks.inspectit.server.anomaly.baseline.impl.HoltWintersBaseline;
import rocks.inspectit.server.anomaly.baseline.impl.MovingAverageBaseline;
import rocks.inspectit.server.anomaly.baseline.impl.NonBaseline;
import rocks.inspectit.server.anomaly.classification.AbstractClassifier;
import rocks.inspectit.server.anomaly.classification.impl.HardClassifier;
import rocks.inspectit.server.anomaly.classification.impl.PercentageClassifier;
import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
import rocks.inspectit.server.anomaly.metric.impl.InfluxDBMetricProvider;
import rocks.inspectit.server.anomaly.notifier.AbstractNotifier;
import rocks.inspectit.server.anomaly.notifier.impl.LogNotifier;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.server.anomaly.threshold.impl.FixedThreshold;
import rocks.inspectit.server.anomaly.threshold.impl.PercentageDerivationThreshold;
import rocks.inspectit.server.anomaly.threshold.impl.PercentileThreshold;
import rocks.inspectit.server.anomaly.threshold.impl.StandardDeviationThreshold;
import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.ExponentialMovingAverageBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.HistoricalBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.HoltWintersBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.MovingAverageBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.NonBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.classification.HardClassifierDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.classification.PercentageClassifierDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.metric.InfluxDBMetricDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.notification.LogNotificationDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.FixedThresholdDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.PercentageDerivationThresholdDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.PercentileThresholdDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.StandardDeviationThresholdDefinition;

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
		} else if (definition instanceof NonBaselineDefinition) {
			return create(NonBaseline.class, definition);
		} else if (definition instanceof HoltWintersBaselineDefinition) {
			return create(HoltWintersBaseline.class, definition);
		} else if (definition instanceof HistoricalBaselineDefinition) {
			return create(HistoricalBaseline.class, definition);
		} else {
			throw new UnsupportedBaselineException(definition.getClass());
		}
	}

	public AbstractThreshold<?> createThreshold(AbstractDefinition definition) {
		if (definition instanceof StandardDeviationThresholdDefinition) {
			return create(StandardDeviationThreshold.class, definition);
		} else if (definition instanceof PercentileThresholdDefinition) {
			return create(PercentileThreshold.class, definition);
		} else if (definition instanceof FixedThresholdDefinition) {
			return create(FixedThreshold.class, definition);
		} else if (definition instanceof PercentageDerivationThresholdDefinition) {
			return create(PercentageDerivationThreshold.class, definition);
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

	public AbstractClassifier<?> createClassifier(AbstractDefinition definition) {
		if (definition instanceof PercentageClassifierDefinition) {
			return create(PercentageClassifier.class, definition);
		} else if (definition instanceof HardClassifierDefinition) {
			return create(HardClassifier.class, definition);
		} else {
			throw new UnsupportedClassifierException(definition.getClass());
		}
	}

	public AbstractNotifier<?> createNotifier(AbstractDefinition definition) {
		if (definition instanceof LogNotificationDefinition) {
			return create(LogNotifier.class, definition);
		} else {
			throw new UnsupportedNotifierException(definition.getClass());
		}
	}

	public static class UnsupportedNotifierException extends RuntimeException {
		/**
		 * Generated UUID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param message
		 */
		public UnsupportedNotifierException(Class<?> clazz) {
			super("Unsupported notifier for definition of type '" + clazz.getSimpleName() + "'.");
		}
	}

	public static class UnsupportedClassifierException extends RuntimeException {
		/**
		 * Generated UUID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param message
		 */
		public UnsupportedClassifierException(Class<?> clazz) {
			super("Unsupported classifier for definition of type '" + clazz.getSimpleName() + "'.");
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
