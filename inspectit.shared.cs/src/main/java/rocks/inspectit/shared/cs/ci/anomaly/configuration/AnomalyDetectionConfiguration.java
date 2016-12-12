package rocks.inspectit.shared.cs.ci.anomaly.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.AbstractContextMatcherConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractAnalyzeProcessorConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractBaselineProcessorConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractClassifyProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "anomaly-detection-configuration")
public class AnomalyDetectionConfiguration implements Serializable {

	private String id;

	// @XmlAttribute(name = "analyze-processor")
	private AbstractAnalyzeProcessorConfiguration analyzeProcessorConfiguration;

	// @XmlAttribute(name = "baseline-processor")
	private AbstractBaselineProcessorConfiguration baselineProcessorConfiguration;

	// @XmlAttribute(name = "classify-processor")
	private AbstractClassifyProcessorConfiguration classifyProcessorConfiguration;

	// @XmlAttribute(name = "context-matcher")
	private Collection<AbstractContextMatcherConfiguration> contextMatcher = new ArrayList<>(0);

	/**
	 * Gets {@link #id}.
	 *
	 * @return {@link #id}
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Sets {@link #id}.
	 *
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets {@link #analyzeProcessorConfiguration}.
	 *
	 * @return {@link #analyzeProcessorConfiguration}
	 */
	public AbstractAnalyzeProcessorConfiguration getAnalyzeProcessorConfiguration() {
		return this.analyzeProcessorConfiguration;
	}

	/**
	 * Sets {@link #analyzeProcessorConfiguration}.
	 *
	 * @param analyzeProcessorConfiguration
	 *            New value for {@link #analyzeProcessorConfiguration}
	 */
	public void setAnalyzeProcessorConfiguration(AbstractAnalyzeProcessorConfiguration analyzeProcessorConfiguration) {
		this.analyzeProcessorConfiguration = analyzeProcessorConfiguration;
	}

	/**
	 * Gets {@link #baselineProcessorConfiguration}.
	 *
	 * @return {@link #baselineProcessorConfiguration}
	 */
	public AbstractBaselineProcessorConfiguration getBaselineProcessorConfiguration() {
		return this.baselineProcessorConfiguration;
	}

	/**
	 * Sets {@link #baselineProcessorConfiguration}.
	 *
	 * @param baselineProcessorConfiguration
	 *            New value for {@link #baselineProcessorConfiguration}
	 */
	public void setBaselineProcessorConfiguration(AbstractBaselineProcessorConfiguration baselineProcessorConfiguration) {
		this.baselineProcessorConfiguration = baselineProcessorConfiguration;
	}

	/**
	 * Gets {@link #classifyProcessorConfiguration}.
	 *
	 * @return {@link #classifyProcessorConfiguration}
	 */
	public AbstractClassifyProcessorConfiguration getClassifyProcessorConfiguration() {
		return this.classifyProcessorConfiguration;
	}

	/**
	 * Sets {@link #classifyProcessorConfiguration}.
	 *
	 * @param classifyProcessorConfiguration
	 *            New value for {@link #classifyProcessorConfiguration}
	 */
	public void setClassifyProcessorConfiguration(AbstractClassifyProcessorConfiguration classifyProcessorConfiguration) {
		this.classifyProcessorConfiguration = classifyProcessorConfiguration;
	}

	/**
	 * Gets {@link #contextMatcher}.
	 *
	 * @return {@link #contextMatcher}
	 */
	public Collection<AbstractContextMatcherConfiguration> getContextMatcher() {
		return this.contextMatcher;
	}
}
