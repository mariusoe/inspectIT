package rocks.inspectit.server.anomaly.baseline;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.baseline.impl.MovingAverageBaseline;
import rocks.inspectit.server.anomaly.baseline.impl.MovingAverageBaselineDefinition;
import rocks.inspectit.server.anomaly.metric.MetricProviderFactory.UnsupportedMetricProviderException;

/**
 * @author Marius Oehler
 *
 */
@Component
public class BaselineFactory {

	@Autowired
	BeanFactory beanFactory;

	public AbstractBaseline<?> getBaseline(BaselineDefinition baselineDefinition) {
		if (baselineDefinition instanceof MovingAverageBaselineDefinition) {
			MovingAverageBaseline baseline = beanFactory.getBean(MovingAverageBaseline.class);
			baseline.setBaselineDefinition((MovingAverageBaselineDefinition) baselineDefinition);
			return baseline;
		} else {
			throw new UnsupportedMetricProviderException("Unsupported baseline for definition of type '" + baselineDefinition.getClass().getSimpleName() + "'.");
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
		public UnsupportedBaselineException(String message) {
			super(message);
		}
	}
}