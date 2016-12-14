package rocks.inspectit.server.anomaly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.ci.ConfigurationInterfaceManager;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.impl.MachineMatcherConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.analyze.DummyAnalyzeProcessorConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.baseline.DummyBaselineProcessorConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.classify.DummyClassifyProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyDetectionConfigurationProvider {

	private Collection<AnomalyDetectionConfiguration> configurations = new ArrayList<>();

	@Autowired
	ConfigurationInterfaceManager config;

	@PostConstruct
	public void DUMMY() {
		// TODO
		// BusinessTransactionMatcherConfiguration matcherConfiguration = new
		// BusinessTransactionMatcherConfiguration();
		// matcherConfiguration.setBuisnessTransactionPattern("testTransaction");
		MachineMatcherConfiguration matcherConfiguration = new MachineMatcherConfiguration();
		matcherConfiguration.setAgentName("TicketMonster");

		DummyClassifyProcessorConfiguration dummyClassifyProcessorConfiguration = new DummyClassifyProcessorConfiguration();
		dummyClassifyProcessorConfiguration.setThreshold(15D);

		AnomalyDetectionConfiguration detectionConfiguration = new AnomalyDetectionConfiguration();
		detectionConfiguration.getContextMatcher().add(matcherConfiguration);
		detectionConfiguration.setAnalyzeProcessorConfiguration(new DummyAnalyzeProcessorConfiguration());
		detectionConfiguration.setBaselineProcessorConfiguration(new DummyBaselineProcessorConfiguration());
		detectionConfiguration.setClassifyProcessorConfiguration(dummyClassifyProcessorConfiguration);
		configurations.add(detectionConfiguration);

		try {
			config.createAnomalyDetectionConfiguration(detectionConfiguration);
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Collection<AnomalyDetectionConfiguration> getConfigurations() {
		return new ArrayList<>(configurations);
	}

}
