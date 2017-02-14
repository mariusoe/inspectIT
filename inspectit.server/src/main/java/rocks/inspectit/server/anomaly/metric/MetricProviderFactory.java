package rocks.inspectit.server.anomaly.metric;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.metric.definition.AbstractMetricDefinition;
import rocks.inspectit.server.anomaly.metric.definition.InfluxDBMetricDefinition;
import rocks.inspectit.server.anomaly.metric.provider.InfluxDBMetricProvider;

/**
 * @author Marius Oehler
 *
 */
@Component
public class MetricProviderFactory {

	@Autowired
	private BeanFactory beanFactory;

	public AbstractMetricProvider<?> getMetricProvider(AbstractMetricDefinition metricDefinition) {
		if (metricDefinition instanceof InfluxDBMetricDefinition) {
			InfluxDBMetricProvider metricProvider = beanFactory.getBean(InfluxDBMetricProvider.class);
			metricProvider.setMetricDefinition((InfluxDBMetricDefinition) metricDefinition);
			return metricProvider;
		} else {
			throw new UnsupportedMetricProviderException("Unsupported metric provider for definition of type '" + metricDefinition.getClass().getSimpleName() + "'.");
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
		public UnsupportedMetricProviderException(String message) {
			super(message);
		}

	}
}
