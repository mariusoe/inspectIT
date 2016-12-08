package rocks.inspectit.server.anomaly.configuration.model;

import java.util.ArrayList;
import java.util.Collection;

import rocks.inspectit.server.anomaly.context.matcher.IAnomalyContextMatcher;
import rocks.inspectit.server.anomaly.processor.analyzer.AbstractAnalyzeProcessor;
import rocks.inspectit.server.anomaly.processor.baseline.AbstractBaselineProcessor;
import rocks.inspectit.server.anomaly.processor.classifier.AbstractClassifyProcessor;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyDetectionConfiguration {

	private IBaselineProcessorConfiguration<? extends AbstractBaselineProcessor<?>> baselineProcessorConfiguration;

	private IAnalyzeProcessorConfiguration<? extends AbstractAnalyzeProcessor<?>> analyzeProcessorConfiguration;

	private IClassifyProcessorConfiguration<? extends AbstractClassifyProcessor<?>> classifyProcessorConfiguration;

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
	public IBaselineProcessorConfiguration<? extends AbstractBaselineProcessor<?>> getBaselineProcessorConfiguration() {
		return this.baselineProcessorConfiguration;
	}

	/**
	 * Sets {@link #baselineProcessorConfiguration}.
	 *
	 * @param baselineProcessorConfiguration
	 *            New value for {@link #baselineProcessorConfiguration}
	 */
	public void setBaselineProcessorConfiguration(IBaselineProcessorConfiguration<? extends AbstractBaselineProcessor<?>> baselineProcessorConfiguration) {
		this.baselineProcessorConfiguration = baselineProcessorConfiguration;
	}

	/**
	 * Gets {@link #analyzeProcessorConfiguration}.
	 *
	 * @return {@link #analyzeProcessorConfiguration}
	 */
	public IAnalyzeProcessorConfiguration<? extends AbstractAnalyzeProcessor<?>> getAnalyzeProcessorConfiguration() {
		return this.analyzeProcessorConfiguration;
	}

	/**
	 * Sets {@link #analyzeProcessorConfiguration}.
	 *
	 * @param analyzeProcessorConfiguration
	 *            New value for {@link #analyzeProcessorConfiguration}
	 */
	public void setAnalyzeProcessorConfiguration(IAnalyzeProcessorConfiguration<? extends AbstractAnalyzeProcessor<?>> analyzeProcessorConfiguration) {
		this.analyzeProcessorConfiguration = analyzeProcessorConfiguration;
	}

	/**
	 * Gets {@link #classifyProcessorConfiguration}.
	 *
	 * @return {@link #classifyProcessorConfiguration}
	 */
	public IClassifyProcessorConfiguration<? extends AbstractClassifyProcessor<?>> getClassifyProcessorConfiguration() {
		return this.classifyProcessorConfiguration;
	}

	/**
	 * Sets {@link #classifyProcessorConfiguration}.
	 *
	 * @param classifyProcessorConfiguration
	 *            New value for {@link #classifyProcessorConfiguration}
	 */
	public void setClassifyProcessorConfiguration(IClassifyProcessorConfiguration<? extends AbstractClassifyProcessor<?>> classifyProcessorConfiguration) {
		this.classifyProcessorConfiguration = classifyProcessorConfiguration;
	}

}
