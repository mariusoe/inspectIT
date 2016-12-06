package rocks.inspectit.server.anomaly.configuration;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.configuration.model.AnomalyDetectionConfiguration;
import rocks.inspectit.server.anomaly.configuration.model.analyzer.DummyAnalyzeProcessorConfiguration;
import rocks.inspectit.server.anomaly.configuration.model.baseline.DummyBaselineProcessorConfiguration;
import rocks.inspectit.server.anomaly.configuration.model.classifier.DummyClassifyProcessorConfiguration;
import rocks.inspectit.server.anomaly.context.matcher.IAnomalyContextMatcher;
import rocks.inspectit.server.anomaly.context.matcher.impl.StarMatcher;
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
		detectionConfiguration.getContextMatcher().add(new StarMatcher());
		detectionConfiguration.setAnalyzeProcessorConfiguration(new DummyAnalyzeProcessorConfiguration());
		detectionConfiguration.setBaselineProcessorConfiguration(new DummyBaselineProcessorConfiguration());
		detectionConfiguration.setClassifyProcessorConfiguration(new DummyClassifyProcessorConfiguration());
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
