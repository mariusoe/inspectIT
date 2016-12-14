package rocks.inspectit.server.anomaly.context.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import rocks.inspectit.server.anomaly.context.matcher.AbstractAnomalyContextMatcher;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.processor.analyzer.AbstractAnalyzeProcessor;
import rocks.inspectit.server.anomaly.processor.baseline.AbstractBaselineProcessor;
import rocks.inspectit.server.anomaly.processor.classifier.AbstractClassifyProcessor;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyContext {

	private String id;

	private AnomalyStatus status = AnomalyStatus.UNKOWN;

	private double baseline = Double.NaN;

	private SummaryStatistics standardDeviation = new SummaryStatistics();

	private ConfidenceBand confidenceBand;

	private Collection<AnalyzableData<?>> intervalBuffer = new LinkedList<>();

	private Collection<AbstractAnomalyContextMatcher> contextMatcher = new ArrayList<>(0);

	private AbstractBaselineProcessor baselineProcessor;

	private AbstractClassifyProcessor classifyProcessor;

	private AbstractAnalyzeProcessor analyzeProcessor;

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
	 * Gets {@link #confidenceBand}.
	 *
	 * @return {@link #confidenceBand}
	 */
	public ConfidenceBand getConfidenceBand() {
		return this.confidenceBand;
	}

	/**
	 * Sets {@link #confidenceBand}.
	 *
	 * @param confidenceBand
	 *            New value for {@link #confidenceBand}
	 */
	public void setConfidenceBand(ConfidenceBand confidenceBand) {
		this.confidenceBand = confidenceBand;
	}

	/**
	 * Gets {@link #status}.
	 *
	 * @return {@link #status}
	 */
	public AnomalyStatus getStatus() {
		return this.status;
	}

	/**
	 * Sets {@link #status}.
	 *
	 * @param status
	 *            New value for {@link #status}
	 */
	public void setStatus(AnomalyStatus status) {
		this.status = status;
	}

	/**
	 * Gets {@link #baselineProcessor}.
	 *
	 * @return {@link #baselineProcessor}
	 */
	public AbstractBaselineProcessor getBaselineProcessor() {
		return this.baselineProcessor;
	}

	/**
	 * Sets {@link #baselineProcessor}.
	 *
	 * @param baselineProcessor
	 *            New value for {@link #baselineProcessor}
	 */
	public void setBaselineProcessor(AbstractBaselineProcessor baselineProcessor) {
		this.baselineProcessor = baselineProcessor;
	}

	/**
	 * Gets {@link #classifyProcessor}.
	 *
	 * @return {@link #classifyProcessor}
	 */
	public AbstractClassifyProcessor getClassifyProcessor() {
		return this.classifyProcessor;
	}

	/**
	 * Sets {@link #classifyProcessor}.
	 *
	 * @param classifyProcessor
	 *            New value for {@link #classifyProcessor}
	 */
	public void setClassifyProcessor(AbstractClassifyProcessor classifyProcessor) {
		this.classifyProcessor = classifyProcessor;
	}

	/**
	 * Gets {@link #analyzeProcessor}.
	 *
	 * @return {@link #analyzeProcessor}
	 */
	public AbstractAnalyzeProcessor getAnalyzeProcessor() {
		return analyzeProcessor;
	}

	/**
	 * Sets {@link #analyzeProcessor}.
	 *
	 * @param analyzeProcessor
	 *            New value for {@link #analyzeProcessor}
	 */
	public void setAnalyzeProcessor(AbstractAnalyzeProcessor analyzeProcessor) {
		this.analyzeProcessor = analyzeProcessor;
	}

	/**
	 * Gets {@link #baseline}.
	 *
	 * @return {@link #baseline}
	 */
	public double getBaseline() {
		return this.baseline;
	}

	/**
	 * Sets {@link #baseline}.
	 *
	 * @param baseline
	 *            New value for {@link #baseline}
	 */
	public void setBaseline(double baseline) {
		this.baseline = baseline;
	}

	/**
	 * Gets {@link #intervalBuffer}.
	 *
	 * @return {@link #intervalBuffer}
	 */
	public Collection<AnalyzableData<?>> getIntervalBuffer() {
		return this.intervalBuffer;
	}

	/**
	 * Gets {@link #standardDeviation}.
	 *
	 * @return {@link #standardDeviation}
	 */
	public SummaryStatistics getStandardDeviation() {
		return this.standardDeviation;
	}

	/**
	 * Gets {@link #contextMatcher}.
	 *
	 * @return {@link #contextMatcher}
	 */
	public Collection<AbstractAnomalyContextMatcher> getContextMatcher() {
		return this.contextMatcher;
	}

	public Collection<AnalyzableData<?>> swapBuffer() {
		Collection<AnalyzableData<?>> currentBuffer = intervalBuffer;
		intervalBuffer = new LinkedList<>();
		return currentBuffer;
	}
}
