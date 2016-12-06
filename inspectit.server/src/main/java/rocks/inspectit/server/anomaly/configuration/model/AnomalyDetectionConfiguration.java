package rocks.inspectit.server.anomaly.configuration.model;

import java.util.ArrayList;
import java.util.Collection;

import rocks.inspectit.server.anomaly.context.matcher.IAnomalyContextMatcher;
import rocks.inspectit.server.anomaly.processor.analyzer.IAnalyzeProcessor;
import rocks.inspectit.server.anomaly.processor.baseline.IBaselineProcessor;
import rocks.inspectit.server.anomaly.processor.classifier.IClassifyProcessor;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyDetectionConfiguration {

	private IBaselineProcessorConfiguration<? extends IBaselineProcessor> baselineProcessorConfiguration;

	private IAnalyzeProcessorConfiguration<? extends IAnalyzeProcessor> analyzeProcessorConfiguration;

	private IClassifyProcessorConfiguration<? extends IClassifyProcessor> classifyProcessorConfiguration;

	private Collection<IAnomalyContextMatcher> contextMatcher = new ArrayList<>(0);

	/**
	 * Gets {@link #contextMatcher}.
	 *
	 * @return {@link #contextMatcher}
	 */
	public Collection<IAnomalyContextMatcher> getContextMatcher() {
		return this.contextMatcher;
	}

	/**
	 * Sets {@link #contextMatcher}.
	 *
	 * @param contextMatcher
	 *            New value for {@link #contextMatcher}
	 */
	public void setContextMatcher(Collection<IAnomalyContextMatcher> contextMatcher) {
		this.contextMatcher = contextMatcher;
	}

	/**
	 * Gets {@link #baselineProcessorConfiguration}.
	 *
	 * @return {@link #baselineProcessorConfiguration}
	 */
	public IBaselineProcessorConfiguration<? extends IBaselineProcessor> getBaselineProcessorConfiguration() {
		return this.baselineProcessorConfiguration;
	}

	/**
	 * Sets {@link #baselineProcessorConfiguration}.
	 *
	 * @param baselineProcessorConfiguration
	 *            New value for {@link #baselineProcessorConfiguration}
	 */
	public void setBaselineProcessorConfiguration(IBaselineProcessorConfiguration<? extends IBaselineProcessor> baselineProcessorConfiguration) {
		this.baselineProcessorConfiguration = baselineProcessorConfiguration;
	}

	/**
	 * Gets {@link #analyzeProcessorConfiguration}.
	 *
	 * @return {@link #analyzeProcessorConfiguration}
	 */
	public IAnalyzeProcessorConfiguration<? extends IAnalyzeProcessor> getAnalyzeProcessorConfiguration() {
		return this.analyzeProcessorConfiguration;
	}

	/**
	 * Sets {@link #analyzeProcessorConfiguration}.
	 *
	 * @param analyzeProcessorConfiguration
	 *            New value for {@link #analyzeProcessorConfiguration}
	 */
	public void setAnalyzeProcessorConfiguration(IAnalyzeProcessorConfiguration<? extends IAnalyzeProcessor> analyzeProcessorConfiguration) {
		this.analyzeProcessorConfiguration = analyzeProcessorConfiguration;
	}

	/**
	 * Gets {@link #classifyProcessorConfiguration}.
	 *
	 * @return {@link #classifyProcessorConfiguration}
	 */
	public IClassifyProcessorConfiguration<? extends IClassifyProcessor> getClassifyProcessorConfiguration() {
		return this.classifyProcessorConfiguration;
	}

	/**
	 * Sets {@link #classifyProcessorConfiguration}.
	 *
	 * @param classifyProcessorConfiguration
	 *            New value for {@link #classifyProcessorConfiguration}
	 */
	public void setClassifyProcessorConfiguration(IClassifyProcessorConfiguration<? extends IClassifyProcessor> classifyProcessorConfiguration) {
		this.classifyProcessorConfiguration = classifyProcessorConfiguration;
	}

}
