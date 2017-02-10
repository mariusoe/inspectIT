package rocks.inspectit.server.__anomaly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.__anomaly.processing.AnomalyProcessingUnit;
import rocks.inspectit.server.__anomaly.processing.AnomalyProcessingUnitFactory;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.anomaly.classification.HealthState;
import rocks.inspectit.shared.cs.anomaly.configuration.AnomalyDetectionConfiguration;
import rocks.inspectit.shared.cs.anomaly.extractor.IValueExtractor;
import rocks.inspectit.shared.cs.anomaly.extractor.impl.InvocationSequenceDurationExtractor;
import rocks.inspectit.shared.cs.anomaly.selector.impl.AgentNameSelector;
import rocks.inspectit.shared.cs.anomaly.selector.impl.AgentNameSelector.AgentNameSelectorConfiguration;

/**
 * @author Marius Oehler
 *
 */
// @Component
public class AnomalyDetectionSystem {

	AnomalyDetectionConfiguration testConfig;

	/**
	 * Logger for the class.
	 */
	@Log
	private Logger log;

	@Autowired
	private AnomalyProcessingUnitFactory processingUnitFactory;

	@Autowired
	private List<IValueExtractor<?>> valueExtractors;

	private Map<Class<?>, List<IValueExtractor<?>>> valueExtractorMap;

	private List<AnomalyProcessingUnit> processingUnits = new ArrayList<>();

	/**
	 * Gets {@link #valueExtractorMap}.
	 *
	 * @return {@link #valueExtractorMap}
	 */
	public Map<Class<?>, List<IValueExtractor<?>>> getValueExtractorMap() {
		return this.valueExtractorMap;
	}

	public HealthState classify(DefaultData data) {
		HealthState healthState = HealthState.UNKNOWN;

		for (AnomalyProcessingUnit processingUnit : processingUnits) {
			if (processingUnit.select(data)) {
				HealthState state = processingUnit.process(data);

				if (state == HealthState.CRITICAL) {
					healthState = HealthState.CRITICAL;
				} else if ((state == HealthState.WARNING) && (healthState != HealthState.CRITICAL)) {
					healthState = HealthState.WARNING;
				} else if (state == HealthState.NORMAL) {
					healthState = HealthState.NORMAL;
				}
			}
		}

		return healthState;
	}

	@PostConstruct
	public void postConstruct() {
		if (log.isInfoEnabled()) {
			log.info("|-Anomaly Detection System has been initialized..");
		}

		/// test
		testConfig = new AnomalyDetectionConfiguration();
		AgentNameSelectorConfiguration ansConfig = new AgentNameSelector.AgentNameSelectorConfiguration();
		ansConfig.setTargetAgentName("DummyAgent");
		testConfig.getDataSelectors().add(ansConfig);
		testConfig.setValueExtractorClass(InvocationSequenceDurationExtractor.class);


		AnomalyProcessingUnit processingUnit = processingUnitFactory.createProcessingUnit(testConfig);
		processingUnits.add(processingUnit);

		Map<Class<?>, List<IValueExtractor<?>>> extractorMap = new HashMap<>();
		for (IValueExtractor<?> valueExtractor : valueExtractors) {
			List<IValueExtractor<?>> valueExtractorList = extractorMap.get(valueExtractor.getDataClass());
			if (valueExtractorList == null) {
				valueExtractorList = new ArrayList<>();
				extractorMap.put(valueExtractor.getDataClass(), valueExtractorList);
			}
			valueExtractorList.add(valueExtractor);
		}
		valueExtractorMap = Collections.unmodifiableMap(extractorMap);

		log.info("||-Found {} value extractor(s).", valueExtractors.size());
	}

	public boolean canBeProcessed(DefaultData data) {
		for (AnomalyProcessingUnit processingUnit : processingUnits) {
			if (processingUnit.select(data)) {
				return true;
			}
		}
		return false;
	}
}
