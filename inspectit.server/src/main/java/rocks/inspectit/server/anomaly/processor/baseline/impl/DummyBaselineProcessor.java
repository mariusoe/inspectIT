package rocks.inspectit.server.anomaly.processor.baseline.impl;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.context.model.ConfidenceBand;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.processor.baseline.AbstractBaselineProcessor;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.baseline.DummyBaselineProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class DummyBaselineProcessor extends AbstractBaselineProcessor<DummyBaselineProcessorConfiguration> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(AnomalyContext context, Collection<AnalyzableData<?>> data) {
		if (CollectionUtils.isEmpty(data)) {
			return;
		}

		SummaryStatistics statistics = new SummaryStatistics();
		for (AnalyzableData<?> analyzableData : data) {
			statistics.addValue(analyzableData.getValue());
		}

		double standardDeviation = statistics.getStandardDeviation();

		ConfidenceBand confidenceBand = new ConfidenceBand(statistics.getMean() + standardDeviation, statistics.getMean() - standardDeviation);

		context.setBaseline(statistics.getMean());
		context.setConfidenceBand(confidenceBand);

		System.out.println("mean of " + statistics.getN() + " elements has been " + statistics.getMean());
		System.out.println("new band is: " + confidenceBand);
	}

}
