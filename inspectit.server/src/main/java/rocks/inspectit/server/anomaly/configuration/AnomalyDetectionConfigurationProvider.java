package rocks.inspectit.server.anomaly.configuration;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.configuration.model.AnomalyDetectionConfiguration;
import rocks.inspectit.server.anomaly.context.matcher.IAnomalyContextMatcher;
import rocks.inspectit.server.anomaly.context.matcher.impl.BusinessTransactionMatcher;
import rocks.inspectit.server.anomaly.processor.analyzer.impl.DummyAnalyzeProcessor;
import rocks.inspectit.server.anomaly.processor.baseline.impl.DummyBaselineProcessor;
import rocks.inspectit.server.anomaly.processor.classifier.impl.DummyClassifyProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyDetectionConfigurationProvider {

	private Collection<AnomalyDetectionConfiguration> configurations = new ArrayList<>();

	@PostConstruct
	public void DUMMY() {
		// TODO
		AnomalyDetectionConfiguration detectionConfiguration = new AnomalyDetectionConfiguration();
		detectionConfiguration.getContextMatcher().add(new BusinessTransactionMatcher("testTransaction"));
		detectionConfiguration.setAnalyzeProcessorConfiguration(new DummyAnalyzeProcessor.Configuration());
		detectionConfiguration.setBaselineProcessorConfiguration(new DummyBaselineProcessor.Configuration());
		detectionConfiguration.setClassifyProcessorConfiguration(new DummyClassifyProcessor.Configuration(15D));
		configurations.add(detectionConfiguration);
	}

	public AnomalyDetectionConfiguration getConfiguration(DefaultData defaultData) {
		for (AnomalyDetectionConfiguration configuration : configurations) {
			for (IAnomalyContextMatcher matcher : configuration.getContextMatcher()) {
				if (matcher.matches(defaultData)) {
					return configuration;
				}
			}
		}

		return null;
	}

}
