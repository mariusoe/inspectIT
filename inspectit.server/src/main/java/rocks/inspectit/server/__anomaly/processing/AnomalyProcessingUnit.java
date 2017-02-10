package rocks.inspectit.server.__anomaly.processing;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.anomaly.classification.HealthState;
import rocks.inspectit.shared.cs.anomaly.configuration.AnomalyDetectionConfiguration;
import rocks.inspectit.shared.cs.anomaly.extractor.IValueExtractor;
import rocks.inspectit.shared.cs.anomaly.selector.AbstractDataSelector;

/**
 * @author Marius Oehler
 *
 */
// @Component
public class AnomalyProcessingUnit {

	@Log
	private Logger log;

	private List<AbstractDataSelector<DefaultData, ?>> dataSelectors = new ArrayList<>();

	private IValueExtractor<DefaultData> valueExtractor;

	private AnomalyDetectionConfiguration configuration;

	/**
	 * Sets {@link #configuration}.
	 *
	 * @param configuration
	 *            New value for {@link #configuration}
	 */
	public void setConfiguration(AnomalyDetectionConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Gets {@link #configuration}.
	 *
	 * @return {@link #configuration}
	 */
	public AnomalyDetectionConfiguration getConfiguration() {
		return this.configuration;
	}

	/**
	 * Gets {@link #dataSelectors}.
	 *
	 * @return {@link #dataSelectors}
	 */
	public List<AbstractDataSelector<DefaultData, ?>> getDataSelectors() {
		return this.dataSelectors;
	}

	public boolean select(DefaultData data) {
		if (!valueExtractor.getDataClass().isInstance(data)) {
			return false;
		}

		// logical operators AND OR
		for (AbstractDataSelector<DefaultData, ?> dataSelector : dataSelectors) {
			if (dataSelector.select(data)) {
				return true;
			}
		}
		return false;
	}

	public HealthState process(DefaultData defaultData) {
		double value = valueExtractor.extractValue(defaultData);

		log.info("Config '{}' is value: {}", configuration.getUuid(), value);

		return HealthState.UNKNOWN;
	}

	/**
	 * Sets {@link #valueExtractor}.
	 *
	 * @param valueExtractor
	 *            New value for {@link #valueExtractor}
	 */
	public void setValueExtractor(IValueExtractor<DefaultData> valueExtractor) {
		this.valueExtractor = valueExtractor;
	}

}
