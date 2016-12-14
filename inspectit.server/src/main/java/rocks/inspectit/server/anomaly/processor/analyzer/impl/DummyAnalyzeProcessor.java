package rocks.inspectit.server.anomaly.processor.analyzer.impl;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.processor.analyzer.AbstractAnalyzeProcessor;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.analyze.DummyAnalyzeProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class DummyAnalyzeProcessor extends AbstractAnalyzeProcessor<DummyAnalyzeProcessorConfiguration> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void analyze(AnalyzableData<?> analyzable) {
		System.out.println("analyze: " + analyzable);
	}

}
