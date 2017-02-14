package rocks.inspectit.server.anomaly.classification;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.classification.stddev.StandardDeviationClassifier;
import rocks.inspectit.server.anomaly.classification.stddev.StandardDeviationClassifierDefinition;
import rocks.inspectit.server.anomaly.metric.MetricProviderFactory.UnsupportedMetricProviderException;

/**
 * @author Marius Oehler
 *
 */
@Component
public class ClassifierFactory {

	@Autowired
	private BeanFactory beanFactory;

	public AbstractClassifier<?> createClassifier(IClassifierDefinition classifierDefinition) {
		if (classifierDefinition instanceof StandardDeviationClassifierDefinition) {
			StandardDeviationClassifier classifier = beanFactory.getBean(StandardDeviationClassifier.class);
			classifier.setDefinition((StandardDeviationClassifierDefinition) classifierDefinition);
			return classifier;
		} else {
			throw new UnsupportedMetricProviderException("Unsupported classifier for definition of type '" + classifierDefinition.getClass().getSimpleName() + "'.");
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
		public UnsupportedClassifierException(String message) {
			super(message);
		}
	}
}
