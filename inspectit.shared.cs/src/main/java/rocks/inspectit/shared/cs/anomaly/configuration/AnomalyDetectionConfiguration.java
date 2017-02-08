package rocks.inspectit.shared.cs.anomaly.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import rocks.inspectit.shared.cs.anomaly.extractor.IValueExtractor;
import rocks.inspectit.shared.cs.anomaly.selector.IDataSelectorConfiguration;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyDetectionConfiguration {

	private String uuid = UUID.randomUUID().toString();

	private String name;

	private List<IDataSelectorConfiguration> dataSelectors = new ArrayList<>();

	private Class<? extends IValueExtractor<?>> valueExtractorClass;

	/**
	 * Gets {@link #name}.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets {@link #name}.
	 *
	 * @param name
	 *            New value for {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets {@link #dataSelectors}.
	 *
	 * @return {@link #dataSelectors}
	 */
	public List<IDataSelectorConfiguration> getDataSelectors() {
		return this.dataSelectors;
	}

	/**
	 * Sets {@link #dataSelectors}.
	 *
	 * @param dataSelectors
	 *            New value for {@link #dataSelectors}
	 */
	public void setDataSelectors(List<IDataSelectorConfiguration> dataSelectors) {
		this.dataSelectors = dataSelectors;
	}

	/**
	 * Gets {@link #valueExtractorClass}.
	 *
	 * @return {@link #valueExtractorClass}
	 */
	public Class<? extends IValueExtractor<?>> getValueExtractorClass() {
		return this.valueExtractorClass;
	}

	/**
	 * Sets {@link #valueExtractorClass}.
	 *
	 * @param valueExtractorClass
	 *            New value for {@link #valueExtractorClass}
	 */
	public void setValueExtractorClass(Class<? extends IValueExtractor<?>> valueExtractorClass) {
		this.valueExtractorClass = valueExtractorClass;
	}

	/**
	 * Gets {@link #uuid}.
	 *
	 * @return {@link #uuid}
	 */
	public String getUuid() {
		return this.uuid;
	}
}
